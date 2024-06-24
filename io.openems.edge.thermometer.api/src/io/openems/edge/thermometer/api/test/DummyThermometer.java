package io.openems.edge.thermometer.api.test;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.thermometer.api.Thermometer;

/**
 * A Dummy Thermometer used in Unittests.
 */

public class DummyThermometer extends AbstractOpenemsComponent implements Thermometer, OpenemsComponent {

    public DummyThermometer(String id) {
	super(OpenemsComponent.ChannelId.values(), //
		Thermometer.ChannelId.values());
	this.channels().forEach(Channel::nextProcessImage);
	super.activate(null, id, "", true);
    }

}
