/// <reference path="../../../typings/browser.d.ts" />
module ebeguWeb.API {

    export enum TSFamilienstatus {
        ALLEINERZIEHEND = <any>"ALLEINERZIEHEND",
        VERHEIRATET = <any>"VERHEIRATET",
        KONKUBINAT = <any>"KONKUBINAT ",
        LAENGER_FUENF_JAHRE = <any>"LAENGER_FUENF_JAHRE",
        WENIGER_FUENF_JAHRE = <any>"WENIGER_FUENF_JAHRE",
        PARTNERSCHAFT = <any>"PARTNERSCHAFT"
    }

    export function getTSFamilienstatusValues(): Array<TSFamilienstatus> {
        return [
            TSFamilienstatus.ALLEINERZIEHEND,
            TSFamilienstatus.VERHEIRATET,
            TSFamilienstatus.KONKUBINAT,
            TSFamilienstatus.LAENGER_FUENF_JAHRE,
            TSFamilienstatus.WENIGER_FUENF_JAHRE,
            TSFamilienstatus.PARTNERSCHAFT
        ];
    }


}
