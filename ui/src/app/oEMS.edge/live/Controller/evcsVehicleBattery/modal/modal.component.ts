import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { RangeValue } from '@ionic/core';
import { TranslateService } from '@ngx-translate/core';
import { Edge, EdgeConfig, Service, Websocket } from 'src/app/shared/shared';

type Mode = 'AUTOMATIC' | 'CHARGE' | 'DISCHARGE' | 'OFF';


@Component({
    // selector: EvcsVehicleBatteryModalComponent.SELECTOR,
    templateUrl: './modal.component.html',
})
export class EvcsVehicleBatteryModalComponent implements OnInit {

    // private static readonly SELECTOR = "evcsVehicleBattery-modal";

    @Input() public edge: Edge;
    @Input() public component: EdgeConfig.Component;
    @Input() public evcs: EdgeConfig.Component;

    public thresholds: RangeValue = {
        lower: null,
        upper: null,
    };

    constructor(
        public service: Service,
        public websocket: Websocket,
        public router: Router,
        protected translate: TranslateService,
        public modalCtrl: ModalController,
    ) { }

    ngOnInit() {
        this.thresholds['lower'] = this.component.properties['minVehicleSoc'];
        this.thresholds['upper'] = this.component.properties['maxVehicleSoc'];
    };

    /**
    * Updates the Charge-Mode of the EVCS-Controller.
    *
    * @param event
    */
    updateMode(event: CustomEvent) {
        let oldMode = this.component.properties.mode;
        let newMode: Mode;

        switch (event.detail.value) {
            case 'AUTOMATIC':
                newMode = 'AUTOMATIC';
                break;
            case 'CHARGE':
                newMode = 'CHARGE';
                break;
            case 'DISCHARGE':
                newMode = 'DISCHARGE';
                break;
            case 'OFF':
                newMode = 'OFF';
                break;
        }

        if (this.edge != null) {
            this.edge.updateComponentConfig(this.websocket, this.component.id, [
                { name: 'mode', value: newMode },
            ]).then(() => {
                this.component.properties.mode = newMode;
                this.service.toast(this.translate.instant('General.changeAccepted'), 'success');
            }).catch(reason => {
                this.component.properties.mode = oldMode;
                this.service.toast(this.translate.instant('General.changeFailed') + '\n' + reason.error.message, 'danger');
                console.warn(reason);
            });
        }
    }

    /**
    * Updates the Min-Power of force charging
    *
    * @param event
    */
    updateThresholds() {
        let oldLowerThreshold = this.component.properties['minVehicleSoc'];
        let oldUpperThreshold = this.component.properties['maxVehicleSoc'];

        let newLowerThreshold = this.thresholds['lower'];
        let newUpperThreshold = this.thresholds['upper'];

        // prevents automatic update when no values have changed
        if (this.edge != null && (oldLowerThreshold != newLowerThreshold || oldUpperThreshold != newUpperThreshold)) {
            this.edge.updateComponentConfig(this.websocket, this.component.id, [
                { name: 'minVehicleSoc', value: newLowerThreshold },
                { name: 'maxVehicleSoc', value: newUpperThreshold },
            ]).then(() => {
                this.component.properties['minVehicleSoc'] = newLowerThreshold;
                this.component.properties['maxVehicleSoc'] = newUpperThreshold;
                this.service.toast(this.translate.instant('General.changeAccepted'), 'success');
            }).catch(reason => {
                this.component.properties['minVehicleSoc'] = oldLowerThreshold;
                this.component.properties['maxVehicleSoc'] = oldUpperThreshold;
                this.service.toast(this.translate.instant('General.changeFailed') + '\n' + reason.error.message, 'danger');
                console.warn(reason);
            });
        }
    }

    updateDischargePower(event: CustomEvent) {
        let oldValue = this.component.properties['dischargePower'];
        let newValue = event.detail.value;

        // prevents automatic update when no values have changed
        if (this.edge != null && oldValue != newValue) {
            this.edge.updateComponentConfig(this.websocket, this.component.id, [
                { name: 'dischargePower', value: newValue },
            ]).then(() => {
                this.component.properties['dischargePower'] = newValue;
                this.service.toast(this.translate.instant('General.changeAccepted'), 'success');
            }).catch(reason => {
                this.component.properties['dischargePower'] = oldValue;
                this.service.toast(this.translate.instant('General.changeFailed') + '\n' + reason.error.message, 'danger');
                console.warn(reason);
            });
        }
    }

    updateMinVehicleSoc(event: CustomEvent) {
        let oldValue = this.component.properties['minVehicleSoc'];
        let newValue = event.detail.value;

        // prevents automatic update when no values have changed
        if (this.edge != null && oldValue != newValue) {
            this.edge.updateComponentConfig(this.websocket, this.component.id, [
                { name: 'minVehicleSoc', value: newValue },
            ]).then(() => {
                this.component.properties['minVehicleSoc'] = newValue;
                this.service.toast(this.translate.instant('General.changeAccepted'), 'success');
            }).catch(reason => {
                this.component.properties['minVehicleSoc'] = oldValue;
                this.service.toast(this.translate.instant('General.changeFailed') + '\n' + reason.error.message, 'danger');
                console.warn(reason);
            });
        }
    }
}


