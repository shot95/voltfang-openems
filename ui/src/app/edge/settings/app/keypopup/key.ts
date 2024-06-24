import { App } from "./app";

export type Key = {
    keyId: string
    keyDescription?: string // oEMS
    bundles?: (App[])[]
}
