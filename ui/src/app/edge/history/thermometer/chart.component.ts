import { Component} from '@angular/core';
import { ChartAxis, HistoryUtils, Utils, YAxisTitle } from 'src/app/shared/service/utils';
import { ChannelAddress,EdgeConfig } from '../../../shared/shared';
import { AbstractHistoryChart } from 'src/app/shared/genericComponents/chart/abstracthistorychart';
import { isSupportedThermometerWidgetFactory } from 'src/app/shared/edge/thermometer/thermometer.widget.factories';
import { getColorByIndexRGB } from 'src/app/shared/edge/colorpalette/color.palette';

type ChartLabels = {
  production: string,
  discharge: string,
  consumption: string,
  price: string
}

@Component({
  selector: 'thermometerChart',
  templateUrl: '../../../shared/genericComponents/chart/abstracthistorychart.html',
})
export class ThermometerChartComponent extends AbstractHistoryChart {

  public sensors: EdgeConfig.Component[] = null;

  protected override getChartData(): HistoryUtils.ChartData {
    this.forceLineChart = true;
    const thermometerChannel = this.getThermometerChannel();
    const channels: HistoryUtils.InputChannel[] = [];
    let i = 0;
    for(const index in thermometerChannel){
      channels.push({
        name: "Temperature" + index,
        powerChannel: thermometerChannel[index],
      });
      i++;
    }

    return{
      input: channels,
      output: (data: HistoryUtils.ChannelData) => {
        const datasets: HistoryUtils.DisplayValues[] = [];
        for(let j = 0; j < i; j++){
          let aliasName = this.translate.instant("General.temperature") + j;
          if (thermometerChannel[j]){
            aliasName = this.config.getComponent(thermometerChannel[j].componentId).alias;
          }
          datasets.push({
            name: aliasName,
            converter: () => {
              return data['Temperature'+j].map(value => Utils.multiplySafely(value, 100));
            },
            color: getColorByIndexRGB(j),
            order: j,
            hiddenOnInit: false,
          });
        }
        return datasets;
      },
      tooltip: {
        formatNumber: '1.0-2',

      },
      yAxes:[
        {
          unit: YAxisTitle.TEMPERATURE,
          position: 'left',
          yAxisId: ChartAxis.LEFT,
        }],
    };


  }


  private  getThermometerChannel(): ChannelAddress[] {

      const result: ChannelAddress[] = [];

      // Get sensorComponents
      this.sensors = this.config.getComponentsImplementingNature("io.openems.edge.thermometer.api.Thermometer")
        .filter(component =>
          isSupportedThermometerWidgetFactory(component.factoryId))
        .sort((c1, c2) => c1.alias.localeCompare(c2.alias));

      for (const sensor of this.sensors) {
        result.push(
          new ChannelAddress(sensor.id, 'Temperature'),
        );
      }
      return result;
  }

}
