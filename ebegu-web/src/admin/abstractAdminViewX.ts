import {Directive, inject} from '@angular/core';
import {TSGesuchsperiode} from '@kibon/shared/model/entity';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';
import {TSGesuchsperiodeStatus, TSRole} from '@kibon/shared/model/enums';
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

    public isAnyAdminRole(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorRoles()
        );
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }

    public periodenParamsEditableForPeriode(
        gesuchsperiode: TSGesuchsperiode
    ): boolean {
        if (gesuchsperiode?.status) {
            // Fuer SuperAdmin immer auch editierbar, wenn AKTIV oder INAKTIV, sonst nur ENTWURF
            if (TSGesuchsperiodeStatus.GESCHLOSSEN === gesuchsperiode.status) {
                return false;
            }
            if (
                this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles())
            ) {
                return true;
            }
            return TSGesuchsperiodeStatus.ENTWURF === gesuchsperiode.status;
        }
        return false;
    }
}
