package io.openems.edge.app.battery;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.function.ThrowingTriFunction;
import io.openems.common.oem.OpenemsEdgeOem;
import io.openems.common.session.Language;
import io.openems.common.types.EdgeConfig;
import io.openems.common.utils.EnumUtils;
import io.openems.common.utils.JsonUtils;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.core.appmanager.AbstractOpenemsApp;
import io.openems.edge.core.appmanager.AppAssistant;
import io.openems.edge.core.appmanager.AppConfiguration;
import io.openems.edge.core.appmanager.AppDescriptor;
import io.openems.edge.core.appmanager.ComponentUtil;
import io.openems.edge.core.appmanager.ConfigurationTarget;
import io.openems.edge.core.appmanager.DefaultEnum;
import io.openems.edge.core.appmanager.Nameable;
import io.openems.edge.core.appmanager.OpenemsApp;
import io.openems.edge.core.appmanager.OpenemsAppCardinality;
import io.openems.edge.core.appmanager.TranslationUtil;
import io.openems.edge.core.appmanager.dependency.Tasks;
import io.openems.edge.core.appmanager.formly.JsonFormlyUtil;
import io.openems.edge.core.appmanager.formly.enums.InputType;
import io.openems.edge.core.appmanager.formly.enums.Validation;

/**
 * Describes a app for a Voltfang Akasol battery.
 *
 * <pre>
 * {
 * "appId":"App.Battery.Voltfang",
 * "alias":"Batterie Voltfang Akasol",
 * "instanceId": UUID,
 * "image": base64,
 * "properties":{
 * "BATTERY_ID": "battery0",
 * "CAN_ID": "can0",
 * },
 * "appDescriptor": {
 * "websiteUrl": {@link AppDescriptor#getWebsiteUrl()}
 * }
 * }
 * </pre>
 */
@Component(name = "App.Battery.Voltfang")
public class VoltfangBattery extends AbstractBatteryApp<io.openems.edge.app.battery.VoltfangBattery.Property>
		implements OpenemsApp {

	public enum Property implements DefaultEnum, Nameable {
		// Component-IDs
		BATTERY_ID("battery0"), //
		INVERTER_ID("batteryInverter0"), //
		ESS_ID("ess0"), //
		// Properties
		ALIAS("Batterie Voltfang Akasol"), //
		BMMS("2"), //
		KUNBUS_VERSION("CONNECT_S"), //
		INVERTER_IP_ADRESS("192.168.101.142") //
		;

		private final String defaultValue;

		private Property(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		@Override
		public String getDefaultValue() {
			return this.defaultValue;
		}

	}

	private static enum KunbusVersion {
		CONNECT_S("Connect S"), //
		CONNECT_PLUS("Connect +"); //

		private final String name;

		private KunbusVersion(String name) {
			this.name = name;
		}

		private static List<String> getOptions() {
			return Stream.of(KunbusVersion.values()) //
					.map(v -> v.name).collect(Collectors.toList());
		}
	}

	@Activate
	public VoltfangBattery(@Reference ComponentManager componentManager, ComponentContext componentContext,
			@Reference ConfigurationAdmin cm, @Reference ComponentUtil componentUtil) {
		super(componentManager, componentContext, cm, componentUtil);
	}

	@Override
	protected ThrowingTriFunction<ConfigurationTarget, EnumMap<Property, JsonElement>, Language, AppConfiguration, OpenemsNamedException> appConfigurationFactory() {
		return (t, p, l) -> {
			// values the user enters
			var alias = this.getValueOrDefault(p, Property.ALIAS, this.getName(l));
			var bmms = EnumUtils.getAsInt(p, Property.BMMS);
			var version = this.getValueOrDefault(p, Property.KUNBUS_VERSION);
			var inverterIp = this.getValueOrDefault(p, Property.INVERTER_IP_ADRESS);
			// values which are being auto generated by the appmanager
			var capacity = bmms * 16500;

			var batteryId = this.getId(t, p, Property.BATTERY_ID);
			var canId = "can" + batteryId;
			var inverterId = this.getId(t, p, Property.INVERTER_ID);
			var modbusId = "modbus" + inverterId;
			var essId = this.getId(t, p, Property.ESS_ID);
			var linuxVersion = version.equals("Connect +") ? "4" : "5";

			var components = Lists
					.newArrayList(
							new EdgeConfig.Component(batteryId, alias, "battery.voltfang.akasol",
									JsonUtils.buildJsonObject() //
											.addProperty("can.id", canId) //
											.addProperty("bmm", bmms) //
											.addProperty("capacity", capacity) //
											.build()), //
							new EdgeConfig.Component(canId, "Bridge " + alias, "Bridge.CAN.linuxv" + linuxVersion, //
									JsonUtils.buildJsonObject() //
											// .addProperty("selected.hardware", "KUNBUS_REV_PI_CAN_EXTENSION") //
											.build()), //
							new EdgeConfig.Component(modbusId, "Bridge KACO Battery Inverter", "Bridge.Modbus.Tcp", //
									JsonUtils.buildJsonObject() //
											.addProperty("ip", inverterIp) //
											.build()), //
							new EdgeConfig.Component(inverterId, "KACO Battery Inverter",
									"Battery-Inverter.Kaco.BlueplanetGridsave", //
									JsonUtils.buildJsonObject() //
											.addProperty("modbus.id", modbusId) //
											.build()), //
							new EdgeConfig.Component(essId, "ESS Voltfang", "Ess.Generic.ManagedSymmetric", //
									JsonUtils.buildJsonObject() //
											.addProperty("enabled", true) //
											.addProperty("startStop", "START") //
											.addProperty("batteryInverter.id", inverterId) //
											.addProperty("battery.id", batteryId) //
											.build()), //
							new EdgeConfig.Component("ctrlLimitActivePower0", "Leistungslimitierung",
									"Controller.Symmetric.LimitActivePower", JsonUtils.buildJsonObject() //
											.addProperty("ess.id", essId) //
											.addProperty("maxChargePower", 50000) //
											.addProperty("maxDischargePower", 50000) //
											.build()), //
							new EdgeConfig.Component("ctrlLimitTotalDischarge0", "Entladelimit",
									"Controller.Ess.LimitTotalDischarge", //
									JsonUtils.buildJsonObject() //
											.addProperty("ess.id", essId) //
											.addProperty("minSoc", 1) //
											.addProperty("forceChargeSoc", 0) //
											.addProperty("forceChargePower", 500) //
											.build()), //
							new EdgeConfig.Component("ctrlFixActivePower0", "Zwangsbeladung",
									"Controller.Ess.FixActivePower", JsonUtils.buildJsonObject() //
											.addProperty("ess.id", essId) //
											.addProperty("power", 0) //
											.build()) //
			);

			// final var executionOrder = Lists.newArrayList("ctrlLimitActivePower0",
			// "ctrlLimitTotalDischarge0",
			// "ctrlFixActivePower0");

			return AppConfiguration.create() //
					.addTask(Tasks.component(components)) //
					// .addTask(Tasks.scheduler(executionOrder)) //
					.build();

		};
	}

	@Override
	public AppAssistant getAppAssistant(Language language) {
		var bundle = AbstractOpenemsApp.getTranslationBundle(language);
		return AppAssistant.create(this.getName(language)) //
				.fields(JsonUtils.buildJsonArray() //
						.add(JsonFormlyUtil.buildInput(Property.BMMS) //
								.setLabel(TranslationUtil.getTranslation(bundle, this.getAppId() + ".Bmms")) //
								.setDescription(
										TranslationUtil.getTranslation(bundle, this.getAppId() + ".bmms.description")) //
								.setInputType(InputType.NUMBER) //
								.setDefaultValue(Property.BMMS.getDefaultValue()) //
								.setMin(1) //
								.setMax(15) //
								.isRequired(true) //
								.build()) //
						.add(JsonFormlyUtil.buildSelect(Property.KUNBUS_VERSION) //
								.setLabel(TranslationUtil.getTranslation(bundle, this.getAppId() + ".KunbusVersion")) //
								.setDescription(TranslationUtil.getTranslation(bundle,
										this.getAppId() + ".KunbusVersion.description")) //
								.setOptions(KunbusVersion.getOptions()) //
								.build()) //
						.add(JsonFormlyUtil.buildInput(Property.INVERTER_IP_ADRESS) //
								.setLabel(TranslationUtil.getTranslation(bundle, "ipAddress")) //
								.setDescription(TranslationUtil.getTranslation(bundle, "App.Battery.ip.description")) //
								.isRequired(true) //
								.setDefaultValue("192.168.101.142") //
								.setValidation(Validation.IP) //
								.build())
						.build()) //
				.build();
	}

	@Override
	public AppDescriptor getAppDescriptor(OpenemsEdgeOem oem) {
		return AppDescriptor.create() //
				.setWebsiteUrl(oem.getAppWebsiteUrl(this.getAppId())) //
				.build();
	}

	@Override
	protected Class<Property> getPropertyClass() {
		return Property.class;
	}

	@Override
	public OpenemsAppCardinality getCardinality() {
		return OpenemsAppCardinality.MULTIPLE;
	}

}