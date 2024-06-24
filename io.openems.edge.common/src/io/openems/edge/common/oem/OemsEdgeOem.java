package io.openems.edge.common.oem;

import io.openems.common.channel.Level;
import io.openems.common.channel.PersistencePriority;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;

public interface OemsEdgeOem extends OpenemsComponent {
	
	public static final String SINGLETON_SERVICE_PID = "oEms.OemProvider";
	public static final String SINGLETON_COMPONENT_ID = "_oem";
	
	public static final String DEFAULT_EDGE_ID = "edge-notSet";
	public static final String DEFAULT_SERIALNUMBER = "serialnumber-notSet";
	
	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		EDGE_ID_NOT_SET(Doc.of(Level.FAULT)//
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Edge-ID not set")), //
		SERIAL_NUMBER_NOT_SET(Doc.of(Level.FAULT)//
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Serialnumber not set")) //
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	/**
	 * Gets the Channel for {@link ChannelId#EDGE_ID_NOT_SET}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getEdgeIdNotSetChannel() {
		return this.channel(ChannelId.EDGE_ID_NOT_SET);
	}

	/**
	 * Gets the Disk is Full Warning State. See {@link ChannelId#EDGE_ID_NOT_SET}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getEdgeIdNotSet() {
		return this.getEdgeIdNotSetChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on {@link ChannelId#EDGE_ID_NOT_SET}
	 * Channel.
	 *
	 * @param value the next value
	 */
	public default void _setEdgeIdNotSet(boolean value) {
		this.getEdgeIdNotSetChannel().setNextValue(value);
	}

	/**
	 * Gets the Channel for {@link ChannelId#SERIAL_NUMBER_NOT_SET}.
	 *
	 * @return the Channel
	 */
	public default StateChannel getSerialNumberNotSetChannel() {
		return this.channel(ChannelId.SERIAL_NUMBER_NOT_SET);
	}

	/**
	 * Gets the Disk is Full Warning State. See
	 * {@link ChannelId#SERIAL_NUMBER_NOT_SET}.
	 *
	 * @return the Channel {@link Value}
	 */
	public default Value<Boolean> getSerialNumberNotSet() {
		return this.getSerialNumberNotSetChannel().value();
	}

	/**
	 * Internal method to set the 'nextValue' on
	 * {@link ChannelId#SERIAL_NUMBER_NOT_SET} Channel.
	 *
	 * @param value the next value
	 */
	public default void _setSerialNumberNotSet(boolean value) {
		this.getSerialNumberNotSetChannel().setNextValue(value);
	}
}
