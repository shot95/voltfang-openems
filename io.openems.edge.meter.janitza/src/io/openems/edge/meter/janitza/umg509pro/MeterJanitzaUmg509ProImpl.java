package io.openems.edge.meter.janitza.umg509pro;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.INVERT_IF_TRUE;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.KEEP_POSITIVE;


import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.meter.api.MeterType;

/**
 * Implements the Janitza UMG 509 Pro power analyzer.
 *
 * <p>
 * <a href="https://www.janitza.de/files/download/manuals/current/UMG509-PRO/janitza-mal-umg509pro-en.pdf">Manual</a>
 */
@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Meter.Janitza.UMG509Pro", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterJanitzaUmg509ProImpl extends AbstractOpenemsModbusComponent
		implements MeterJanitzaUmg509Pro, ElectricityMeter, ModbusComponent, OpenemsComponent, ModbusSlave {

	@Reference
	private ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config;

	public MeterJanitzaUmg509ProImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				MeterJanitzaUmg509Pro.ChannelId.values() //
		);

		// Automatically calculate sum values from L1/L2/L3
		ElectricityMeter.calculateSumCurrentFromPhases(this);
		ElectricityMeter.calculateAverageVoltageFromPhases(this);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		this.config = config;
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public MeterType getMeterType() {
		return this.config.type();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
			
		var powerConverter = INVERT_IF_TRUE(this.config.invert());
		if (this.config.type() == MeterType.PRODUCTION) {
			powerConverter = ElementToChannelConverter.chain(powerConverter, KEEP_POSITIVE);
		}
		
		var modbusProtocol = new ModbusProtocol(this, //
				new FC3ReadRegistersTask(19000, Priority.HIGH, //
						m(ElectricityMeter.ChannelId.VOLTAGE_L1, new FloatDoublewordElement(19000), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L2, new FloatDoublewordElement(19002), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.VOLTAGE_L3, new FloatDoublewordElement(19004), SCALE_FACTOR_3), //
						new DummyRegisterElement(19006, 19011), //
						m(ElectricityMeter.ChannelId.CURRENT_L1, new FloatDoublewordElement(19012), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L2, new FloatDoublewordElement(19014), SCALE_FACTOR_3), //
						m(ElectricityMeter.ChannelId.CURRENT_L3, new FloatDoublewordElement(19016), SCALE_FACTOR_3), //
						new DummyRegisterElement(19018, 19019),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, new FloatDoublewordElement(19020), //
								powerConverter), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, new FloatDoublewordElement(19022), //
								powerConverter), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, new FloatDoublewordElement(19024), //
								powerConverter), //
						m(ElectricityMeter.ChannelId.ACTIVE_POWER, new FloatDoublewordElement(19026), //
								powerConverter), //
						new DummyRegisterElement(19028, 19035),
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L1, new FloatDoublewordElement(19036), //
								INVERT_IF_TRUE(this.config.invert())), //
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L2, new FloatDoublewordElement(19038), //
								INVERT_IF_TRUE(this.config.invert())), //
						m(ElectricityMeter.ChannelId.REACTIVE_POWER_L3, new FloatDoublewordElement(19040), //
								INVERT_IF_TRUE(this.config.invert())), //
						m(ElectricityMeter.ChannelId.REACTIVE_POWER, new FloatDoublewordElement(19042), //
								INVERT_IF_TRUE(this.config.invert()))),
				//
				new FC3ReadRegistersTask(19050, Priority.LOW, //
						m(ElectricityMeter.ChannelId.FREQUENCY, new FloatDoublewordElement(19050), SCALE_FACTOR_3)));

		if (this.config.invert()) {
			modbusProtocol.addTask(new FC3ReadRegistersTask(19068, Priority.LOW, //
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, new FloatDoublewordElement(19068)),
					new DummyRegisterElement(19070, 19075),
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, new FloatDoublewordElement(19076))));
		} else {
			modbusProtocol.addTask(new FC3ReadRegistersTask(19068, Priority.LOW, //
					m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, new FloatDoublewordElement(19068)),
					new DummyRegisterElement(19070, 19075),
					m(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, new FloatDoublewordElement(19076))));
		}

		return modbusProtocol;
	}

	@Override
	public String debugLog() {
		return "L:" + this.getActivePower().asString();
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable(//
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				ElectricityMeter.getModbusSlaveNatureTable(accessMode) //
		);
	}
}
