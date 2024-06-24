package io.openems.edge.meter.elgris;

import java.util.Map;

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
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.sunspec.DefaultSunSpecModel;
import io.openems.edge.bridge.modbus.sunspec.SunSpecModel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.sunspec.AbstractSunSpecMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Meter.Elgris", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
})
public class MeterElgrisImpl extends AbstractSunSpecMeter
		implements ElectricityMeter, ModbusComponent, ModbusSlave, OpenemsComponent {

	
	private static final int READ_FROM_MODBUS_BLOCK = 1;
	
	private static final Map<SunSpecModel, Priority> ACTIVE_MODELS = ImmutableMap.<SunSpecModel, Priority>builder()
			.put(DefaultSunSpecModel.S_1, Priority.LOW) //
			.put(DefaultSunSpecModel.S_201, Priority.LOW) //
			.put(DefaultSunSpecModel.S_202, Priority.LOW) //
			.put(DefaultSunSpecModel.S_203, Priority.LOW) //
			.put(DefaultSunSpecModel.S_204, Priority.LOW) //
			.build();

	
	private Logger log = LoggerFactory.getLogger(MeterElgrisImpl.class);

	@Reference
	private ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config;

	public MeterElgrisImpl() throws OpenemsException {
		super(//
				ACTIVE_MODELS, //
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id(), READ_FROM_MODBUS_BLOCK)) {
			return;
		}
		this.config = config;
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
	protected void onSunSpecInitializationCompleted() {
		this.logInfo(this.log, "SunSpec initialization finished. " + this.channels().size() + " Channels available.");

		/*
		 * SymmetricMeter
		 */
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.FREQUENCY, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.HZ, DefaultSunSpecModel.S203.HZ, DefaultSunSpecModel.S202.HZ,
				DefaultSunSpecModel.S201.HZ);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.ACTIVE_POWER, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.W, DefaultSunSpecModel.S203.W, DefaultSunSpecModel.S202.W,
				DefaultSunSpecModel.S201.W);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.REACTIVE_POWER, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.VAR, DefaultSunSpecModel.S203.VAR, DefaultSunSpecModel.S202.VAR,
				DefaultSunSpecModel.S201.VAR);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.VOLTAGE, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.PH_V, DefaultSunSpecModel.S203.PH_V, DefaultSunSpecModel.S202.PH_V,
				DefaultSunSpecModel.S201.PH_V, //
				DefaultSunSpecModel.S204.PH_VPH_A, DefaultSunSpecModel.S203.PH_VPH_A, DefaultSunSpecModel.S202.PH_VPH_A,
				DefaultSunSpecModel.S201.PH_VPH_A, //
				DefaultSunSpecModel.S204.PH_VPH_B, DefaultSunSpecModel.S203.PH_VPH_B, DefaultSunSpecModel.S202.PH_VPH_B,
				DefaultSunSpecModel.S201.PH_VPH_B, //
				DefaultSunSpecModel.S204.PH_VPH_C, DefaultSunSpecModel.S203.PH_VPH_C, DefaultSunSpecModel.S202.PH_VPH_C,
				DefaultSunSpecModel.S201.PH_VPH_C);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.CURRENT, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.A, DefaultSunSpecModel.S203.A, DefaultSunSpecModel.S202.A,
				DefaultSunSpecModel.S201.A);

		/*
		 * ElectricityMeter
		 */
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.ACTIVE_POWER_L1, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.WPH_A, DefaultSunSpecModel.S203.WPH_A, DefaultSunSpecModel.S202.WPH_A,
				DefaultSunSpecModel.S201.WPH_A);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.ACTIVE_POWER_L2, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.WPH_B, DefaultSunSpecModel.S203.WPH_B, DefaultSunSpecModel.S202.WPH_B,
				DefaultSunSpecModel.S201.WPH_B);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.ACTIVE_POWER_L3, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.WPH_C, DefaultSunSpecModel.S203.WPH_C, DefaultSunSpecModel.S202.WPH_C,
				DefaultSunSpecModel.S201.WPH_C);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.CURRENT_L1, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.APH_A, DefaultSunSpecModel.S203.APH_A, DefaultSunSpecModel.S202.APH_A,
				DefaultSunSpecModel.S201.APH_A);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.CURRENT_L2, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.APH_B, DefaultSunSpecModel.S203.APH_B, DefaultSunSpecModel.S202.APH_B,
				DefaultSunSpecModel.S201.APH_B);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.CURRENT_L3, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.APH_C, DefaultSunSpecModel.S203.APH_C, DefaultSunSpecModel.S202.APH_C,
				DefaultSunSpecModel.S201.APH_C);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.REACTIVE_POWER_L1, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.V_A_RPH_A, DefaultSunSpecModel.S203.V_A_RPH_A,
				DefaultSunSpecModel.S202.V_A_RPH_A, DefaultSunSpecModel.S201.V_A_RPH_A);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.REACTIVE_POWER_L2, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.V_A_RPH_B, DefaultSunSpecModel.S203.V_A_RPH_B,
				DefaultSunSpecModel.S202.V_A_RPH_B, DefaultSunSpecModel.S201.V_A_RPH_B);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.REACTIVE_POWER_L3, //
				ElementToChannelConverter.INVERT_IF_TRUE(!this.config.invert()), //
				DefaultSunSpecModel.S204.V_A_RPH_C, DefaultSunSpecModel.S203.V_A_RPH_C,
				DefaultSunSpecModel.S202.V_A_RPH_C, DefaultSunSpecModel.S201.V_A_RPH_C);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.VOLTAGE_L1, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.PH_VPH_A, DefaultSunSpecModel.S203.PH_VPH_A, DefaultSunSpecModel.S202.PH_VPH_A,
				DefaultSunSpecModel.S201.PH_VPH_A);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.VOLTAGE_L2, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.PH_VPH_B, DefaultSunSpecModel.S203.PH_VPH_B, DefaultSunSpecModel.S202.PH_VPH_B,
				DefaultSunSpecModel.S201.PH_VPH_B);
		this.mapFirstPointToChannel(//
				ElectricityMeter.ChannelId.VOLTAGE_L3, //
				ElementToChannelConverter.SCALE_FACTOR_3, //
				DefaultSunSpecModel.S204.PH_VPH_C, DefaultSunSpecModel.S203.PH_VPH_C, DefaultSunSpecModel.S202.PH_VPH_C,
				DefaultSunSpecModel.S201.PH_VPH_C);
		if (!this.config.invert()) {
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP, DefaultSunSpecModel.S203.TOT_WH_IMP,
					DefaultSunSpecModel.S202.TOT_WH_IMP, DefaultSunSpecModel.S201.TOT_WH_IMP);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP, DefaultSunSpecModel.S203.TOT_WH_EXP,
					DefaultSunSpecModel.S202.TOT_WH_EXP, DefaultSunSpecModel.S201.TOT_WH_EXP);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L1, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_A, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_A,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_A, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_A);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L2, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_B, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_B,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_B, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_B);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L3, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_C, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_C,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_C, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_C);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L1, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_A, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_A,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_A, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_A);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L2, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_B, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_B,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_B, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_B);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L3, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_C, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_C,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_C, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_C);
		} else {
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP, DefaultSunSpecModel.S203.TOT_WH_EXP,
					DefaultSunSpecModel.S202.TOT_WH_EXP, DefaultSunSpecModel.S201.TOT_WH_EXP);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP, DefaultSunSpecModel.S203.TOT_WH_IMP,
					DefaultSunSpecModel.S202.TOT_WH_IMP, DefaultSunSpecModel.S201.TOT_WH_IMP);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L1, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_A, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_A,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_A, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_A);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L2, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_B, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_B,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_B, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_B);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L3, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_EXP_PH_C, DefaultSunSpecModel.S203.TOT_WH_EXP_PH_C,
					DefaultSunSpecModel.S202.TOT_WH_EXP_PH_C, DefaultSunSpecModel.S201.TOT_WH_EXP_PH_C);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L1, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_A, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_A,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_A, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_A);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L2, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_B, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_B,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_B, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_B);
			this.mapFirstPointToChannel(//
					ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY_L3, //
					ElementToChannelConverter.DIRECT_1_TO_1, //
					DefaultSunSpecModel.S204.TOT_WH_IMP_PH_C, DefaultSunSpecModel.S203.TOT_WH_IMP_PH_C,
					DefaultSunSpecModel.S202.TOT_WH_IMP_PH_C, DefaultSunSpecModel.S201.TOT_WH_IMP_PH_C);
		}
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable(//
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				ElectricityMeter.getModbusSlaveNatureTable(accessMode) //
		);
	}

}