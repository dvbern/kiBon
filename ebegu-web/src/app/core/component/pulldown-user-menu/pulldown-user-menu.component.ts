/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    ViewEncapsulation,
    inject
} from '@angular/core';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {StateService} from '@uirouter/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-pulldown-user-menu',
    templateUrl: './pulldown-user-menu.component.html',
    styleUrls: ['./pulldown-user-menu.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class PulldownUserMenuComponent implements OnInit {
    private readonly authService = inject(AuthServiceRS);
    private readonly applicationPropertyRS = inject(
        SharedUtilApplicationPropertyRsService
    );
    private readonly state = inject(StateService);

    public multimandantAktiv: boolean;
    public frenchEnabled: boolean;
    public testfaelleEnabled: boolean;

    public ngOnInit(): void {
        this.initMandantSwitch();
        this.initFrenchEnabled();
        this.initTestFaelleEnabled();
    }

    public getFullName(): Observable<string | undefined> {
        return this.authService.principal$.pipe(
            map(principal => principal?.getFullName())
        );
    }

    public getSuperAdminRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getSuperAdminRoles();
    }

    public getAllAdministratorRevisorRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllAdministratorRevisorRole();
    }

    public getInstitutionProfilRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getInstitutionProfilRoles();
    }

    public getMandantRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getMandantRoles();
    }

    public getTraegerschaftId(): Observable<string | undefined> {
        return this.authService.principal$.pipe(
            map(principal => principal?.currentBerechtigung?.traegerschaft?.id)
        );
    }

    public getAdministratorBgTsGemeindeOrMandantRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAdministratorBgTsGemeindeOrMandantRole();
    }

    public getAllRolesForSozialdienst(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesForSozialdienst();
    }

    private initMandantSwitch(): void {
        this.applicationPropertyRS.isMultimandantEnabled().subscribe(res => {
            this.multimandantAktiv = res;
        });
    }

    private initFrenchEnabled(): void {
        this.applicationPropertyRS.getFrenchEnabled().subscribe(res => {
            this.frenchEnabled = res;
        });
    }

    private initTestFaelleEnabled() {
        this.applicationPropertyRS.isTestfaelleEnabled().subscribe(res => {
            this.testfaelleEnabled = res;
        });
    }

    public isDevMode() {
        return this.applicationPropertyRS.isDevMode();
    }

    public logout(): void {
        this.authService.initLogout();
    }
}
