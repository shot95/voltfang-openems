import { Environment } from "src/environments";
import { theme } from "src/environments/theme";

export const environment: Environment = {
    ...theme, ...{

        backend: 'OpenEMS Backend',
        url: "wss://" + location.hostname + "/ems/openems-backend-ui2",

        production: true,
        debugMode: false,
    },
};
