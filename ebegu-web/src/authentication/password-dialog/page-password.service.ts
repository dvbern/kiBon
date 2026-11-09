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
import {Injectable, NgZone, inject} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {CONSTANTS} from '@models/constants';
import {StateService} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap, tap} from 'rxjs/operators';
import {PasswordDialog} from './password-dialog';

/**
 * Password protection of admin pages (e.g. /admin, gesuchsperiode).
 */
@Injectable({providedIn: 'root'})
export class PagePasswordService {
    private readonly http = inject(HttpClient);
    private readonly dialog = inject(MatDialog);
    private readonly zone = inject(NgZone);
    private readonly $state = inject(StateService);

    /**
     * Emits once the page is unlocked. On cancel or a wrong password, navigates to redirectState.
     * Unsubscribing (e.g. with takeUntilDestroyed) closes an open password dialog.
     */
    public unlock$(redirectState: string): Observable<void> {
        return this.http
            .get<boolean>(`${CONSTANTS.REST_API}admin/pagePassword/required`)
            .pipe(
                // on error, ask for the password rather than leaving the page open
                catchError(() => of(true)),
                switchMap(passwordRequired =>
                    passwordRequired ? this.openPasswordDialog() : of(true)
                ),
                tap(valid => {
                    if (!valid) {
                        this.redirect(redirectState);
                    }
                }),
                filter(valid => valid),
                map((): void => undefined)
            );
    }

    private openPasswordDialog(): Observable<boolean> {
        return new Observable<string | null>(subscriber => {
            // zone.run makes this work even if the transition started outside Angular's zone
            const dialogRef = this.zone.run(() =>
                this.dialog.open(PasswordDialog, {
                    disableClose: true,
                    width: '50rem',
                    backdropClass: 'blur-backdrop',
                    closeOnNavigation: false
                })
            );
            const subscription = dialogRef.afterClosed().subscribe(subscriber);

            // MatDialog is provided in root and closeOnNavigation is disabled,
            // so the dialog would outlive the page without this
            return () => {
                subscription.unsubscribe();
                dialogRef.close();
            };
        }).pipe(
            switchMap(password =>
                password ? this.verifyPassword$(password) : of(false)
            )
        );
    }

    private verifyPassword$(password: string): Observable<boolean> {
        return this.http
            .post<boolean>(`${CONSTANTS.REST_API}admin/pagePassword/verify`, {
                password
            })
            .pipe(catchError(() => of(false)));
    }

    private redirect(redirectState: string): void {
        this.$state.go(redirectState, {}, {location: 'replace'}).catch(err => {
            console.error('Redirect after cancel failed', err);
            window.location.assign('/');
        });
    }
}
