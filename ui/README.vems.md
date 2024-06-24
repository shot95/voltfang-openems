# OpenEMS UI

This project was generated with [angular-cli](https://github.com/angular/angular-cli).

## Theme OpenEMS

- OpenEMS Edge - expects a Edge *Controller.Api.Websocket* on default port `8075`

   - Serve to port `4200`
   
      `ng serve`
      `ng serve -o -c openems-edge-dev`
      `ng serve -o -c voltfang-edge-dev`

   - Build Development

      `ng build`

      `ng build -c "openems,openems-edge-dev"`
      `ng build -c "voltfang, voltfang-edge-dev"`

   - Build Production

      `ng build -c "openems,openems-edge-prod,prod"`
      `ng build -c "voltfang, voltfang-edge-dev"`

- OpenEMS Backend

   - Serve to port `4200` - expects a Backend *Ui.Websocket* on default port `8082`
   
      `OpenEMS            | ng serve -o -c openems-backend-local --host=127.0.0.1`

      `voltfang           | ng serve -o -c voltfang-backend-local --host=127.0.0.1`

   - Serve to port `4200` - connects to *Ui.Websocket* of DEV-Backend (dev.oems.energy)

      `OpenEMS            | ng serve -o -c openems-backend-dev`

      `voltfang           | ng serve -o -c voltfang-backend-dev`

   - Build Development - ONLY for debugging purposes!

      `OpenEMS            | ng build -c "openems,openems-backend-dev" --base-href /ems/`

      `voltfang           | ng build -c "voltfang,voltfang-backend-dev" --base-href /ems/`

   - Build Production - for the deployment on DEV and PROD environment

      `OpenEMS            | ng build -c "openems,openems-backend-prod,prod"  --base-href /ems/`

      `voltfang           | ng build -c "voltfang,voltfang-backend-prod,prod" --base-href /ems/`

## Further help

#### Creating a Theme

- Create new folder under `/src/themes`
   - Files in `root` will be copied to `/` of the OpenEMS UI
   - `scss/variables.scss` will be used for styling
   - `environments/*.ts` define settings for Backend/Edge and development/production environments
- Generate contents of `root` folder using https://realfavicongenerator.net Place them in `root` subdirectory
- Add entries in `angular.json`

#### i18n - internationalization

Translation is based on [ngx-translate](https://github.com/ngx-translate). The language can be changed at runtime in the "About UI" dialog.

##### In HTML template use:

`<p translate>General.storageSystem</p>`

* add attribute 'translate'
* content of the tag is the path to translation in [translate.ts](app/shared/translate.ts) file

##### In typescript code use:
```
import { TranslateService } from '@ngx-translate/core';
constructor(translate: TranslateService) {}
this.translate.instant('General.storageSystem')
```

#### Subscribe
For "subscribe" please follow this: https://stackoverflow.com/questions/38008334/angular-rxjs-when-should-i-unsubscribe-from-subscription
```
import { Subject } from 'rxjs/Subject';
import { takeUntil } from 'rxjs/operators';
private stopOnDestroy: Subject<void> = new Subject<void>();
ngOnInit() {
    /*subject*/.pipe(takeUntil(this.stopOnDestroy)).subscribe(/*variable*/ => {
        ...
    });
}
ngOnDestroy() {
    this.stopOnDestroy.next();
    this.stopOnDestroy.complete();
}
```
