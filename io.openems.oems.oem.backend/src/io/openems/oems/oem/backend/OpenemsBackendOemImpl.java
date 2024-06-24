package io.openems.oems.oem.backend;

import org.osgi.service.component.annotations.Component;

import io.openems.common.oem.OpenemsBackendOem;

@Component
public class OpenemsBackendOemImpl implements OpenemsBackendOem {

	@Override
	public String getInfluxdbTag() {
		return "edge";
	}

	@Override
	public String getAppCenterMasterKey() {
		return "8fyk-Gma9-EUO3-j3gi";
	}
}
