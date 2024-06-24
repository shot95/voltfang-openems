package io.openems.edge.pvinverter.cluster;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "PV-Inverter Cluster", //
		description = "Combines several PV-Inverters to one.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "pvInverter0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "PV-Inverter-IDs", description = "IDs of PvInverter devices.")
	String[] pvInverter_ids();

	@AttributeDefinition(name = "Master Mode", description = "If true, the output power is completely passed to the masterInverter (masterInverter_id)")
	boolean masterMode() default false; // oEMS

	@AttributeDefinition(name = "Master-Inverter-ID", description = "ID of the master PvInverter device (only needed, when masterMode is true).")
	String masterInverter_id(); // oEMS

	String webconsole_configurationFactory_nameHint() default "PV-Inverter Cluster [{id}]";
}
