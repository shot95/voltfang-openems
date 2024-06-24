package io.openems.edge.pvinverter.api;

import io.openems.edge.meter.api.ElectricityMeter;

/**
 * A MetaPvInverter is a wrapper for physical Pv Inverter systems. It is not a
 * physical PvInverter itself. This is used to distinguish e.g. an PvInverterCluster from an
 * actual PvInverter.
 */
public interface MetaPvInverter extends ElectricityMeter {


}
