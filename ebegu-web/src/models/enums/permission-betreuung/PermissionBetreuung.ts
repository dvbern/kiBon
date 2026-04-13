import {TSRole} from '../TSRole';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

export enum PermissionBetreuung {
    TABELLARISCHE_BETREUUNG_MASKE = 'TABELLARISCHE_BETREUUNG_MASKE'
}

export const PERMISSIONS_BETREUUNG: {
    [k in PermissionBetreuung]: Array<TSRole>;
} = {
    TABELLARISCHE_BETREUUNG_MASKE: [
        ...TSRoleUtil.getInstitutionOnlyRoles(),
        ...TSRoleUtil.getTraegerschaftOnlyRoles()
    ]
};
