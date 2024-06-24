package io.openems.common.channel;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import io.openems.common.types.OpenemsType;
import io.openems.common.utils.EnumUtils;

/**
 * Units of measurement used in OpenEMS.
 * 
 * <p>
 * Units marked as 'cumulated' are per definition steadily increasing, i.e.
 * {@code Value(t + 1) >= Value(t)}. This applies e.g. to consumed energy in
 * [Wh_Σ]. To calculate the 'discrete' consumed energy in [Wh] for a certain
 * period, subtract first cumulated value from last cumulated value.
 */
public enum Unit {
	// ##########
	// Generic
	// ##########

	/**
	 * No Unit.
	 */
	NONE(""),

	/**
	 * Percentage [%], 0-100.
	 */
	PERCENT("%"),

	/**
	 * Thousandth [‰], 0-1000.
	 */
	THOUSANDTH("‰"),

	/**
	 * On or Off.
	 */
	ON_OFF("On/Off"), // Symbol is ignored in #format()

	// ##########
	// Power
	// ##########

	/**
	 * Unit of Active Power [W].
	 */
	WATT("W"),

	/**
	 * Unit of Active Power [mW].
	 */
	MILLIWATT("mW", WATT, -3),

	/**
	 * Unit of Active Power [kW].
	 */
	KILOWATT("kW", WATT, 3),

	/**
	 * Unit of Reactive Power [var].
	 */
	VOLT_AMPERE_REACTIVE("var"),

	/**
	 * Unit of Reactive Power [kvar].
	 */
	KILOVOLT_AMPERE_REACTIVE("kvar", VOLT_AMPERE_REACTIVE, 3),

	/**
	 * Unit of Apparent Power [VA].
	 */
	VOLT_AMPERE("VA"),

	/**
	 * Unit of Apparent Power [kVA].
	 */
	KILOVOLT_AMPERE("kVA", VOLT_AMPERE, 3),

	// ##########
	// Voltage
	// ##########

	/**
	 * Unit of Voltage [V].
	 */
	VOLT("V"),

	/**
	 * Unit of Voltage [mV].
	 */
	MILLIVOLT("mV", VOLT, -3),

	/**
	 * Unit of Voltage [uV].
	 */
	MICROVOLT("uV", VOLT, -6),

	// ##########
	// Current
	// ##########

	/**
	 * Unit of Current [A].
	 */
	AMPERE("A"),

	/**
	 * Unit of Current [mA].
	 */
	MILLIAMPERE("mA", AMPERE, -3),

	/**
	 * Unit of Current [uA].
	 */
	MICROAMPERE("uA", AMPERE, -6),

	// ##########
	// Electric Charge
	// ##########

	/**
	 * Unit of Electric Charge.
	 */
	AMPERE_HOURS("Ah"),

	/**
	 * Unit of Electric Charge.
	 */
	MILLIAMPERE_HOURS("mAh", AMPERE_HOURS, -3),

	/**
	 * Unit of Electric Charge.
	 */
	KILOAMPERE_HOURS("kAh", AMPERE_HOURS, 3),

	// ##########
	// Energy
	// ##########

	/**
	 * Unit of Energy [Wh].
	 */
	WATT_HOURS("Wh"),

	/**
	 * Unit of Energy [kWh].
	 */
	KILOWATT_HOURS("kWh", WATT_HOURS, 3),

	/**
	 * Unit of Reactive Energy [varh].
	 */
	VOLT_AMPERE_REACTIVE_HOURS("varh"),

	/**
	 * Unit of Reactive Energy [kVArh].
	 */
	KILOVOLT_AMPERE_REACTIVE_HOURS("kvarh", VOLT_AMPERE_REACTIVE_HOURS, 3),

	/**
	 * Unit of Energy [Wh/Wp].
	 */
	WATT_HOURS_BY_WATT_PEAK("Wh/Wp"),

	/**
	 * Unit of Apparent Energy [VAh].
	 */
	VOLT_AMPERE_HOURS("VAh"),

	// ##########
	// Cumulated Energy
	// ##########

	/**
	 * Unit of cumulated Energy [Wh_Σ].
	 */
	CUMULATED_WATT_HOURS("Wh_Σ", WATT_HOURS),

	// ##########
	// Energy Tariff
	// ##########

	/**
	 * Unit of Energy Price [€/MWh].
	 */
	EUROS_PER_MEGAWATT_HOUR("€/MWh"),

	// ##########
	// Frequency
	// ##########

	/**
	 * Unit of Frequency [Hz].
	 */
	HERTZ("Hz"),

	/**
	 * Unit of Frequency [mHz].
	 */
	MILLIHERTZ("mHz", HERTZ, -3),

	// ##########
	// Temperature
	// ##########

	/**
	 * Unit of Temperature [C].
	 */
	DEGREE_CELSIUS("C"),

	/**
	 * Unit of Temperature [dC].
	 */
	DEZIDEGREE_CELSIUS("dC", DEGREE_CELSIUS, -1),

	// ##########
	// Time
	// ##########

	/**
	 * Unit of Time [s].
	 */
	SECONDS("sec"),

	/**
	 * Unit of Time [ms].
	 */
	MILLISECONDS("ms", SECONDS, -3),

	/**
	 * Unit of Time.
	 */
	MINUTE("min"),

	/**
	 * Unit of Time.
	 */
	HOUR("h"),

	// ##########
	// Cumulated Time
	// ##########

	/**
	 * Unit of cumulated time [s].
	 */
	CUMULATED_SECONDS("sec_Σ", SECONDS),

	// ##########
	// Resistance
	// ##########

	/**
	 * Unit of Resistance [Ohm].
	 */
	OHM("Ohm"),

	/**
	 * Unit of Resistance [kOhm].
	 */
	KILOOHM("kOhm", OHM, 3),

	/**
	 * Unit of Resistance [mOhm].
	 */
	MILLIOHM("mOhm", OHM, -3),

	/**
	 * Unit of Resistance [uOhm].
	 */
	MICROOHM("uOhm", OHM, -6),

	//oEMS start
	
	// ##########
	// Percolation Q
	// ##########

	/**
	 * Unit of Percolation [m³/s].
	 */
	CUBICMETER_PER_SECOND("m³/s"),

	/**
	 * Unit of Percolation [m³/h].
	 */
	CUBICMETER_PER_HOUR("m³/h"),

	/**
	 * Unit of Percolation [l/min].
	 */
	LITER_PER_MINUTE("l/min"),

	/**
	 * Unit of Percolation [dl/min].
	 */
	DECILITER_PER_MINUTE("dl/min", LITER_PER_MINUTE, -1),

	// ##########
	// Pressure
	// ##########
	/**
	 * Unit of Pressure[Pa].
	 */
	PASCAL("Pa"),

	/**
	 * Unit of Pressure[hPa].
	 */
	HECTO_PASCAL("hPa", PASCAL, 2),

	/**
	 * Unit of Pressure [bar].
	 */
	BAR("bar"),

	/**
	 * Unit of Pressure [dbar].
	 */
	DECI_BAR("dbar", BAR, -1),

	/**
	 * Unit of Pressure [cbar].
	 */
	CENTI_BAR("cbar", BAR, -2),

	// ##########
	// Rotation
	// ##########
	/**
	 * Unit of Rotation per seconds.
	 */
	ROTATION_PER_SECONDS("R/sec"),

	/**
	 * Unit of Rotation per minute.
	 */

	ROTATION_PER_MINUTE("R/min"),

	// ##########
	// Angle
	// ##########

	/**
	 * Unit of Degree [°].
	 *
	 */
	DEGREE("°"),

	MILLI_DEGREE("m°", DEGREE, -3),

	// #########
	// Volume
	// ########

	/**
	 * Unit volume [m³].
	 */
	CUBIC_METER("m³"),

	/**
	 * Unit volume [l].
	 */
	LITRES("l", CUBIC_METER, -3),

	// #########
	// Wireless signal strength
	// ########

	/**
	 * Unit of wireless signal strength [dBm].
	 */
	DECIBEL_MILLIWATT("dBm"),
	// #########
	// SPEED
	// ########

	/**
	 * Unit of speed [m/s].
	 */
	METER_PER_SECOND("m/s"),

	/**
	 * Unit of speed [mph].
	 */
	MILES_PER_HOUR("mph"),

	/**
	 * Unit of speed [km/h].
	 */
	KILOMETER_PER_HOUR("km/h"),

	// #########
	// Irradiation
	// ########

	/**
	 * Unit of Irradiation [W/m²].
	 */
	WATT_PER_SQUARE_METER("W/m²");

	//oEMS end

	public final String symbol;
	public final Unit baseUnit;
	public final int scaleFactor;
	public final Unit discreteUnit;

	/**
	 * Use this constructor for discrete Base-Units.
	 * 
	 * @param symbol the unit symbol
	 */
	private Unit(String symbol) {
		this.symbol = symbol;
		this.baseUnit = null;
		this.scaleFactor = 0;
		this.discreteUnit = null;
	}

	/**
	 * Use this constructor for cumulated Units.
	 * 
	 * @param symbol       the unit symbol
	 * @param discreteUnit the discrete unit that is derived by subtracting first
	 *                     cumulated value from last cumulated value.
	 */
	private Unit(String symbol, Unit discreteUnit) {
		this.symbol = symbol;
		this.baseUnit = null;
		this.scaleFactor = 0;
		this.discreteUnit = discreteUnit;
	}

	/**
	 * Use this constructor for discrete derived units.
	 * 
	 * @param symbol      the unit symbol
	 * @param baseUnit    the discrete Base-Unit of this Unit
	 * @param scaleFactor the scale factor to convert between this Unit and its
	 *                    Base-Unit.
	 */
	private Unit(String symbol, Unit baseUnit, int scaleFactor) {
		this.symbol = symbol;
		this.baseUnit = baseUnit;
		this.scaleFactor = scaleFactor;
		this.discreteUnit = null;
	}

	public Unit getBaseUnit() {
		return this.baseUnit;
	}

	/**
	 * Gets the value in its base unit, e.g. converts [kW] to [W].
	 *
	 * @param value the value
	 * @return the converted value
	 */
	public int getAsBaseUnit(int value) {
		return (int) (value * Math.pow(10, this.scaleFactor));
	}

	/**
	 * Gets the value in its base unit, e.g. converts [kW] to [W].
	 *
	 * @param value the value
	 * @return the converted value
	 */
	public int getAsBaseUnit(double value) {
		return (int) (value * Math.pow(10, this.scaleFactor));
	}

	/**
	 * Allows the conversion of a value to another scaled Unit. e.g. converts kV to
	 * mV. Unit conversion is only allowed if both Units have the same base Unit.
	 * 
	 * @param value the value
	 * @param unit  the other Unit to convert to.
	 * @return the converted value.
	 */
	public int convertToScaledUnit(int value, Unit unit) {
		if (this.baseUnit == unit.baseUnit || this == unit.baseUnit) {
			return (int) (this.getAsBaseUnit(value) * Math.pow(10, unit.scaleFactor * -1));
		} else if (this.baseUnit == unit) {
			return this.getAsBaseUnit(value);
		} else {
			return value;
		}
	}

	/**
	 * Allows the conversion of a value to another scaled Unit. e.g. converts kV to
	 * mV. Unit conversion is only allowed if both Units have the same base Unit.
	 *
	 * @param value the value
	 * @param unit  the other Unit to convert to.
	 * @return the converted value.
	 */
	public double convertToScaledUnit(double value, Unit unit) {
		if (this.baseUnit == unit.baseUnit || this == unit.baseUnit) {
			return (double) (this.getAsBaseUnit(value) * Math.pow(10, unit.scaleFactor * -1));
		} else if (this.baseUnit == unit) {
			return this.getAsBaseUnit(value);
		} else {
			return value;
		}
	}

	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * Formats the value in the given type.
	 *
	 * <p>
	 * For most cases this adds the unit symbol to the value, like "123 kW".
	 * Booleans are converted to "ON" or "OFF".
	 *
	 * @param value the value {@link Object}
	 * @param type  the {@link OpenemsType}
	 * @return the formatted value as String
	 */
	public String format(Object value, OpenemsType type) {
		return switch (this) {
		case NONE -> //
			value.toString();

		case AMPERE, DEGREE_CELSIUS, DEZIDEGREE_CELSIUS, EUROS_PER_MEGAWATT_HOUR, HERTZ, MILLIAMPERE, MICROAMPERE,
				MILLIHERTZ, MILLIVOLT, MICROVOLT, PERCENT, VOLT, VOLT_AMPERE, VOLT_AMPERE_REACTIVE, WATT, KILOWATT,
				MILLIWATT, WATT_HOURS, OHM, KILOOHM, SECONDS, AMPERE_HOURS, HOUR, CUMULATED_SECONDS, KILOAMPERE_HOURS,
				KILOVOLT_AMPERE, KILOVOLT_AMPERE_REACTIVE, KILOVOLT_AMPERE_REACTIVE_HOURS, KILOWATT_HOURS, MICROOHM,
				MILLIAMPERE_HOURS, MILLIOHM, MILLISECONDS, MINUTE, THOUSANDTH, VOLT_AMPERE_HOURS,
				VOLT_AMPERE_REACTIVE_HOURS, WATT_HOURS_BY_WATT_PEAK, CUMULATED_WATT_HOURS, BAR, 
				// oems start
				CUBICMETER_PER_SECOND, CUBICMETER_PER_HOUR, LITER_PER_MINUTE, DECILITER_PER_MINUTE, PASCAL,
				HECTO_PASCAL, DECI_BAR, CENTI_BAR, ROTATION_PER_SECONDS, ROTATION_PER_MINUTE, DEGREE, MILLI_DEGREE,
				CUBIC_METER, LITRES, DECIBEL_MILLIWATT, METER_PER_SECOND, MILES_PER_HOUR, KILOMETER_PER_HOUR,
				WATT_PER_SQUARE_METER // oems end
			-> value + " " + this.symbol;

		case ON_OFF -> //
			value == null ? "UNDEFINED" : ((Boolean) value).booleanValue() ? "ON" : "OFF";
		};
	}

	@Override
	public String toString() {
		return EnumUtils.nameAsCamelCase(this) + (this.symbol.isEmpty() ? "" : " [" + this.symbol + "]");
	}

	/**
	 * Finds a Unit by its Symbol.
	 * 
	 * @param symbol      the Symbol
	 * @param defaultUnit the defaultUnit; this value is returned if no Unit with
	 *                    the given Symbol exists
	 * @return the Unit; or the defaultUnit if it was not found
	 */
	public static Unit fromSymbolOrElse(String symbol, Unit defaultUnit) {
		return Stream.of(Unit.values()) //
				.filter(u -> u.symbol == symbol) //
				.findFirst() //
				.orElse(defaultUnit);
	}

	/**
	 * Get the corresponding aggregate function of current {@link Unit}.
	 * 
	 * @return corresponding aggregate function
	 */
	public Function<DoubleStream, OptionalDouble> getChannelAggregateFunction() {
		if (this.isCumulated()) {
			return DoubleStream::max;
		} else {
			return DoubleStream::average;
		}
	}

	/**
	 * Returns true if this is a cumulated unit.
	 * 
	 * @return true if this {@link Unit} is cumulated, otherwise false
	 */
	public boolean isCumulated() {
		return this.discreteUnit != null;
	}

	/*
	 * Static check for non-duplicated Symbols.
	 */
	static {
		if (!Stream.of(Unit.values())//
				.map(u -> u.symbol) //
				.allMatch(new HashSet<>()::add)) {
			throw new IllegalArgumentException("Symbols in Unit must be unique!");
		}
	}
}
