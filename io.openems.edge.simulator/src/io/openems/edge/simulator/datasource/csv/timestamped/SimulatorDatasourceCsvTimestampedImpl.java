package io.openems.edge.simulator.datasource.csv.timestamped;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.types.ChannelAddress;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.simulator.CsvUtils;
import io.openems.edge.simulator.DataContainer;
import io.openems.edge.simulator.datasource.api.AbstractCsvDatasource;
import io.openems.edge.simulator.datasource.api.SimulatorDatasource;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Simulator.Datasource.CSV.Timestamped", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE //
})
public class SimulatorDatasourceCsvTimestampedImpl extends AbstractCsvDatasource
		implements SimulatorDatasourceCsvTimestamped, SimulatorDatasource, OpenemsComponent, EventHandler {

	@Reference
	private ComponentManager componentManager;

	private Config config;

	private final List<LocalTime> timestamps = new ArrayList<>();
	
	private int currentIndex = -1;

	public SimulatorDatasourceCsvTimestampedImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				SimulatorDatasourceCsvTimestamped.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws NumberFormatException, IOException {
		this.config = config;
		super.activate(context, config.id(), config.alias(), config.enabled(), 0);
		this.createTimestampList();
		this.moveToCurrentTime();
	}
	
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE -> {
			if (this.currentIndex < this.timestamps.size() - 1) {
				this.moveToCurrentTime();			
			} else {
				this.rewindAtMidnight();
			}
		}
		}
	}

	private void createTimestampList() {
		while (this.data.hasNext()) {
			this.addTimestamp();
			this.data.nextRecord();
		}
		// add last timestamp
		this.addTimestamp();
		this.data.rewind();
	}
	
	private void addTimestamp() {
		Long timestamp = this.getValue(OpenemsType.LONG, new ChannelAddress(this.id(), "Time"));
		this.timestamps.add(LocalTime.from(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())));
	}
	
	private void moveToCurrentTime() {
		var now = LocalTime.now(this.componentManager.getClock());
		while ((this.currentIndex < this.timestamps.size() - 1) //
				&& this.timestamps.get(this.currentIndex + 1).isBefore(now)) {
			this.data.nextRecord();
			this.currentIndex++;
		}
	}
	
	private void rewindAtMidnight() {
		var now = LocalTime.now(this.componentManager.getClock());
		if (now.getHour() == 0) {
			this.data.rewind();
			this.currentIndex = -1;
			this.moveToCurrentTime();
		}
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	protected ComponentManager getComponentManager() {
		return this.componentManager;
	}

	@Override
	protected DataContainer getData() throws NumberFormatException, IOException {
		return CsvUtils.parseCsv(this.config.source(), this.config.format(), this.config.factor());
	}

}
