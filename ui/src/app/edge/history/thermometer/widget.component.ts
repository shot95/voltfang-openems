import { Component, Input, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DefaultTypes } from 'src/app/shared/service/defaulttypes';
import { ChannelAddress, Edge, EdgeConfig, Service, Utils } from '../../../shared/shared';
import { AbstractHistoryWidget } from '../abstracthistorywidget';
import { isSupportedThermometerWidgetFactory } from 'src/app/shared/edge/thermometer/thermometer.widget.factories';

@Component({
    selector: ThermometerWidgetComponent.SELECTOR,
    templateUrl: './widget.component.html',
})
export class ThermometerWidgetComponent extends AbstractHistoryWidget implements OnInit, OnChanges, OnDestroy {

    @Input() public period: DefaultTypes.HistoryPeriod;

    private static readonly SELECTOR = "thermometerWidget";

    public readonly CONVERT_TO_CELSIUS = Utils.CONVERT_TO_DEGREE;

    //in °C
    public minTemperature: number | null = null;
    public maxTemperature: number | null = null;

    public edge: Edge = null;

    protected thermometerComponents: EdgeConfig.Component[] = [];

    constructor(
        public override service: Service,
        private route: ActivatedRoute,
    ) {
        super(service);
    }
    ngOnInit() {
        this.service.setCurrentComponent('', this.route).then(edge => {
            this.edge = edge;
        });
    }

    ngOnDestroy() {
        this.unsubscribeWidgetRefresh();
    }

    ngOnChanges() {
        this.updateValues();
    }

    protected override updateValues() {

    }

    protected getChannelAddresses(edge: Edge, config: EdgeConfig): Promise<ChannelAddress[]> {
        return new Promise((resolve) => {
            const channels: ChannelAddress[] = [];
            this.thermometerComponents = config.getComponentsImplementingNature("io.openems.edge.thermometer.api.Thermometer")
                .filter(component =>
                    isSupportedThermometerWidgetFactory(component.factoryId));
            for (const component of this.thermometerComponents) {
                channels.push(new ChannelAddress(component.id, 'Temperature'));
            }
            resolve(channels);
        });
    }
}

