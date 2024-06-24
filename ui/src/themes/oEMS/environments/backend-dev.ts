import { Environment } from "src/environments";
import { theme } from "src/environments/theme";

export const environment: Environment = {
    ...theme, ...{

        backend: 'OpenEMS Backend',
        url: "wss://" + "dev.oems.energy" + "/ems/openems-backend-ui2",

        production: false,
        debugMode: true,
    },
};
