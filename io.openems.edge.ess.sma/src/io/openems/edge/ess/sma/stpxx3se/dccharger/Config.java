package io.openems.edge.ess.sma.stpxx3se.dccharger;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "ESS SMA Sunny Tripower SE DC Charger PV", //
		description = "Implements the SMA Sunny Tripower XX SE as a PV inverter.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "charger0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Core ID", description = "ID of the SMA STP XX SE Ess Component.")
	String core_id() default "batteryInverter0";
	
	@AttributeDefinition(name = "String Number", description = "SMA STP String to use.")
	PvString pvString() default PvString.ONE;

	String webconsole_configurationFactory_nameHint() default "ESS SMA Sunny Tripower SE DC Charger PV [{id}]";

}
