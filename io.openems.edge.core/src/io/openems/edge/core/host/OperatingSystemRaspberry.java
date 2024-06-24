package io.openems.edge.core.host;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.ConfigurationProperty;
import io.openems.common.utils.StringUtils;
import io.openems.edge.common.user.User;
//import io.openems.edge.core.host.NetworkInterface.Inet4AddressWithNetmask;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandRequest;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandRequest.SystemCommand;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandResponse;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandResponse.SystemCommandResponse;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemRestartRequest;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemRestartResponse;
import io.openems.edge.core.host.jsonrpc.SetNetworkConfigRequest;

/**
 * OperatingSystem implementation for generic raspberry systems.
 */
public class OperatingSystemRaspberry implements OperatingSystem {

	private static final Path UDEV_PATH = Paths.get("/etc/udev/rules.d/99-usb-serial.rules");
	private final HostImpl parent;

	protected OperatingSystemRaspberry(HostImpl parent) {
		this.parent = parent;
	}

	@Override
	public NetworkConfiguration getNetworkConfiguration() throws OpenemsNamedException {
		var interfaces = new TreeMap<String, NetworkInterface<?>>();

		NetworkInterface<File> ni = this.parseNetworkdConfiguration("eth0");
		interfaces.put(ni.getName(), ni);
		try {
			ni = this.parseNetworkdConfiguration("eth1");
			interfaces.put(ni.getName(), ni);
		} catch (OpenemsException e) {
			; // no eth1
		}
		return new NetworkConfiguration(interfaces);
	}

	@Override
	public void handleSetNetworkConfigRequest(User user, NetworkConfiguration oldNetworkConfiguration,
			SetNetworkConfigRequest request) throws OpenemsNamedException {

		// Note: we do not allow to change Network from outside
		throw new OpenemsException("Changing the IP address is not allowed");
	}

	// making this a class member is a bit dirty
	private Inet4Address ifAddress = null;

	private synchronized Inet4AddressWithSubnetmask evalInet4AddressForNetwork(String networkName)
			throws OpenemsNamedException {
		short netmaskSize = 32;
		try {
			var interfaceName = networkName;
			java.net.NetworkInterface networkInterface = java.net.NetworkInterface.getByName(interfaceName);
			Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
			while (inetAddress.hasMoreElements()) {
				InetAddress currentAddress = inetAddress.nextElement();
				if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
					this.ifAddress = (Inet4Address) currentAddress;
					// get netmask
					InterfaceAddress ifadr = networkInterface.getInterfaceAddresses().get(0);
					netmaskSize = ifadr.getNetworkPrefixLength();
					return new Inet4AddressWithSubnetmask(networkName, this.ifAddress, netmaskSize);
				}
			}
		} catch (Exception e) {
			throw new OpenemsException("Unable to read " + networkName + " ex: " + e.getMessage());
		}
		throw new OpenemsException("No ipv4 address for " + networkName + " found");
	}

	protected <A> NetworkInterface<A> parseNetworkdConfiguration(String networkName) throws OpenemsNamedException {

		final Inet4AddressWithSubnetmask i4sn = this.evalInet4AddressForNetwork(networkName);
		final var linkLocalAddressing = ConfigurationProperty.of(this.ifAddress.isLinkLocalAddress());
		final var dhcp = ConfigurationProperty.of(true); // TODO

		ConfigurationProperty<Inet4Address> dns;
		ConfigurationProperty<Inet4Address> gateway;

		try {
			// TODO proper DNS and gateway configuration
			dns = ConfigurationProperty.of((Inet4Address) Inet4Address.getByName("0.0.0.0"));
			gateway = ConfigurationProperty.of((Inet4Address) Inet4Address.getByName("0.0.0.0"));
		} catch (UnknownHostException e) {
			dns = ConfigurationProperty.asNotSet();
			gateway = ConfigurationProperty.asNotSet();
		}

		String alias = networkName;
		if (networkName.compareTo("eth0") == 0) {
			alias = "LAN A (eth0)"; // kunbus specific
		} else if (networkName.compareTo("eth1") == 0) {
			alias = "LAN B (eth1)";
		}

		var addresses = new AtomicReference<ConfigurationProperty<Set<Inet4AddressWithSubnetmask>>>(
				ConfigurationProperty.asNotSet());
		var adrContent = addresses.get().getValue();
		adrContent = new HashSet<>();
		adrContent.add(Inet4AddressWithSubnetmask.fromString(i4sn.toString()));
		addresses.set(ConfigurationProperty.of(adrContent));

		return new NetworkInterface<>(alias, dhcp, linkLocalAddressing, gateway, dns, addresses.get(), null);

	}

	@Override
	public CompletableFuture<ExecuteSystemCommandResponse> handleExecuteSystemCommandRequest(
			ExecuteSystemCommandRequest request) {
		var result = new CompletableFuture<ExecuteSystemCommandResponse>();
		this.execute(request.systemCommand, //
				scr -> result.complete(new ExecuteSystemCommandResponse(request.id, scr)),
				e -> result.completeExceptionally(e));
		return result;
	}

	@Override
	public CompletableFuture<ExecuteSystemRestartResponse> handleExecuteSystemRestartRequest(
			ExecuteSystemRestartRequest request) {
		final var result = new CompletableFuture<ExecuteSystemRestartResponse>();
		var sc = new SystemCommand(//
				switch (request.type) { // actual command string
				case HARD -> "/usr/bin/systemctl reboot -i"; // "-i" is for "ignore inhibitors and users"
				case SOFT -> "/usr/bin/systemctl restart openems";
				}, //
				false, // runInBackground
				5, // timeoutSeconds
				Optional.empty(), // username
				Optional.empty()); // password
		this.execute(sc, //
				scr -> result.complete(new ExecuteSystemRestartResponse(request.id, scr)),
				e -> result.completeExceptionally(e));
		return result;
	}

	private void execute(SystemCommand sc, Consumer<SystemCommandResponse> scr, Consumer<Throwable> error) {
		try {
			final Process proc;
			if (sc.username().isPresent() && sc.password().isPresent()) {
				// Authenticate with user and password
				proc = getRuntime().exec(new String[] { //
						"/bin/bash", "-c", "--", //
						"echo " + sc.password().get() + " | " //
								+ " /usr/bin/sudo -Sk -p '' -u \"" + sc.username().get() + "\" -- " //
								+ sc.command() });
			} else if (sc.password().isPresent()) {
				// Authenticate with password (user must have 'sudo' permissions)
				proc = getRuntime().exec(new String[] { //
						"/bin/bash", "-c", "--", //
						"echo " + sc.password().get() + " | " //
								+ " /usr/bin/sudo -Sk -p '' -- " //
								+ sc.command() });
			} else {
				// No authentication: run as current user
				proc = getRuntime().exec(new String[] { //
						"/bin/bash", "-c", "--", sc.command() });
			}

			// get stdout and stderr
			var stdoutFuture = supplyAsync(new InputStreamToString(this.parent, sc.command(), proc.getInputStream()));
			var stderrFuture = supplyAsync(new InputStreamToString(this.parent, sc.command(), proc.getErrorStream()));

			if (sc.runInBackground()) {
				/*
				 * run in background
				 */
				var stdout = new String[] { //
						"Command [" + sc.command() + "] executed in background...", //
						"Check system logs for more information." };
				scr.accept(new SystemCommandResponse(stdout, new String[0], 0));

			} else {
				/*
				 * run in foreground with timeout
				 */
				runAsync(() -> {
					var stderr = new ArrayList<>();
					try {
						// apply command timeout
						if (!proc.waitFor(sc.timeoutSeconds(), TimeUnit.SECONDS)) {
							stderr.add("Command [" + sc.command() + "] timed out.");
							proc.destroy();
						}

						var stdout = stdoutFuture.get(1, TimeUnit.SECONDS);
						stderr.addAll(stderrFuture.get(1, TimeUnit.SECONDS));
						scr.accept(new SystemCommandResponse(//
								stdout.toArray(new String[stdout.size()]), //
								stderr.toArray(new String[stderr.size()]), //
								proc.exitValue() //
						));

					} catch (Throwable e) {
						error.accept(e);
					}
				});
			}
		} catch (IOException e) {
			error.accept(e);
		}
	}

	/**
	 * Asynchronously converts a InputStream to a String.
	 */
	private static class InputStreamToString implements Supplier<List<String>> {
		private final Logger log = LoggerFactory.getLogger(InputStreamToString.class);

		private final HostImpl parent;
		private final String command;
		private final InputStream stream;

		public InputStreamToString(HostImpl parent, String command, InputStream stream) {
			this.parent = parent;
			this.command = StringUtils.toShortString(command, 20);
			this.stream = stream;
		}

		@Override
		public List<String> get() {
			List<String> result = new ArrayList<>();
			BufferedReader reader = null;
			String line = null;
			try {
				reader = new BufferedReader(new InputStreamReader(this.stream));
				while ((line = reader.readLine()) != null) {
					result.add(line);
					this.parent.logInfo(this.log, "[" + this.command + "] " + line);
				}
			} catch (Throwable e) {
				result.add(e.getClass().getSimpleName() + ": " + line);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						/* ignore */
					}
				}
			}
			return result;
		}
	}

	@Override
	public String getUsbConfiguration() throws OpenemsNamedException {
		try {
			if (!Files.exists(UDEV_PATH)) {
				return "";
			}
			var lines = Files.readAllLines(UDEV_PATH, StandardCharsets.UTF_8);
			return String.join("\n", lines);
		} catch (IOException e) {
			throw new OpenemsException("Unable to read file [" + UDEV_PATH + "]: " + e.getMessage());
		}
	}

}
