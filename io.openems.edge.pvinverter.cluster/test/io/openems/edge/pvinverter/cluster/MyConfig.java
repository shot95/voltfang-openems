package io.openems.edge.pvinverter.cluster;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String[] pvInverterIds;
		private String masterInverterId;

		private Builder() {
		}

		public MyConfig build() {
			return new MyConfig(this);
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setMasterInverterId(String masterInverterId) {
			this.masterInverterId = masterInverterId;
			return this;
		}

		public Builder setPvInverterIds(String... pvInverterIds) {
			this.pvInverterIds = pvInverterIds;
			return this;
		}
	}

	/**
	 * Create a Config builder.
	 *
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String masterInverter_id() {
		return this.builder.masterInverterId;
	}

	@Override
	public boolean masterMode() {
		return false;
	}

	@Override
	public String[] pvInverter_ids() {
		return this.builder.pvInverterIds;
	}
}