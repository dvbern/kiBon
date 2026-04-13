import {Directive, inject} from '@angular/core';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../models/enums/TSRole';
import {TSRoleUtil} from '../utils/TSRoleUtil';

@Directive()
export class AbstractAdminViewX {
    authServiceRS = inject(AuthServiceRS);

    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;

    public isReadonly(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getJAAdministratorRoles()
        );
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }
}
