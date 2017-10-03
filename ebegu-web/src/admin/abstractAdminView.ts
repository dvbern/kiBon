import {TSRole} from '../models/enums/TSRole';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import AuthServiceRS from '../authentication/service/AuthServiceRS.rest';

export default class AbstractAdminViewController {

    TSRole: any;
    TSRoleUtil: any;

    constructor(public authServiceRS: AuthServiceRS) {
        this.TSRole = TSRole;
        this.TSRoleUtil = TSRoleUtil;
    }

    $onInit() {
    }

    public isReadonly(): boolean {
        return !this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorRoles());
    }
}
