import {TSRole} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

export enum PermissionDokumente {
    DOKUMENTE_UEBERNEHMEN = 'DOKUMENTE_UEBERNEHMEN'
}

export const PERMISSIONS_DOKUMENTE: {
    [k in PermissionDokumente]: Array<TSRole>;
} = {
    [PermissionDokumente.DOKUMENTE_UEBERNEHMEN]: [
        ...TSRoleUtil.getSuperAdminRoles(),
        ...TSRoleUtil.getGemeindeOrBGOrTSRoles(),
        ...TSRoleUtil.getGesuchstellerSozialdienstRolle()
    ]
};
