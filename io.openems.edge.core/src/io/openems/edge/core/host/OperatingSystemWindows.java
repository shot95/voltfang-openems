package io.openems.edge.core.host;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.openems.common.exceptions.NotImplementedException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;
import io.openems.common.utils.JsonUtils;
import io.openems.edge.common.user.User;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandRequest;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandResponse;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemCommandResponse.SystemCommandResponse;
import io.openems.edge.core.host.jsonrpc.ExecuteSystemRestartRequest;
import io.openems.edge.core.host.jsonrpc.SetNetworkConfigRequest;

/**
 * OperatingSystem implementation for Windows.
 */
public class OperatingSystemWindows implements OperatingSystem {

	protected OperatingSystemWindows() {
	}

	@Override
	public NetworkConfiguration getNetworkConfiguration() throws OpenemsNamedException {
		// not implemented
		return new NetworkConfiguration(new TreeMap<>());
	}

	@Override
	public void handleSetNetworkConfigRequest(User user, NetworkConfiguration oldNetworkConfiguration,
			SetNetworkConfigRequest request) throws OpenemsNamedException {
		throw new NotImplementedException("SetNetworkConfigRequest is not implemented for Windows");
	}

	@Override
	public CompletableFuture<ExecuteSystemCommandResponse> handleExecuteSystemCommandRequest(
			ExecuteSystemCommandRequest request) {
		// throw new NotImplementedException("ExecuteSystemCommandRequest is not
		// implemented for Windows");
		// oEMS
		final var result = new CompletableFuture<ExecuteSystemCommandResponse>();
		return this.oemsUpdateSimulation(request, result);
	}

	@Override
	public String getUsbConfiguration() throws OpenemsNamedException {
		// not implemented
		return "";
	}

	@Override
	public CompletableFuture<? extends JsonrpcResponseSuccess> handleExecuteSystemRestartRequest(
			ExecuteSystemRestartRequest request) throws NotImplementedException {
		throw new NotImplementedException("ExecuteSystemRestartRequest is not implemented for Windows");
	}

	// oEMS start
	private boolean versioning = false;

	private CompletableFuture<ExecuteSystemCommandResponse> oemsUpdateSimulation(ExecuteSystemCommandRequest request,
			CompletableFuture<ExecuteSystemCommandResponse> result) {

		// System.out.println("Windows Excecute Command:
		// ------------------------------\n\r\n\r");
		// System.out.println(request.getParams().toString());
		// System.out.println("------------------------------\n\r\n\r");

		String[] stdout = new String[1];
		try {
			var cmd = JsonUtils.getAsString(request.getParams(), "command");
			if (cmd.startsWith("dpkg-query")) {
				// simulate that this Version will be installed
				if (!this.versioning) {
					stdout = new String[] { "2024.3.1-1-1" };
				} else {
					stdout = new String[] { "2024.3.1-2-1" };
					this.versioning = false;

				}
			} else if (cmd.startsWith("which at")) {
				stdout = new String[] { "/usr/bin/at" };
			} else if (cmd.startsWith("echo '")) {
				stdout = new String[] { //
						"+-+-+-+ 1 Note: This is a Windows Execution Simulation", //
						"+-+-+-+ 2 ", //
						"+-+-+-+ 3 ", //
						"+-+-+-+ 4 ", //
						"#-#-#-# FINISHED SUCCESSFULLY" //
						// "#-#-#-# FINISHED WITH ERROR" //

				};

				var logfile = cmd.substring(cmd.indexOf("fi;   } >") + 9, cmd.indexOf(" 2>&1"));

				try (final var writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(logfile), StandardCharsets.ISO_8859_1))) {
					for (String s : stdout) {
						writer.write(s + "\n\r");
					}
				}
				this.versioning = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Throwable ex = null;

		Consumer<SystemCommandResponse> scr = s -> result.complete(new ExecuteSystemCommandResponse(request.id, s));
		Consumer<Throwable> error = e -> result.completeExceptionally(ex);
		var success = true;
		if (success) {
			scr.accept(new SystemCommandResponse(stdout, new String[0], 0));
		} else {
			error.accept(null);
		}
		return result;
	}
	// oEMS end
}
