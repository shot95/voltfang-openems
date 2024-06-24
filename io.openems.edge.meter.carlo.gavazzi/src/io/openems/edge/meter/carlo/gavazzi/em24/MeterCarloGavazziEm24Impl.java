package io.openems.edge.meter.carlo.gavazzi.em24;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;

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

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.element.WordOrder;
import io.openems.edge.bridge.modbus.api.task.FC4ReadInputRegistersTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.meter.api.MeterType;
import io.openems.edge.meter.carlo.gavazzi.common.AbstractMeterCarloGavazziEmSeries;
import io.openems.edge.meter.carlo.gavazzi.common.MeterCarloGavazziEmSeries;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Meter.CarloGavazzi.EM24", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterCarloGavazziEm24Impl extends AbstractMeterCarloGavazziEmSeries
		implements MeterCarloGavazziEmSeries, ElectricityMeter, ModbusComponent, OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config;

	public MeterCarloGavazziEm24Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				MeterCarloGavazziEmSeries.ChannelId.values() //
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
		final var offset = 300000 + 1;
		/**
		 * See Modbus definition PDF-file in doc directory and
		 * https://www.galoz.co.il/wp-content/uploads/2014/11/EM341-Modbus.pdf
		 */

		final ElectricityMeter.ChannelId energyChannelA;
		final ElectricityMeter.ChannelId energyChannelB;
		if (this.config.invert()) {
			energyChannelA = ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY;
			energyChannelB = ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY;
		} else {
			energyChannelA = ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY;
			energyChannelB = ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY;
		}

		ModbusProtocol modbusProtocol = super.defineModbusProtocol(this.config.invert());

		modbusProtocol.addTask(new FC4ReadInputRegistersTask(300056 - offset, Priority.LOW, //
				// 300056 ~ Hz
				m(ElectricityMeter.ChannelId.FREQUENCY, new UnsignedWordElement(300056 - offset), SCALE_FACTOR_2)));
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(300063 - offset, Priority.LOW, //
				// 300063 ~ KWh(+) TOT
				m(energyChannelA, new SignedDoublewordElement(300063 - offset).wordOrder(WordOrder.LSWMSW),
						SCALE_FACTOR_2)));
		modbusProtocol.addTask(new FC4ReadInputRegistersTask(300093 - offset, Priority.LOW, //
				// 300093 ~ KWh(-) TOT
				m(energyChannelB, new SignedDoublewordElement(300093 - offset).wordOrder(WordOrder.LSWMSW),
						SCALE_FACTOR_2)));

		return modbusProtocol;
	}

}
