import {TSRole} from '../../../models/enums/TSRole';

export enum PermissionLastenausgleichTagesschule {
    LOAD_VERLAUF = 'LOAD_VERLAUF'
}

export const PERMISSION_LATS: {
    [k in PermissionLastenausgleichTagesschule]: Array<TSRole>;
} = {
    LOAD_VERLAUF: [
        TSRole.SUPER_ADMIN,
        TSRole.ADMIN_MANDANT,
        TSRole.SACHBEARBEITER_MANDANT,
        TSRole.ADMIN_GEMEINDE,
        TSRole.SACHBEARBEITER_GEMEINDE,
        TSRole.ADMIN_TS,
        TSRole.SACHBEARBEITER_TS
    ]
};
