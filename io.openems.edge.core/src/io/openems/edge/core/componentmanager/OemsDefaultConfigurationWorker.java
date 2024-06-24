package io.openems.edge.core.componentmanager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.jsonrpc.request.UpdateComponentConfigRequest.Property;
import io.openems.edge.common.oem.OemsEdgeOem;
import io.openems.edge.core.componentmanager.ConfigurationUtils.Config;
import io.openems.edge.core.host.HostImpl;

/**
 * This Worker checks if certain OpenEMS-Components are configured and - if not
 * - configures them. It is used to make sure a set of standard components are
 * always activated by default on a deployed energy management system.
 */
public class OemsDefaultConfigurationWorker extends ComponentManagerWorker {

	/**
	 * Time to wait before doing the check. This allows the system to completely
	 * boot and read configurations.
	 */
	private static final int INITIAL_WAIT_TIME = 7_500; // in ms

	private final Logger log = LoggerFactory.getLogger(OemsDefaultConfigurationWorker.class);
	private final ConfigurationUtils configUtils;
	private final OemsMigration oemsMigration;

	public OemsDefaultConfigurationWorker(ComponentManagerImpl parent) {
		super(parent);
		this.configUtils = new ConfigurationUtils(this.parent, this.log);
		this.oemsMigration = new OemsMigration(this.configUtils);
	}

	/**
	 * Creates all default configurations.
	 *
	 * @param existingConfigs already existing {@link Config}s
	 * @return true on error, false if default configuration was successfully
	 *         applied
	 */
	private boolean createDefaultConfigurations(List<Config> existingConfigs) {
		final var defaultConfigurationFailed = new AtomicBoolean(false);

		/*
		 * Create Timedata.Rrd4j
		 */
		if (existingConfigs.stream().noneMatch(c -> //
		// Check if either "Timedata.Rrd4j" or
		// "Timedata.InfluxDB" exist
		"Timedata.Rrd4j".equals(c.factoryPid) || "Timedata.InfluxDB".equals(c.factoryPid))) {
			// if not -> create configuration for "Timedata.Rrd4j"
			this.configUtils.createConfiguration(defaultConfigurationFailed, "Timedata.Rrd4j", Arrays.asList(//
					new Property("id", "rrd4j0"), //
					new Property("alias", ""), //
					new Property("enabled", true), //
					new Property("noOfCycles", 60) //
			));
		}

		/*
		 * Create OEMProvider
		 */
		if (existingConfigs.stream().noneMatch(c -> //
		c.componentId.isPresent() && OemsEdgeOem.SINGLETON_COMPONENT_ID.equals(c.componentId.get()))) {
			// if not exists -> create configuration for "OpenEMS.OemProvider"
			String hostname;
			try {
				hostname = "edge-" + Integer.parseInt(HostImpl.execReadEdgeId());
			} catch (Exception e) {
				hostname = OemsEdgeOem.DEFAULT_EDGE_ID;
			}
			String serialNumber;
			try {
				serialNumber = HostImpl.execReadSerialnumber();
			} catch (Exception e) {
				serialNumber = OemsEdgeOem.DEFAULT_SERIALNUMBER;
			}
			this.configUtils.createConfiguration(defaultConfigurationFailed, OemsEdgeOem.SINGLETON_SERVICE_PID,
					Arrays.asList(//
							new Property("alias", OemsEdgeOem.SINGLETON_SERVICE_PID), //
							new Property("edgeId", hostname), //
							new Property("options", ""), //
							new Property("serialNumber", serialNumber) //
					));
		}

		/*
		 * Delete configuration for deprecated Controller.Api.Rest
		 */
		existingConfigs.stream().filter(c -> //
		c.componentId.isPresent() && "Controller.Api.Rest".equals(c.factoryPid)).forEach(c -> {
			this.configUtils.deleteConfiguration(defaultConfigurationFailed, c.componentId.get());
		});

		// migrate configuration.
		this.oemsMigration.migrateConfigurations(existingConfigs, defaultConfigurationFailed);

		return defaultConfigurationFailed.get();
	}

	@Override
	protected void forever() {
		var existingConfigs = this.configUtils.readConfigs();

		boolean defaultConfigurationFailed;
		try {
			defaultConfigurationFailed = this.createDefaultConfigurations(existingConfigs);
		} catch (Exception e) {
			this.parent.logError(this.log, "Unable to create default configuration: " + e.getMessage());
			e.printStackTrace();
			defaultConfigurationFailed = true;
		}

		// Set DefaultConfigurationFailed channel value
		this.parent._setDefaultConfigurationFailed(defaultConfigurationFailed);

		// Execute this worker only once
		this.deactivate();
	}

	@Override
	protected int getCycleTime() {
		// initial cycle time
		return OemsDefaultConfigurationWorker.INITIAL_WAIT_TIME;
	}

}
