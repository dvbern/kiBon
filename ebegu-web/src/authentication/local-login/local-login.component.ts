/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {HttpClient} from '@angular/common/http';
import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnInit,
    inject
} from '@angular/core';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {TargetState} from '@uirouter/core';
import {Observable} from 'rxjs';
import {TSRole} from '@kibon/shared/model/enums';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

type LocalLoginUser = {
    group: string;
    name: string;
    vorname: string;
    email: string;
    info: string;
    role: TSRole;
};

@Component({
    selector: 'dv-local-login',
    templateUrl: './local-login.component.html',
    styleUrls: ['./local-login.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class LocalLoginComponent implements OnInit {
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly http = inject(HttpClient);

    @Input() public returnTo: TargetState;

    public users$: Observable<LocalLoginUser[]>;
    public createUserResponse$: Observable<LocalLoginUser[]>;
    private groupSortKey: Record<string, number> = {
        MANDANT: 0,
        KANTON: 1,
        TRAEGERSCHAFT: 2,
        INSTITUTION: 3,
        SOZIALDIENST: 4,
        GESUCHSTELLER: 5,
        GEMEINDE: 6
    };
    private roleSortKey: Record<TSRole, number> = {
        SUPER_ADMIN: 0,

        ADMIN_MANDANT: 1,
        SACHBEARBEITER_MANDANT: 2,

        ADMIN_INSTITUTION: 3,
        SACHBEARBEITER_INSTITUTION: 4,

        ADMIN_SOZIALDIENST: 5,
        SACHBEARBEITER_SOZIALDIENST: 6,

        ADMIN_TRAEGERSCHAFT: 7,
        SACHBEARBEITER_TRAEGERSCHAFT: 8,

        ADMIN_GEMEINDE: 9,
        SACHBEARBEITER_GEMEINDE: 10,

        ADMIN_FERIENBETREUUNG: 11,
        SACHBEARBEITER_FERIENBETREUUNG: 12,

        ADMIN_BG: 13,
        SACHBEARBEITER_BG: 14,

        ADMIN_TS: 15,
        SACHBEARBEITER_TS: 16,

        JURIST: 17,
        REVISOR: 18,
        STEUERAMT: 19,

        GESUCHSTELLER: 20,
        ANONYMOUS: 21
    };

    ngOnInit(): void {
        this.users$ = this.http.get<LocalLoginUser[]>(
            `${CONSTANTS.REST_API}locallogin/listUsers`
        );
    }

    public logIn(email: string): void {
        this.authServiceRS.initLoginReturnToTargetState(this.returnTo, email);
    }

    public createUser(): void {
        this.createUserResponse$ = this.http.get<LocalLoginUser[]>(
            `${CONSTANTS.REST_API}locallogin/createUsers`
        );
    }

    groupByGroup(users: LocalLoginUser[]): [string, LocalLoginUser[]][] {
        if (!users) {
            return Object.entries({});
        }
        const groupedByGroup = users.reduce(
            (acc: Record<string, LocalLoginUser[]>, user) => {
                if (!(user.group in acc)) {
                    acc[user.group] = [];
                }
                acc[user.group].push(user);
                return acc;
            },
            {}
        );
        return Object.entries(groupedByGroup).sort(([groupA], [groupB]) => {
            return this.groupSortKey[groupA] - this.groupSortKey[groupB];
        });
    }

    public sortUsersByRole(users: LocalLoginUser[]): LocalLoginUser[] {
        return users.sort((a, b) => {
            return this.roleSortKey[a.role] - this.roleSortKey[b.role];
        });
    }
}
