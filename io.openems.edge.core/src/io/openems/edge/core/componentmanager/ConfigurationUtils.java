package io.openems.edge.core.componentmanager;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;

import io.openems.common.exceptions.OpenemsException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.request.CreateComponentConfigRequest;
import io.openems.common.jsonrpc.request.DeleteComponentConfigRequest;
import io.openems.common.jsonrpc.request.UpdateComponentConfigRequest;
import io.openems.common.jsonrpc.request.UpdateComponentConfigRequest.Property;

public class ConfigurationUtils {

	private final ComponentManagerImpl parent;
	private final Logger log;
	
	public ConfigurationUtils(ComponentManagerImpl parent, Logger log) {
		this.parent = parent;
		this.log = log;
	}
	
	/**
	 * Reads all currently active configurations.
	 *
	 * @return a list of currently active {@link Config}s
	 */
	protected List<Config> readConfigs() {
		List<Config> result = new ArrayList<>();
		try {
			var cm = this.parent.cm;
			var configs = cm.listConfigurations(null); // NOTE: here we are not filtering for enabled=true
			if (configs != null) {
				for (Configuration config : configs) {
					result.add(Config.from(config));
				}
			}
		} catch (Exception e) {
			this.parent.logError(this.log, e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	
	
	/**
	 * Creates a Component configuration.
	 *
	 * @param defaultConfigurationFailed the result of the last configuration,
	 *                                   updated on error
	 * @param factoryPid                 the Factory-PID
	 * @param properties                 the Component properties
	 */
	protected void createConfiguration(AtomicBoolean defaultConfigurationFailed, String factoryPid,
			List<Property> properties) {
		try {
			this.parent.logInfo(this.log,
					"Creating Component configuration [" + factoryPid + "]: " + properties.stream() //
							.map(p -> p.getName() + ":" + p.getValue().toString()) //
							.collect(Collectors.joining(", ")));
			var response = this.parent.handleCreateComponentConfigRequest(null /* no user */,
					new CreateComponentConfigRequest(factoryPid, properties));
			response.get(60, TimeUnit.SECONDS);

		} catch (OpenemsNamedException | InterruptedException | ExecutionException | TimeoutException e) {
			this.parent.logError(this.log,
					"Unable to create Component configuration for Factory [" + factoryPid + "]: " + e.getMessage());
			e.printStackTrace();
			defaultConfigurationFailed.set(true);
		}
	}

	/**
	 * Updates a Component configuration.
	 *
	 * @param defaultConfigurationFailed the result of the last configuration,
	 *                                   updated on error
	 * @param componentId                the Component-ID
	 * @param properties                 the Component properties
	 */
	protected void updateConfiguration(AtomicBoolean defaultConfigurationFailed, String componentId,
			List<Property> properties) {
		try {
			this.parent.logInfo(this.log,
					"Updating Component configuration [" + componentId + "]: " + properties.stream() //
							.map(p -> p.getName() + ":" + p.getValue().toString()) //
							.collect(Collectors.joining(", ")));

			var response = this.parent.handleUpdateComponentConfigRequest(null /* no user */,
					new UpdateComponentConfigRequest(componentId, properties));
			response.get(60, TimeUnit.SECONDS);

		} catch (OpenemsNamedException | InterruptedException | ExecutionException | TimeoutException e) {
			this.parent.logError(this.log,
					"Unable to update Component configuration for Component [" + componentId + "]: " + e.getMessage());
			e.printStackTrace();
			defaultConfigurationFailed.set(true);
		}
	}

	/**
	 * Deletes a Component configuration.
	 *
	 * @param defaultConfigurationFailed the result of the last configuration,
	 *                                   updated on error
	 * @param componentId                the Component-ID
	 */
	protected void deleteConfiguration(AtomicBoolean defaultConfigurationFailed, String componentId) {
		try {
			this.parent.logInfo(this.log, "Deleting Component [" + componentId + "]");

			var response = this.parent.handleDeleteComponentConfigRequest(null /* no user */,
					new DeleteComponentConfigRequest(componentId));
			response.get(60, TimeUnit.SECONDS);

		} catch (OpenemsNamedException | InterruptedException | ExecutionException | TimeoutException e) {
			this.parent.logError(this.log, "Unable to delete Component [" + componentId + "]: " + e.getMessage());
			e.printStackTrace();
			defaultConfigurationFailed.set(true);
		}
	}
	
	
	/**
	 * Holds a configuration.
	 */
	public static class Config {
		protected static Config from(Configuration config) throws OpenemsException {
			var properties = config.getProperties();
			if (properties == null) {
				throw new OpenemsException(config.getPid() + ": Properties is 'null'");
			}
			var componentIdObj = properties.get("id");
			String componentId;
			if (componentIdObj != null) {
				componentId = componentIdObj.toString();
			} else {
				componentId = null;
			}
			var pid = config.getPid();
			return new Config(config.getFactoryPid(), componentId, pid, properties);
		}

		protected final String factoryPid;
		protected final Optional<String> componentId;
		protected final String pid;
		protected final Dictionary<String, Object> properties;

		private Config(String factoryPid, String componentId, String pid, Dictionary<String, Object> properties) {
			this.factoryPid = factoryPid;
			this.componentId = Optional.ofNullable(componentId);
			this.pid = pid;
			this.properties = properties;
		}
	}
}
