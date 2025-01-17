package io.openems.edge.meter.microcare.sdm630;

import org.junit.Test;

import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.meter.api.MeterType;

public class MeterMicrocareSdm630ImplTest {

	private static final String METER_ID = "meter0";
	private static final String MODBUS_ID = "modbus0";

	@Test
	public void test() throws Exception {
		new ComponentTest(new MeterMicrocareSdm630Impl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("setModbus", new DummyModbusBridge(MODBUS_ID)) //
				.activate(MyConfig.create() //
						.setId(METER_ID) //
						.setModbusId(MODBUS_ID) //
						.setType(MeterType.GRID) //
						.setReducedReading(false) // oEMS
						.build()) //
		;
	}

	// oEMS Start
	@Test
	public void testReducedReading() throws Exception {
		new ComponentTest(new MeterMicrocareSdm630Impl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("setModbus", new DummyModbusBridge(MODBUS_ID)) //
				.activate(MyConfig.create() //
						.setId(METER_ID) //
						.setModbusId(MODBUS_ID) //
						.setType(MeterType.GRID) //
						.setReducedReading(true) //
						.build()) //
		;
	}
	// oEMS End
}
