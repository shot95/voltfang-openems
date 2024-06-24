import { formatNumber } from '@angular/common';
import { Component } from '@angular/core';
import { ChannelAddress, CurrentData, EdgeConfig, Utils } from '../../../../shared/shared';
import { AbstractFlatWidget } from 'src/app/shared/genericComponents/flat/abstract-flat-widget';
import { EvcsVehicleBatteryModalComponent } from './modal/modal.component';

@Component({
    selector: 'EvcsVehicleBattery',
    templateUrl: './EvcsVehicleBattery.component.html',
})
export class EvcsVehicleBatteryComponent extends AbstractFlatWidget {

    public mode: Mode = null;
    public evcsComponent: EdgeConfig.Component = null;
    public evcsSoc: number;
    public evcsStatus: string;
    public evcsChargePowerLimit: number = null;
    public isConnectionSuccessful: boolean = false;
    public isEnergySinceBeginningAllowed: boolean = false;
    public isCharging: boolean = false;
    public isDischarging: boolean = false;

    public readonly CONVERT_TO_KILO_WATTHOURS = Utils.CONVERT_TO_KILO_WATTHOURS;
    public readonly CONVERT_WATT_TO_KILOWATT = Utils.CONVERT_WATT_TO_KILOWATT;
    public readonly CONVERT_TO_PERCENT = Utils.CONVERT_TO_PERCENT;

    protected override getChannelAddresses() {
        this.evcsComponent = this.config.getComponent(this.component.properties["evcs.id"]);
        return [
            // controller channels
            new ChannelAddress(this.component.id, '_PropertyMode'),

            // evcs channels
            new ChannelAddress(this.evcsComponent.id, 'ActivePower'), // DISCHARGE power
            new ChannelAddress(this.evcsComponent.id, 'ChargePower'), // CHARGE power
            new ChannelAddress(this.evcsComponent.id, 'Phases'),
            new ChannelAddress(this.evcsComponent.id, 'Plug'),
            new ChannelAddress(this.evcsComponent.id, 'Status'),
            new ChannelAddress(this.evcsComponent.id, 'State'),
            new ChannelAddress(this.evcsComponent.id, 'EnergySession'),
            new ChannelAddress(this.evcsComponent.id, 'SetChargePowerLimit'),
            new ChannelAddress(this.evcsComponent.id, 'Soc'),
        ];
    }

    protected override onCurrentData(currentData: CurrentData) {
        this.mode = this.component.properties["mode"];
        let chargePowerLimit = currentData.allComponents[this.evcsComponent.id + '/SetChargePowerLimit'];
        this.evcsChargePowerLimit = (chargePowerLimit != undefined) ? chargePowerLimit : null;
        let evcsState = currentData.allComponents[this.evcsComponent.id + '/State'];
        this.isConnectionSuccessful = (evcsState != undefined && evcsState != 3) ? true : false;
        this.evcsStatus = this.getState(currentData.allComponents[this.evcsComponent.id + "/Status"], currentData.allComponents[this.evcsComponent.id + "/Plug"]);
        let chargePower = currentData.allComponents[this.evcsComponent.id + 'ChargePower'];
        this.isCharging = chargePower ? chargePower != 0 : false;
        let dischargePower = currentData.allComponents[this.evcsComponent.id + 'ActivePower'];
        this.isDischarging = dischargePower ? dischargePower != 0 : false;
        // Check if Energy since beginning is allowed
        if (chargePower > 0 || currentData.allComponents[this.evcsComponent.id + '/Status'] == 2 || currentData.allComponents[this.evcsComponent.id + '/Status'] == 7) {
            this.isEnergySinceBeginningAllowed = true;
        }

        this.evcsSoc = currentData.allComponents[this.evcsComponent.id + '/Soc'];
    }

    async presentModal() {
        const modal = await this.modalController.create({
            component: EvcsVehicleBatteryModalComponent,
            componentProps: {
                component: this.component,
                edge: this.edge,
                evcs: this.evcsComponent,
            },
        });
        return await modal.present();
    }

    /**
  * Use 'convertChargePower' to convert/map a value
  *
  * @param value takes @Input value or channelAddress for chargePower
  * @returns value
  */
    public convertChargePower = (value: any): string => {
        return this.convertPower(Utils.multiplySafely(value, -1), true);
    };

    /**
     * Use 'convertDischargePower' to convert/map a value
     *
     * @param value takes @Input value or channelAddress for dischargePower
     * @returns value
     */
    public convertDischargePower = (value: any): string => {
        return this.convertPower(value);
    };

    /**
 * Use 'convertPower' to check whether 'charge/discharge' and to be only showed when not negative
 *
 * @param value takes passed value when called
 * @returns only positive and 0
 */
    public convertPower(value: number, isCharge?: boolean) {
        if (value == null) {
            return '-';
        }

        let thisValue: number = (value / 1000);

        // Round thisValue to Integer when decimal place equals 0
        if (thisValue > 0) {
            return formatNumber(thisValue, 'de', '1.0-1') + " kW"; // TODO get locale dynamically

        } else if (thisValue == 0 && isCharge) {
            // if thisValue is 0, then show only when charge and not discharge
            return '0 kW';

        } else {
            return '-';
        }
    }

    /**
 * Returns the state of the EVCS
 *
 * @param state
 * @param plug
 *
 */
    private getState(state: number, plug: number): string {
        if (this.mode === 'OFF') {
            return this.translate.instant('Edge.Index.Widgets.EVCS.chargingStationDeactivated');
        }
        let chargeState = state;
        let chargePlug = plug;

        if (chargePlug == null) {
            if (chargeState == null) {
                return this.translate.instant('Edge.Index.Widgets.EVCS.notCharging');
            }
        } else if (chargePlug != ChargePlug.PLUGGED_ON_EVCS_AND_ON_EV_AND_LOCKED) {
            return this.translate.instant('Edge.Index.Widgets.EVCS.cableNotConnected');
        }
        switch (chargeState) {
            case ChargeState.STARTING:
                return this.translate.instant('Edge.Index.Widgets.EVCS.starting');
            case ChargeState.UNDEFINED:
            case ChargeState.ERROR:
                return this.translate.instant('Edge.Index.Widgets.EVCS.error');
            case ChargeState.READY_FOR_CHARGING:
                return this.translate.instant('Edge.Index.Widgets.EVCS.readyForCharging');
            case ChargeState.NOT_READY_FOR_CHARGING:
                return this.translate.instant('Edge.Index.Widgets.EVCS.notReadyForCharging');
            case ChargeState.AUTHORIZATION_REJECTED:
                return this.translate.instant('Edge.Index.Widgets.EVCS.notCharging');
            case ChargeState.CHARGING:
                return this.translate.instant('Edge.Index.Widgets.EVCS.charging');
            case ChargeState.ENERGY_LIMIT_REACHED:
                return this.translate.instant('Edge.Index.Widgets.EVCS.chargeLimitReached');
            case ChargeState.CHARGING_FINISHED:
                return this.translate.instant('Edge.Index.Widgets.EVCS.carFull');
        }
    }
}

type Mode = 'AUTOMATIC' | 'CHARGE' | 'DISCHARGE' | 'OFF';

enum ChargeState {
    UNDEFINED = -1,           //Undefined
    STARTING,                 //Starting
    NOT_READY_FOR_CHARGING,   //Not ready for Charging e.g. unplugged, X1 or "ena" not enabled, RFID not enabled,...
    READY_FOR_CHARGING,       //Ready for Charging waiting for EV charging request
    CHARGING,                 //Charging
    ERROR,                    //Error
    AUTHORIZATION_REJECTED,   //Authorization rejected
    ENERGY_LIMIT_REACHED,     //Energy limit reached
    CHARGING_FINISHED         //Charging has finished
}

enum ChargePlug {
    UNDEFINED = -1,                           //Undefined
    UNPLUGGED,                                //Unplugged
    PLUGGED_ON_EVCS,                          //Plugged on EVCS
    PLUGGED_ON_EVCS_AND_LOCKED = 3,           //Plugged on EVCS and locked
    PLUGGED_ON_EVCS_AND_ON_EV = 5,            //Plugged on EVCS and on EV
    PLUGGED_ON_EVCS_AND_ON_EV_AND_LOCKED = 7  //Plugged on EVCS and on EV and locked
}
