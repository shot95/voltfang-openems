package io.openems.common;

/**
 * Adjustments for OpenEMS OEM distributions.
 */
// CHECKSTYLE:OFF
public class OpenemsOEM {
	// CHECKSTYLE:ON
	public enum Manufacturer {
		OPENEMS, //
		VOLTFANG //
		;
		
	}

	/*
	 * General.
	 */
	public static final String MANUFACTURER = "Voltfang GmbH";

	/*
	 * Backend-Api Controller
	 */
	public static final String BACKEND_API_URI = "ws://localhost:8081";


	/*
	 * supported operating system. can be debian or raspberry 
	 */
	public static final String SUPPORTED_OPERATING_SYSTEM = "raspberry";

}
