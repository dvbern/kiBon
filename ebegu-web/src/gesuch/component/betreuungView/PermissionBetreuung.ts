import {TSRole} from '../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

export enum PermissionBetreuung {
    TABELLARISCHE_BETREUUNG_MASKE = 'TABELLARISCHE_BETREUUNG_MASKE',
    ABRECHNUNG_GUTSCHEINE = 'ABRECHNUNG_GUTSCHEINE'
}

export const PERMISSIONS_BETREUUNG: {
    [k in PermissionBetreuung]: ReadonlyArray<TSRole>;
} = {
    [PermissionBetreuung.TABELLARISCHE_BETREUUNG_MASKE]:
        TSRoleUtil.getInstitutionOnlyRoles().concat(
            TSRoleUtil.getTraegerschaftOnlyRoles()
        ),
    [PermissionBetreuung.ABRECHNUNG_GUTSCHEINE]:
        TSRoleUtil.getAllRolesButTraegerschaftInstitution()
};
