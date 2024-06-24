export enum SupportedFactories {

    OneWire = 'OneWire.Thermometer',
    ConfigThermometer = 'Config.Thermometer',
    HeaterThermometer = 'Thermometer.Heater',

}

export function isSupportedThermometerWidgetFactory(factoryId: string): boolean {
    return Object.values(SupportedFactories).includes(factoryId as SupportedFactories);
}
