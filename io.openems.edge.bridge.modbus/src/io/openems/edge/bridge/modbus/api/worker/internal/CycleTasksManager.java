package io.openems.edge.bridge.modbus.api.worker.internal;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.bridge.modbus.api.LogVerbosity;
import io.openems.edge.bridge.modbus.api.task.Task;
import io.openems.edge.bridge.modbus.api.task.WaitTask;
import io.openems.edge.bridge.modbus.api.worker.ModbusWorker;

/**
 * Manages the Read-, Write- and Wait-Tasks for one Cycle.
 * 
 * <p>
 * <li>{@link #onBeforeProcessImage()} initialize the next Cycle if previous
 * Cycle had finished
 * <li>{@link #onExecuteWrite()} puts Write-Tasks as highest priority
 */
public class CycleTasksManager {

	private final Logger log = LoggerFactory.getLogger(CycleTasksManager.class);

	private final TasksSupplier tasksSupplier;
	private final DefectiveComponents defectiveComponents;
	private final Consumer<Boolean> cycleTimeIsTooShortChannel;
	private final AtomicReference<LogVerbosity> logVerbosity;

	private final WaitDelayHandler waitDelayHandler;
	private final WaitTask.Mutex waitMutexTask = new WaitTask.Mutex();

	private CycleTasks cycleTasks;

	public CycleTasksManager(TasksSupplier tasksSupplier, DefectiveComponents defectiveComponents,
			Consumer<Boolean> cycleTimeIsTooShortChannel, Consumer<Long> cycleDelayChannel,
			AtomicReference<LogVerbosity> logVerbosity) {
		this.tasksSupplier = tasksSupplier;
		this.defectiveComponents = defectiveComponents;
		this.cycleTimeIsTooShortChannel = cycleTimeIsTooShortChannel;
		this.logVerbosity = logVerbosity;

		this.waitDelayHandler = new WaitDelayHandler(() -> this.onWaitDelayTaskFinished(), cycleDelayChannel);
	}

	protected CycleTasksManager(TasksSupplier tasksSupplier, DefectiveComponents defectiveComponents,
			Consumer<Boolean> cycleTimeIsTooShortChannel, Consumer<Long> cycleDelayChannel) {
		this(tasksSupplier, defectiveComponents, cycleTimeIsTooShortChannel, cycleDelayChannel,
				new AtomicReference<>(LogVerbosity.NONE));
	}

	private static enum StateMachine {
		INITIAL_WAIT, //
		READ_BEFORE_WRITE, //
		WAIT_FOR_WRITE, //
		WRITE, //
		WAIT_BEFORE_READ, //
		READ_AFTER_WRITE, //
		FINISHED
	}

	private StateMachine state = StateMachine.FINISHED;

	/**
	 * Called on BEFORE_PROCESS_IMAGE event.
	 */
	public synchronized void onBeforeProcessImage() {
		// Calculate Delay
		var waitDelayHandlerLog = this.waitDelayHandler.onBeforeProcessImage(this.isTraceLog());

		// Evaluate Cycle-Time-Is-Too-Short, invalidate time measurement and stop early
		var cycleTimeIsTooShort = this.state != StateMachine.FINISHED;
		this.cycleTimeIsTooShortChannel.accept(cycleTimeIsTooShort);
		if (cycleTimeIsTooShort) {
			this.waitDelayHandler.timeIsInvalid();
			if (this.isTraceLog()) {
				this.log.info("State: " + this.state + " unchanged" //
						+ " (in onBeforeProcessImage)" //
						+ " Delay [" + this.waitDelayHandler.getWaitDelayTask().initialDelay + "] " //
						+ waitDelayHandlerLog);
			}
			return;
		}

		// Update WaitDelayHandler Queue size
		this.waitDelayHandler.updateTotalNumberOfTasks(this.tasksSupplier.getTotalNumberOfTasks());

		// Fill queues for this Cycle
		this.cycleTasks = this.tasksSupplier.getCycleTasks(this.defectiveComponents);

		// On defectiveComponents invalidate time measurement
		if (this.cycleTasks.containsDefectiveComponent(this.defectiveComponents)) {
			this.waitDelayHandler.timeIsInvalid();
			waitDelayHandlerLog += " DEFECTIVE_COMPONENT";
		}

		// Initialize next Cycle
		if (this.isTraceLog()) {
			this.log.info("State: " + this.state + " -> " + StateMachine.INITIAL_WAIT //
					+ " (in onBeforeProcessImage)" //
					+ " Delay [" + this.waitDelayHandler.getWaitDelayTask().initialDelay + "] " //
					+ waitDelayHandlerLog);
		}
		this.state = StateMachine.INITIAL_WAIT;

		// Interrupt wait
		this.waitMutexTask.release();
	}

	/**
	 * Called on EXECUTE_WRITE event.
	 */
	public synchronized void onExecuteWrite() {
		if (this.isTraceLog()) {
			this.log.info("State: " + this.state + " -> " + StateMachine.WRITE + " (onExecuteWrite)");
		}

		this.state = StateMachine.WRITE;
		this.waitMutexTask.release();
	}

	/**
	 * Gets the next {@link Task}. This is called in a separate Thread by
	 * {@link ModbusWorker}.
	 * 
	 * @return next {@link Task}
	 */
	public Task getNextTask() {
		if (this.cycleTasks == null) {
			return this.waitMutexTask;
		}

		var previousState = this.state; // drop before release

		var nextTask = switch (this.state) {

		case INITIAL_WAIT ->
			// Waiting for planned waiting time to pass
			this.waitDelayHandler.getWaitDelayTask();

		case READ_BEFORE_WRITE -> {
			// Read-Task available?
			var task = this.cycleTasks.reads().poll();
			if (task != null) {
				yield task;
			}
			// Otherwise -> next state + recursive call
			this.state = StateMachine.WAIT_FOR_WRITE;
			yield this.getNextTask();
		}

		case WAIT_FOR_WRITE ->
			// Waiting for EXECUTE_WRITE event
			this.waitMutexTask;

		case WRITE -> {
			// Write-Task available?
			var task = this.cycleTasks.writes().poll();
			if (task != null) {
				yield task;
			}
			// Otherwise -> next state + recursive call
			this.state = StateMachine.WAIT_BEFORE_READ;
			yield this.getNextTask();
		}

		case WAIT_BEFORE_READ ->
			// Waiting for planned waiting time to pass
			this.waitDelayHandler.getWaitDelayTask();

		case READ_AFTER_WRITE -> {
			// Read-Task available?
			var task = this.cycleTasks.reads().poll();
			if (task != null) {
				yield task;
			}
			// Otherwise -> next state + recursive call
			this.state = StateMachine.FINISHED;
			yield this.getNextTask();
		}

		case FINISHED -> {
			this.waitDelayHandler.onFinished();
			// Waiting for BEFORE_PROCESS_IMAGE event
			yield this.waitMutexTask;
		}
		};

		if (this.state != previousState && this.isTraceLog()) {
			this.log.info("State: " + previousState + " -> " + this.state + " (getNextTask)");
		}
		return nextTask;
	}

	/**
	 * Waiting in INITIAL_WAIT or WAIT_BEFORE_READ finished.
	 */
	private synchronized void onWaitDelayTaskFinished() {
		var previousState = this.state;
		this.state = switch (this.state) {
		// Expected
		case INITIAL_WAIT -> StateMachine.READ_BEFORE_WRITE;
		case WAIT_BEFORE_READ -> StateMachine.READ_AFTER_WRITE;
		// Unexpected (the State has been unexpectedly changed in-between)
		default -> this.state;
		};

		if (this.state != previousState) {
			if (this.isTraceLog()) {
				this.log.info("State: " + previousState + " -> " + this.state + " (onWaitDelayTaskFinished)");
			}
		}
	}

	private boolean isTraceLog() {
		return switch (this.logVerbosity.get()) {
		case READS_AND_WRITES_DURATION_TRACE_EVENTS -> true;
		case NONE, DEBUG_LOG, READS_AND_WRITES, READS_AND_WRITES_DURATION, READS_AND_WRITES_VERBOSE -> false;
		};
	}
}