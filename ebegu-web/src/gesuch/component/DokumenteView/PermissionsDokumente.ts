import {TSRole} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

export enum PermissionDokumente {
    DOKUMENTE_ERNEUERN = 'DOKUMENTE_ERNEUERN'
}

export const PERMISSIONS_DOKUMENTE: {
    [k in PermissionDokumente]: Array<TSRole>;
} = {
    [PermissionDokumente.DOKUMENTE_ERNEUERN]: [
        ...TSRoleUtil.getSuperAdminRoles(),
        ...TSRoleUtil.getGemeindeOrBGOrTSRoles(),
        ...TSRoleUtil.getGesuchstellerSozialdienstRolle()
    ]
};
