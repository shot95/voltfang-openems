package io.openems.backend.timedata.influx;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.shared.influxdb.QueryLanguageConfig;

@ObjectClassDefinition(//
		name = "Timedata.InfluxDB", //
		description = "Configures the InfluxDB timedata provider")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "timedata0";

	@AttributeDefinition(name = "Query language", description = "Query language Flux or InfluxQL")
	QueryLanguageConfig queryLanguage() default QueryLanguageConfig.INFLUX_QL;

	@AttributeDefinition(name = "URL", description = "The InfluxDB URL, e.g.: http://ip:port")
	String url();

	@AttributeDefinition(name = "Org", description = "The Organisation; '-' for InfluxDB v1")
	String org() default "-";

	@AttributeDefinition(name = "ApiKey", description = "The ApiKey; 'username:password' for InfluxDB v1")
	String apiKey();

	@AttributeDefinition(name = "Bucket", description = "The bucket name; 'database/retentionPolicy' for InfluxDB v1")
	String bucket();

	@AttributeDefinition(name = "Measurement", description = "The InfluxDB measurement")
	String measurement() default "data";

	@AttributeDefinition(name = "Read-Only mode", description = "Activates the read-only mode. Then no data is written to InfluxDB.")
	boolean isReadOnly() default false;

	@AttributeDefinition(name = "Number of Threads", description = "Pool-Size: the number of threads dedicated to handle the tasks")
	int poolSize() default 10;

	@AttributeDefinition(name = "Number of max scheduled tasks", description = "Max-Size of Queued tasks.")
	int maxQueueSize() default 5000;

	@AttributeDefinition(name = "Channel Whitelist", description = "Channel whitelist (e.g. 'data/channelWhitelist.ems.config') - " //
			+ "channels on these list will be written to the database. If empty it writes all channels.")
	String channelWhitelist();

	@AttributeDefinition(name = "Edge-Filter", description = "Filter Edges by starting digits (e.g. '1011') or by full id (e.g. '100000001'), leave empty for no filter.")
	String[] edgeFilter() default {};

	@AttributeDefinition(name = "Verbose", description = "Enable verbose log output (applied Edge-Filter ids.")
	boolean verbose() default false;

	@AttributeDefinition(name = "Debug Write", description = "Enable write log output.")
	boolean debugWrite() default false;

	String webconsole_configurationFactory_nameHint() default "Timedata InfluxDB";

}
