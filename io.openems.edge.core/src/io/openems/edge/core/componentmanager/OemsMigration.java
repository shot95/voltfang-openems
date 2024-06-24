package io.openems.edge.core.componentmanager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openems.common.jsonrpc.request.UpdateComponentConfigRequest.Property;
import io.openems.edge.core.componentmanager.ConfigurationUtils.Config;

/**
 * OemsMigration is responsible for migration of existing component
 * configurations.
 */
public class OemsMigration {

	private final ConfigurationUtils configUtils;

	public OemsMigration(ConfigurationUtils configUtils) {
		this.configUtils = configUtils;
	}

	/**
	 * Migrate configurations.
	 * 
	 * @param existingConfigs     the existing {@link Config}s
	 * @param micrationConfigFailed the result of the configuration, updated on error
	 */
	public void migrateConfigurations(List<Config> existingConfigs, AtomicBoolean micrationConfigFailed) {
		// this.migrateConfigurationOnVersion_2020_11_5(existingConfigs,
		// micrationConfigFailed);
	}

	/**
	 * Migrate to OpenEMS version 2020.11.5.
	 *
	 * @param existingConfigs     the existing {@link Config}s
	 * @param configurationFailed the result of the configuration, updated on error
	 */
	@SuppressWarnings("unused")
	private void migrateConfigurationOnVersion_2020_11_5(List<Config> existingConfigs,
			AtomicBoolean configurationFailed) {
		/*
		 * Upgrade to generic SOCOMEC implementation.
		 */
		// Threephase
		existingConfigs.stream().filter(c -> c.componentId.isPresent() && (//
		"Meter.SOCOMEC.DirisA10".equals(c.factoryPid) //
				|| "Meter.SOCOMEC.DirisA14".equals(c.factoryPid) //
				|| "Meter.SOCOMEC.DirisB30".equals(c.factoryPid) //
				|| "Meter.SOCOMEC.DirisE24".equals(c.factoryPid) //
		)).forEach(c -> {
			String alias = DictionaryUtils.getAsOptionalString(c.properties, "alias").orElse("");
			boolean enabled = DictionaryUtils.getAsOptionalBoolean(c.properties, "enabled").orElse(true);
			String modbusId = DictionaryUtils.getAsString(c.properties, "modbus.id");
			int modbusUnitId = DictionaryUtils.getAsInteger(c.properties, "modbusUnitId"); // can cause NPE
			boolean invert = DictionaryUtils.getAsOptionalBoolean(c.properties, "invert").orElse(false);
			String type = DictionaryUtils.getAsString(c.properties, "type");

			this.configUtils.deleteConfiguration(configurationFailed, c.componentId.get());

			this.configUtils.createConfiguration(configurationFailed, "Meter.Socomec.Threephase", Arrays.asList(//
					new Property("id", c.componentId.get()), //
					new Property("alias", alias), //
					new Property("enabled", enabled), //
					new Property("modbus.id", modbusId), //
					new Property("modbusUnitId", modbusUnitId), //
					new Property("invert", invert), //
					new Property("type", type) //
			));
		});
		// Singlephase
		existingConfigs.stream().filter(c -> c.componentId.isPresent() //
				&& "Meter.SOCOMEC.CountisE24".equals(c.factoryPid)//
		).forEach(c -> {
			String alias = DictionaryUtils.getAsOptionalString(c.properties, "alias").orElse("");
			boolean enabled = DictionaryUtils.getAsOptionalBoolean(c.properties, "enabled").orElse(true);
			String modbusId = DictionaryUtils.getAsString(c.properties, "modbus.id");
			int modbusUnitId = DictionaryUtils.getAsInteger(c.properties, "modbusUnitId"); // can cause NPE
			boolean invert = DictionaryUtils.getAsOptionalBoolean(c.properties, "invert").orElse(false);
			String type = DictionaryUtils.getAsString(c.properties, "type");

			this.configUtils.deleteConfiguration(configurationFailed, c.componentId.get());

			this.configUtils.createConfiguration(configurationFailed, "Meter.Socomec.Singlephase", Arrays.asList(//
					new Property("id", c.componentId.get()), //
					new Property("alias", alias), //
					new Property("enabled", enabled), //
					new Property("modbus.id", modbusId), //
					new Property("modbusUnitId", modbusUnitId), //
					new Property("invert", invert), //
					new Property("type", type) //
			));
		});
	}

}
