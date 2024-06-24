package io.openems.oems.oem.edge;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.common.oem.OemsEdgeOem;

@ObjectClassDefinition(//
		name = "oEMS Edge OEM Provider", //
		description = "The global oEMS OEM Provider.")
@interface Config {

	@AttributeDefinition(name = "Edge-ID", description = "The Edge-ID of the primary backend, e.g. 'edge-100000001'. ")
	String edgeId() default OemsEdgeOem.DEFAULT_EDGE_ID;

	@AttributeDefinition(name = "Serialnumber", description = "Manufacturer Serialnumber - Hardware Serial number.")
	String serialNumber() default OemsEdgeOem.DEFAULT_SERIALNUMBER;

	@AttributeDefinition(name = "Options", description = "Manufacturer Options.")
	String options();
	
	String webconsole_configurationFactory_nameHint() default "oEMS Edge OEM Provider";

}