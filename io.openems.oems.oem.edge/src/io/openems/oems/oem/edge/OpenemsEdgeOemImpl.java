package io.openems.oems.oem.edge;

import java.util.Map;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.openems.common.OpenemsConstants;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.oem.DummyOpenemsEdgeOem;
import io.openems.common.oem.OpenemsEdgeOem;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.oem.OemsEdgeOem;

@Designate(ocd = Config.class, factory = false)
@Component(//
		name = OpenemsEdgeOemImpl.SINGLETON_SERVICE_PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		immediate = true, //
		property = { //
				"enabled=true" //
		})
public class OpenemsEdgeOemImpl extends AbstractOpenemsComponent
		implements OpenemsComponent, OemsEdgeOem, OpenemsEdgeOem {

	// original package
	private static final String OEMS_EDGE_PACKAGE_PREFIX = "oems-edge-*";
	private static final String APP_BASE_URL = "https://docs.oems.energy/oEMS/latest/apps";
	private static final String OEMS_EDGE_DOWNLOAD_URL_LATEST_VERSION = //
			"https://oems.energy/oems-edge.update/oems-latest.version?id=";
	private static final String OEMS_EDGE_DOWNLOAD_URL_UPDATE_SCRIPT = //
			"https://oems.energy/oems-edge.update/update-oems-edge.sh";

	private Config config;
	private final Logger log = LoggerFactory.getLogger(OpenemsEdgeOemImpl.class);

	@Reference
	private ConfigurationAdmin cm;

	public OpenemsEdgeOemImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				OemsEdgeOem.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, SINGLETON_COMPONENT_ID, SINGLETON_SERVICE_PID, true);
		this.config = config;
		if (OpenemsComponent.validateSingleton(this.cm, SINGLETON_SERVICE_PID, SINGLETON_COMPONENT_ID)) {
			return;
		}

		if (DEFAULT_EDGE_ID.compareTo(this.config.edgeId()) == 0) {
			this._setEdgeIdNotSet(true);
		}
		if (DEFAULT_SERIALNUMBER.compareTo(this.config.serialNumber()) == 0) {
			this._setSerialNumberNotSet(true);
		}
		this.logInfo(this.log,
				"oEMS Provider EdgeID: " + this.config.edgeId() + " SerialNumber: " + this.config.serialNumber());
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public String getManufacturer() {
		return "opernikus GmbH";
	}

	@Override
	public String getManufacturerModel() {
		return "oEMS";
	}

	@Override
	public String getManufacturerOptions() {
		return this.config.options();
	}

	@Override
	public String getManufacturerVersion() {
		return OpenemsConstants.VERSION.toString();
	}

	@Override
	public String getManufacturerSerialNumber() {
		return this.config.serialNumber();
	}

	@Override
	public String getManufacturerEmsSerialNumber() {
		return this.config.edgeId();
	}

	@Override
	public String getBackendApiUrl() {
		return "wss://oems.energy:443/ems/openems-backend";
	}

	@Override
	public String getInfluxdbTag() {
		return "edge";
	}

	@Override
	public SystemUpdateParams getSystemUpdateParams() {
		return new SystemUpdateParams(//
				// oems-edge allows to filter for all oems-edge versions like oems-edge-2024.03
				// or oems-edge-2024.04

				OEMS_EDGE_PACKAGE_PREFIX, //
				OEMS_EDGE_DOWNLOAD_URL_LATEST_VERSION + this.getManufacturerEmsSerialNumber(), //
				OEMS_EDGE_DOWNLOAD_URL_UPDATE_SCRIPT, //
				"-s -o" // Options -s = system update, -o=oems-edge Update   
		);
	}

	@Override
	public String getKacoBlueplanetHybrid10IdentKey() {
		return null;
	}

	private final Map<String, String> appToWebsiteUrl = new ImmutableMap.Builder<String, String>() //
			.put("App.Api.ModbusTcp.ReadOnly", APP_BASE_URL + "/fems-app-modbus-tcp-lesend/") //
			.put("App.Api.ModbusTcp.ReadWrite", APP_BASE_URL + "/fems-app-modbus-tcp-schreibzugriff/") //
			.put("App.Api.Mqtt", APP_BASE_URL + "/api/mqtt/app.html") // oEMS Mqtt App of OpenEMS was missing
			.put("App.Api.RestJson.ReadOnly", APP_BASE_URL + "/fems-app-rest-json-lesend/") //
			.put("App.Api.RestJson.ReadWrite", APP_BASE_URL + "/fems-app-rest-json-schreibzugriff/") //
			// oEMS Api Start
			.put("App.Api.Lmn.Bridge", APP_BASE_URL + "/api/lmn/app.html") //
			.put("App.Api.Mbus.Bridge", APP_BASE_URL + "/api/mbus/app.html") //
			.put("App.Api.Modbus.Bridge.Serial", APP_BASE_URL + "/api/modbus/serial/app.html") //
			.put("App.Api.WMbus.Bridge", APP_BASE_URL + "/api/wmbus/app.html") //
			// oEMS Api End
			// oEMS Battery Start
			.put("App.Battery.Voltfang", APP_BASE_URL + "/battery/voltfang/app.html") //
			// oEMS Battery End
			// oEMS Bsp Start
			.put("App.Bsp.Leaflet.Core.Local", "") // TODO
			.put("App.Bsp.Leaflet.Core.Remote", "") // TODO
			.put("App.Bsp.Leaflet.Relay", "") // TODO
			.put("App.Bsp.Leaflet.Temperature", "") // TODO
			// oEMS Bsp End
			.put("App.Ess.FixActivePower", "") //
			.put("App.Ess.PowerPlantController", "") //
			.put("App.Ess.PrepareBatteryExtension", "") //
			// oEMS Evcs Start
			.put("App.Evcs.Alfen.Basic", APP_BASE_URL + "/e-mobility/evcs/alfen/app.html") //
			.put("App.Evcs.Compleo.Basic", APP_BASE_URL + "/e-mobility/evcs/compleo/app.html") //
			.put("App.Evcs.Dezony", "") // TODO
			.put("App.Evcs.Cluster", APP_BASE_URL + "/fems-app-multiladepunkt-management/") //
			.put("App.Evcs.Cluster.ChargeManagement", APP_BASE_URL + "/e-mobility/charge-management/cluster/app.html") //
			.put("App.Evcs.Cluster.Limiter", APP_BASE_URL + "/e-mobility/charge-management/limiter/app.html") //
			.put("App.Evcs.Keba.Basic", APP_BASE_URL + "/e-mobility/evcs/keba/app.html") //
			.put("App.Evcs.Mennekes.Basic", APP_BASE_URL + "/e-mobility/evcs/mennekes/app.html") //
			.put("App.Evcs.Simulator", APP_BASE_URL + "/e-mobility/evcs/simulator/app.html") //
			// oEMS Evcs End
			.put("App.Evcs.Alpitronic", APP_BASE_URL + "/e-mobility/evcs/alpitronic/app.html") //
			.put("App.Evcs.HardyBarth", APP_BASE_URL + "/fems-app-echarge-hardy-barth-ladestation/") //
			.put("App.Evcs.IesKeywatt", APP_BASE_URL + "/fems-app-ies-keywatt-ladestation/") //
			.put("App.Evcs.Keba", APP_BASE_URL + "/e-mobility/evcs/keba/app.html") //
			.put("App.Evcs.Webasto.Next", "") //
			.put("App.Evcs.Webasto.Unite", "") //
			.put("App.Hardware.KMtronic8Channel", APP_BASE_URL + "/io/relais") //
			.put("App.Heat.HeatPump", APP_BASE_URL + "/fems-app-sg-ready-waermepumpe/") //
			.put("App.Heat.CHP", APP_BASE_URL + "/fems-app-blockheizkraftwerk-bhkw/") //
			.put("App.Heat.HeatingElement", APP_BASE_URL + "/fems-app-heizstab/") //
			// oEMS integrated Systems Start
			.put("App.IntegratedSystem.SMA.SmartEnergy", APP_BASE_URL + "/integratedsystems/sma/smart-energy/app.html")
			.put("App.FENECON.Industrial.S.ISK010", "") //
			.put("App.FENECON.Industrial.S.ISK011", "") //
			.put("App.FENECON.Industrial.S.ISK110", "") //
			.put("App.FENECON.Home", "") //
			.put("App.FENECON.Home.20", "") //
			.put("App.FENECON.Home.30", "") //
			// oEMS integrated Systems End
			.put("App.LoadControl.ManualRelayControl", APP_BASE_URL + "/fems-app-manuelle-relaissteuerung/") //
			.put("App.LoadControl.ThresholdControl", APP_BASE_URL + "/fems-app-schwellwert-steuerung/") //
			// oEMS Meter Start
			.put("App.Meter.BControl", APP_BASE_URL + "/meter/b-control/app.html") //
			.put("App.Meter.Controlin", APP_BASE_URL + "/meter/controlin/app.html") //
			.put("App.Meter.ReadingHeadD0", APP_BASE_URL + "/meter/readinghead/d0/app.html") //
			.put("App.Meter.Eastron", APP_BASE_URL + "/meter/eastron/app.html") //
			.put("App.Meter.Gmc", APP_BASE_URL + "/meter/gossen-metrawatt/app.html") //
			.put("App.Meter.Kdk", APP_BASE_URL + "/fems-app-kdk-zaehler/app.html") //
			.put("App.Meter.Lmn.Consumption", APP_BASE_URL + "/api/lmn/app.html") //
			.put("App.Meter.Mbus.Gas.Consolinno", APP_BASE_URL + "/meter/consolinno/mbus/gas/app.html") //
			.put("App.Meter.Mbus.Heat.Consolinno", APP_BASE_URL + "/meter/consolinno/mbus/heat/app.html") //
			.put("App.Meter.Mbus.Water.Consolinno", APP_BASE_URL + "/meter/consolinno/mbus/water/app.html") //
			.put("App.Meter.Microcare.Sdm630", APP_BASE_URL + "/meter/eastron/app.html") //
			.put("App.Meter.Siemens", APP_BASE_URL + "/meter/siemens/app.html") //
			.put("App.Meter.WMbus.Water.Consolinno", APP_BASE_URL + "/api/wmbus/app.html") //
			// oEMS Meter End
			.put("App.Meter.Socomec", APP_BASE_URL + "/fems-app-socomec-zaehler/") //
			.put("App.Meter.CarloGavazzi", APP_BASE_URL + "/fems-app-carlo-gavazzi-zaehler/app.html") //
			.put("App.Meter.Janitza", APP_BASE_URL + "/meter/janitza/app.html") //
			.put("App.PeakShaving.PeakShaving", "") //
			.put("App.PeakShaving.PhaseAccuratePeakShaving", "") //
			.put("App.PvInverter.Fronius", APP_BASE_URL + "/pv-inverter/fronius/app.html") //
			.put("App.PvInverter.Kaco", APP_BASE_URL + "/pv-inverter/kaco/app.html") //
			.put("App.PvInverter.Kostal", APP_BASE_URL + "/pv-inverter/kostal/app.html") //
			.put("App.PvInverter.Sma", APP_BASE_URL + "/pv-inverter/sma/app.html") //
			.put("App.PvInverter.SolarEdge", APP_BASE_URL + "/pv-inverter/solaredge/app.html") //
			.put("App.PvSelfConsumption.GridOptimizedCharge", APP_BASE_URL + "/fems-app-netzdienliche-beladung/") //
			.put("App.PvSelfConsumption.SelfConsumptionOptimization",
					APP_BASE_URL + "/fems-app-eigenverbrauchsoptimierung/") //
			.put("App.TimeOfUseTariff.Awattar", APP_BASE_URL + "fems-app-awattar-hourly/") //
			.put("App.TimeOfUseTariff.ENTSO-E", "") //
			.put("App.TimeOfUseTariff.Stromdao", APP_BASE_URL + "fems-app-stromdao-corrently/") //
			.put("App.TimeOfUseTariff.Tibber", APP_BASE_URL + "/fems-app-tibber/") //
			.build();

	// NOTE: this will certainly get refactored in future, but it's a good start to
	// simplify creation of OpenEMS distributions.
	@Override
	public String getAppWebsiteUrl(String appId) {
		return this.appToWebsiteUrl.get(appId);
	}

	/**
	 * Helper method for JUnit tests. Tests if the given {@link OpenemsEdgeOem}
	 * provides the same Website-URLs as {@link DummyOpenemsEdgeOem} - (i.e. all are
	 * not-null. See {@link #getAppWebsiteUrl(String)}
	 * 
	 * @param oem the {@link OpenemsEdgeOem}
	 */
	public static void assertAllWebsiteUrlsSet(OpenemsEdgeOem oem) throws OpenemsException {
		var oemImpl = new OpenemsEdgeOemImpl();
		var missing = oemImpl.appToWebsiteUrl.keySet().stream() //
				.filter(appId -> oem.getAppWebsiteUrl(appId) == null) //
				.toList();
		if (!missing.isEmpty()) {
			throw new OpenemsException("Missing Website-URLs in Edge-OEM for [" + String.join(", ", missing) + "]");
		}
	}
}
