/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

import {HttpClient} from '@angular/common/http';
import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnInit,
    inject,
    ChangeDetectorRef,
    DestroyRef,
    NgZone
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatDialog} from '@angular/material/dialog';
import {CONSTANTS} from '@models/constants';
import {StateService, TargetState} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {TSRole} from '../../models/enums/TSRole';
import {PasswordDialog} from '../password-dialog/password-dialog';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';
import {LocalLoginLocalStorageUtil} from './LocalLoginLocalStorageUtil';

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
    styleUrls: ['./local-login.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class LocalLoginComponent implements OnInit {
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly http = inject(HttpClient);
    private readonly dialog = inject(MatDialog);
    private readonly stateService = inject(StateService);
    private readonly zone = inject(NgZone);
    private readonly cdr = inject(ChangeDetectorRef);
    private readonly destroyRef = inject(DestroyRef);

    @Input() public returnTo: TargetState;

    public users$: Observable<LocalLoginUser[]>;
    public createUserResponse$: Observable<LocalLoginUser[]>;
    public unlocked = false;
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
        // password has been entered recently, see LocalLoginLocalStorageUtil
        if (LocalLoginLocalStorageUtil.isUnlocked()) {
            this.unlock();
            return;
        }
        this.http
            .get<boolean>(`${CONSTANTS.REST_API}locallogin/passwordRequired`)
            .pipe(
                takeUntilDestroyed(this.destroyRef),
                // on error, ask for the password rather than leaving the page open
                catchError(() => of(true))
            )
            .subscribe(passwordRequired => {
                if (passwordRequired) {
                    this.openPasswordDialog();
                    return;
                }
                this.unlock();
            });
    }

    private openPasswordDialog(): void {
        // zone.run makes this work even if the transition started outside Angular's zone
        this.zone.run(() => {
            const dialogRef = this.dialog.open(PasswordDialog, {
                disableClose: true,
                width: '50rem',
                backdropClass: 'blur-backdrop',
                closeOnNavigation: false
            });

            dialogRef
                .afterClosed()
                .pipe(
                    takeUntilDestroyed(this.destroyRef),
                    switchMap(password =>
                        password ? this.verifyPassword(password) : of(false)
                    )
                )
                .subscribe(valid => {
                    if (valid) {
                        LocalLoginLocalStorageUtil.setUnlocked();
                        this.unlock();
                        return;
                    }
                    this.stateService
                        .go('onboarding.anmeldung', {}, {location: 'replace'})
                        .catch(err => {
                            console.error('Redirect after cancel failed', err);
                            window.location.assign('/');
                        });
                });

            // MatDialog is provided in root and closeOnNavigation is disabled,
            // so the dialog would outlive this component without this
            this.destroyRef.onDestroy(() => dialogRef.close());
        });
    }

    private verifyPassword(password: string): Observable<boolean> {
        return this.http
            .post<boolean>(`${CONSTANTS.REST_API}locallogin/verifyPassword`, {
                password
            })
            .pipe(catchError(() => of(false)));
    }

    private unlock(): void {
        this.unlocked = true;
        this.users$ = this.http.get<LocalLoginUser[]>(
            `${CONSTANTS.REST_API}locallogin/listUsers`
        );
        this.cdr.markForCheck();
    }

    public logIn(email: string): void {
        if (!this.unlocked) {
            return;
        }
        this.authServiceRS.initLoginReturnToTargetState(this.returnTo, email);
    }

    public createUser(): void {
        if (!this.unlocked) {
            return;
        }
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
        if (!this.unlocked) {
            return [];
        }
        return users.sort((a, b) => {
            return this.roleSortKey[a.role] - this.roleSortKey[b.role];
        });
    }
}
