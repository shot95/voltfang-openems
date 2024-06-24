import { Component } from '@angular/core';
import { ChannelAddress, EdgeConfig, Utils } from 'src/app/shared/shared';
import { AbstractFlatWidget } from 'src/app/shared/genericComponents/flat/abstract-flat-widget';
import { isSupportedThermometerWidgetFactory } from 'src/app/shared/edge/thermometer/thermometer.widget.factories';

@Component({
  selector: 'Thermometer',
  templateUrl: './thermometer.component.html',
})
export class ThermometerComponent extends AbstractFlatWidget {


  protected sensors: EdgeConfig.Component[] | null;
  protected readonly CONVERT_TO_DEGREE = Utils.CONVERT_TO_DEGREE;

  protected override getChannelAddresses() {

    const channelAddresses: ChannelAddress[] = [];

    // Get Thermometer Components

    /*
      if someone wants all Thermometer in their UI remove isSupportedThermometerWidgetFactory
    */
    this.sensors = this.config.getComponentsImplementingNature('io.openems.edge.thermometer.api.Thermometer')
      .filter(component => component.isEnabled && isSupportedThermometerWidgetFactory(component.factoryId))
      .sort((c1, c2) => c1.alias.localeCompare(c2.alias));

    for (const sensor of this.sensors) {
      channelAddresses.push(
        new ChannelAddress(sensor.id, 'Temperature'),
      );
    }
    return channelAddresses;
  }
}
