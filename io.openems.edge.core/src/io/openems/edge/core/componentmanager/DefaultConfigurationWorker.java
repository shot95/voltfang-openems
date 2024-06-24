package io.openems.edge.core.componentmanager;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.edge.core.componentmanager.ConfigurationUtils.Config;

/**
 * This Worker checks if certain OpenEMS-Components are configured and - if not
 * - configures them. It is used to make sure a set of standard components are
 * always activated by default on a deployed energy management system.
 *
 * <p>
 * Example 1: Add JSON/REST-Api Controller by default:
 *
 * <pre>
 * if (existingConfigs.stream().noneMatch(c -> //
 * // Check if either "Controller.Api.Rest.ReadOnly" or
 * // "Controller.Api.Rest.ReadWrite" exist
 * "Controller.Api.Rest.ReadOnly".equals(c.factoryPid) || "Controller.Api.Rest.ReadWrite".equals(c.factoryPid))) {
 * 	// if not -> create configuration for "Controller.Api.Rest.ReadOnly"
 * 	this.createConfiguration(defaultConfigurationFailed, "Controller.Api.Rest.ReadOnly", Arrays.asList(//
 * 			new Property("id", "ctrlApiRest0"), //
 * 			new Property("alias", ""), //
 * 			new Property("enabled", true), //
 * 			new Property("port", 8084), //
 * 			new Property("debugMode", false) //
 * 	));
 * }
 * </pre>
 *
 * <p>
 * Example 2: Add Modbus/TCP-Api Controller by default:
 *
 * <pre>
 * if (existingConfigs.stream().noneMatch(c -> //
 * // Check if either "Controller.Api.Rest.ReadOnly" or
 * // "Controller.Api.Rest.ReadWrite" exist
 * "Controller.Api.ModbusTcp.ReadOnly".equals(c.factoryPid)
 * 		|| "Controller.Api.ModbusTcp.ReadWrite".equals(c.factoryPid))) {
 * 	// if not -> create configuration for "Controller.Api.Rest.ReadOnly"
 * 	this.createConfiguration(defaultConfigurationFailed, "Controller.Api.ModbusTcp.ReadOnly", Arrays.asList(//
 * 			new Property("id", "ctrlApiModbusTcp0"), //
 * 			new Property("alias", ""), //
 * 			new Property("enabled", true), //
 * 			new Property("port", 502), //
 * 			new Property("component.ids", JsonUtils.buildJsonArray().add("_sum").build()), //
 * 			new Property("maxConcurrentConnections", 5) //
 * 	));
 * }
 * </pre>
 */
public class DefaultConfigurationWorker extends ComponentManagerWorker {

	/**
	 * Time to wait before doing the check. This allows the system to completely
	 * boot and read configurations.
	 */
	private static final int INITIAL_WAIT_TIME = 5_000; // in ms

	private final Logger log = LoggerFactory.getLogger(DefaultConfigurationWorker.class);
	private final ConfigurationUtils configUtils; // oEMS configUtils will be used

	public DefaultConfigurationWorker(ComponentManagerImpl parent) {
		super(parent);
		this.configUtils = new ConfigurationUtils(this.parent, this.log);
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
		 * Create Default Logging configuration
		 */
		if (existingConfigs.stream().noneMatch(c -> //
		"org.ops4j.pax.logging".equals(c.pid) && c.properties.get("log4j2.rootLogger.level") != null)) {
			// Adding Configuration manually, because this is not a OpenEMS Configuration
			try {
				var log4j = new Hashtable<String, Object>();
				log4j.put("log4j2.appender.console.type", "Console");
				log4j.put("log4j2.appender.console.name", "console");
				log4j.put("log4j2.appender.console.layout.type", "PatternLayout");
				log4j.put("log4j2.appender.console.layout.pattern", "%d{ISO8601} [%-8.8t] %-5p [%-30.30c] %m%n");

				log4j.put("log4j2.appender.paxosgi.type", "PaxOsgi");
				log4j.put("log4j2.appender.paxosgi.name", "paxosgi");

				log4j.put("log4j2.rootLogger.level", "INFO");
				log4j.put("log4j2.rootLogger.appenderRef.console.ref", "console");
				log4j.put("log4j2.rootLogger.appenderRef.paxosgi.ref", "paxosgi");
				var config = this.parent.cm.getConfiguration("org.ops4j.pax.logging", null);
				config.update(log4j);
			} catch (IOException e) {
				this.parent.logError(this.log, "Unable to create Default Logging configuration: " + e.getMessage());
				e.printStackTrace();
				defaultConfigurationFailed.set(true);
			}
		}

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
		return DefaultConfigurationWorker.INITIAL_WAIT_TIME;
	}

}
