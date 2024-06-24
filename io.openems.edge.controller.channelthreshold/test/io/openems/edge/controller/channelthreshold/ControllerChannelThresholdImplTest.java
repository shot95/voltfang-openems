package io.openems.edge.controller.channelthreshold;

import io.openems.common.channel.AccessMode;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.test.AbstractComponentTest;
import io.openems.common.test.TimeLeapClock;
import org.junit.Before;
import org.junit.Test;

import io.openems.common.types.ChannelAddress;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.controller.test.ControllerTest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class ControllerChannelThresholdImplTest {

	private static final String CTRL_ID = "ctrl0";
	private static final ChannelAddress IO0_INPUT = new ChannelAddress("io0", "Input0");
	private static final ChannelAddress IO0_OUTPUT = new ChannelAddress("io0", "Output0");

	// oEMS Start

	private DummyComponentManager cpm;

	private MyConfig config;


	@Before
	public void setup() {
		this.config = this.createDefaultConfig().build();
	}
	
	private MyConfig.Builder createDefaultConfig() {
		return MyConfig.create() //
				.setId(CTRL_ID) //
				.setInputChannelAddress(IO0_INPUT.toString()) //
				.setOutputChannelAddress(IO0_OUTPUT.toString()) //
				.setLowThreshold(30) //
				.setHighThreshold(90) //
				.setInvert(false) //
				.setHysteresis(5);
	}

	@Test
	public void initialTest() throws Exception {
		var test = new ControllerTest(new ControllerChannelThresholdImpl());
		this.setBaseReferences(test);
		test.activate(this.config); //
	}

	@Test
	public void test() throws Exception {
		var test = new ControllerTest(new ControllerChannelThresholdImpl());
		this.setBaseReferences(test);
		test.activate(this.config) //
				.next(new AbstractComponentTest.TestCase()//
						.input(IO0_INPUT, 50).timeleap((TimeLeapClock) this.cpm.getClock(), 1, ChronoUnit.SECONDS))//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 50).output(IO0_OUTPUT, true)
						)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 25).output(IO0_OUTPUT, null)
						)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 25).output(IO0_OUTPUT, false)
				)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 24).output(IO0_OUTPUT, false)
						)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 27).output(IO0_OUTPUT, false)
					)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 30).output(IO0_OUTPUT, null)
						)//
				.next(new AbstractComponentTest.TestCase().input(IO0_INPUT, 30).output(IO0_OUTPUT, true)
				)//
				.getSut().run();

	}

	private void setBaseReferences(ControllerTest test) throws Exception {
		this.cpm = new DummyComponentManager(new TimeLeapClock(Instant.ofEpochSecond(1577836800), ZoneOffset.UTC));
		test.addReference("componentManager", this.cpm);
		test.addComponent(new MyDummyOpenEmsComponent("io0"));

	}

	private static class MyDummyOpenEmsComponent extends AbstractOpenemsComponent {
		public MyDummyOpenEmsComponent(String id) {
			super(OpenemsComponent.ChannelId.values(), //
					ChannelId.values());
			this.channels().forEach(Channel::nextProcessImage);
			super.activate(null, id, "", true);
		}

		public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
			INPUT_0(Doc.of(OpenemsType.INTEGER)), //
			OUTPUT_0(Doc.of(OpenemsType.BOOLEAN).accessMode(AccessMode.READ_WRITE)) //
			;

			private final Doc doc;

			private ChannelId(Doc doc) {
				this.doc = doc;
			}

			@Override
			public Doc doc() {
				return this.doc;
			}
		}
	}
	// oEMS End
}
