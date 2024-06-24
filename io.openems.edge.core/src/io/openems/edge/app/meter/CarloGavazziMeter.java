package io.openems.edge.app.meter;

import static io.openems.edge.app.common.props.CommonProps.alias;

import java.util.Map;
import java.util.function.Function;

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
import io.openems.common.utils.JsonUtils;
import io.openems.edge.app.enums.MeterType;
import io.openems.edge.app.meter.CarloGavazziMeter.Property;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.core.appmanager.AbstractOpenemsApp;
import io.openems.edge.core.appmanager.AbstractOpenemsAppWithProps;
import io.openems.edge.core.appmanager.AppConfiguration;
import io.openems.edge.core.appmanager.AppDef;
import io.openems.edge.core.appmanager.AppDescriptor;
import io.openems.edge.core.appmanager.ComponentUtil;
import io.openems.edge.core.appmanager.ConfigurationTarget;
import io.openems.edge.core.appmanager.Nameable;
import io.openems.edge.core.appmanager.OpenemsApp;
import io.openems.edge.core.appmanager.OpenemsAppCardinality;
import io.openems.edge.core.appmanager.OpenemsAppCategory;
import io.openems.edge.core.appmanager.Type;
import io.openems.edge.core.appmanager.Type.Parameter;
import io.openems.edge.core.appmanager.Type.Parameter.BundleParameter;
import io.openems.edge.core.appmanager.dependency.Tasks;

/**
 * Describes a app for a Carlo Gavazzi meter.
 *
 * <pre>
  {
    "appId":"App.Meter.CarloGavazzi",
    "alias":"Carlo Gavazzi Zähler",
    "instanceId": UUID,
    "image": base64,
    "properties":{
    	"METER_ID": "meter1",
    	"TYPE": "PRODUCTION",
    	"MODBUS_UNIT_ID": 6
    },
    "appDescriptor": {
    	"websiteUrl": {@link AppDescriptor#getWebsiteUrl()}
    }
  }
 * </pre>
 */
// oEMS - Totally Different - AbstractOpenEMSAppWithProps vs AbstractMeterApp
@Component(name = "App.Meter.CarloGavazzi")
public class CarloGavazziMeter extends AbstractOpenemsAppWithProps<CarloGavazziMeter, Property, BundleParameter>
		 implements OpenemsApp {

	public enum Property implements Type<Property, CarloGavazziMeter, Parameter.BundleParameter>, Nameable {
		// Component-IDs
		METER_ID(AppDef.componentId("meter0")), //
		// Properties
		ALIAS(alias()),
		TYPE(AppDef.copyOfGeneric(MeterProps.type(), def -> def //
				.setRequired(true))), //
		MODBUS_ID(AppDef.componentId("modbus0")), //
		MODBUS_UNIT_ID(MeterProps.modbusUnitId() //
				.setRequired(true) //
				.setDefaultValue(1) //
				.setAutoGenerateField(false)), //
		;

		private final AppDef<? super CarloGavazziMeter, ? super Property, ? super BundleParameter> def;

		private Property(AppDef<? super CarloGavazziMeter, ? super Property, ? super BundleParameter> def) {
			this.def = def;
		}

		@Override
		public Type<Property, CarloGavazziMeter, BundleParameter> self() {
			return this;
		}

		@Override
		public AppDef<? super CarloGavazziMeter, ? super Property, ? super BundleParameter> def() {
			return this.def;
		}

		@Override
		public Function<GetParameterValues<CarloGavazziMeter>, BundleParameter> getParamter() {
			return Parameter.functionOf(AbstractOpenemsApp::getTranslationBundle);
		}
	}

	@Activate
	public CarloGavazziMeter(@Reference ComponentManager componentManager, ComponentContext componentContext,
					  @Reference ConfigurationAdmin cm, @Reference ComponentUtil componentUtil) {
		super(componentManager, componentContext, cm, componentUtil);
	}

	@Override
	protected ThrowingTriFunction<ConfigurationTarget, Map<Property, JsonElement>, Language, AppConfiguration, OpenemsNamedException> appPropertyConfigurationFactory() {
		return (t, p, l) -> {

			var meterId = this.getId(t, p, Property.METER_ID, "meter0");
			var modbusId = this.getId(t, p, Property.MODBUS_ID, "modbus0");
			var alias = this.getValueOrDefault(p, Property.ALIAS, this.getName(l));

			var type = this.getEnum(p, MeterType.class, Property.TYPE);

			var modbusUnitId = this.getInt(p, Property.MODBUS_UNIT_ID);

			var components = Lists.newArrayList(//
					new EdgeConfig.Component(meterId, alias, "Meter.CarloGavazzi.EM300", //
							JsonUtils.buildJsonObject() //
									.addProperty("modbus.id", modbusId) //
									.addProperty("modbusUnitId", modbusUnitId) //
									.addProperty("type", type) //
									.build()) //
			);

			return AppConfiguration.create() //
					.addTask(Tasks.component(components)) //
					.build();
		};
	}

	@Override
	public AppDescriptor getAppDescriptor(OpenemsEdgeOem oem) {
		return AppDescriptor.create() //
				.setWebsiteUrl(oem.getAppWebsiteUrl(this.getAppId())) //
				.build();
	}

	@Override
	public OpenemsAppCardinality getCardinality() {
		return OpenemsAppCardinality.MULTIPLE;
	}

	@Override
	public OpenemsAppCategory[] getCategories() {
		return new OpenemsAppCategory[] { OpenemsAppCategory.METER };
	}

	@Override
	protected CarloGavazziMeter getApp() {
		return this;
	}

	@Override
	protected Property[] propertyValues() {
		return Property.values();
	}



}
