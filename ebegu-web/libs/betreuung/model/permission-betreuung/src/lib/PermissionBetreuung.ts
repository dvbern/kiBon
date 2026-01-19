import {TSRole} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../../../../src/utils/TSRoleUtil';

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
