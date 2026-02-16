/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {CONSTANTS} from '@kibon/shared/model/constants';
import {TSRole} from '@kibon/shared/model/enums';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import * as Sentry from '@sentry/browser';
import {StateService, TargetState} from '@uirouter/core';
import * as angular from 'angular';

import {Observable, ReplaySubject} from 'rxjs';
import {Permission} from '../../app/authorisation/Permission';
import {PERMISSIONS} from '../../app/authorisation/Permissions';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSBenutzer} from '../../models/TSBenutzer';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {AuthLifeCycleService} from './authLifeCycle.service';
import {LogoutResponse} from './LogoutResponse';
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;

const LOG = LogFactory.createLog('AuthServiceRS');

export class AuthServiceRS {
    public static $inject = [
        '$http',
        '$q',
        '$state',
        'EbeguRestUtil',
        'AuthLifeCycleService',
        'SharedUtilApplicationPropertyRsService'
    ];

    private principal?: TSBenutzer;

    // We are using a ReplaySubject, because it blocks the authenticationHook until the first value is emitted.
    // Thus the session restoration from the cookie is completed before the authenticationHook checks for
    // authentication.
    private readonly principalSubject$ = new ReplaySubject<TSBenutzer | null>(
        1
    );

    private _principal$: Observable<TSBenutzer | null> =
        this.principalSubject$.asObservable();
    private angebotTSEnabled: boolean;

    public constructor(
        private readonly $http: IHttpService,
        private readonly $q: IQService,
        private readonly $state: StateService,
        private readonly ebeguRestUtil: EbeguRestUtil,
        private readonly authLifeCycleService: AuthLifeCycleService,
        private readonly applicationPropertyRS: SharedUtilApplicationPropertyRsService
    ) {
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(res => {
                this.angebotTSEnabled = res.angebotTSActivated;
            });
    }

    // Use the observable, when the state must be updated automatically, when the principal changes.
    // e.g. printing the name of the current user
    public get principal$(): Observable<TSBenutzer | null> {
        return this._principal$;
    }

    public set principal$(value$: Observable<TSBenutzer | null>) {
        this._principal$ = value$;
    }

    public getPrincipal(): TSBenutzer | undefined {
        return this.principal;
    }

    public getPrincipalRole(): TSRole | undefined {
        if (this.principal) {
            return this.principal.getCurrentRole();
        }
        return undefined;
    }

    public initWithCookie(): IPromise<TSBenutzer> {
        LOG.debug('initWithCookie');

        try {
            // we take the complete user from Server and store it in principal
            return this.reloadUser();
        } catch (e) {
            LOG.error('cookie decoding failed', e);
            this.clearPrincipal();
            return this.$q.reject(TSAuthEvent.NOT_AUTHENTICATED);
        }
    }

    /**
     * helper that checks if a link redirects to be-login by checking if it
     * ends with .be.ch (to include testsystems)
     *
     * @param link to check
     */
    private isBeLoginLink(link: string): boolean {
        try {
            if (link) {
                const parsedURL = new URL(link);
                return (
                    parsedURL.hostname && parsedURL.hostname.endsWith('.be.ch')
                );
            }
        } catch {
            return false;
        }
        return false;
    }

    public reloadCurrentUser(): IPromise<TSBenutzer> {
        return this.reloadUser();
    }

    private reloadUser(): IPromise<TSBenutzer> {
        return this.loadPrincipal().then(user => {
            this.principal = user;
            this.principalSubject$.next(user);
            this.setPrincipalInSentryUserContext();

            this.authLifeCycleService.changeAuthStatus(
                TSAuthEvent.LOGIN_SUCCESS,
                'logged in'
            );

            return user;
        });
    }

    private loadPrincipal(): IPromise<TSBenutzer> {
        return this.$http
            .get(`${CONSTANTS.REST_API}auth/authenticated-user`)
            .then(response => response.data)
            .then(restBenutzer =>
                this.ebeguRestUtil.parseUser(new TSBenutzer(), restBenutzer)
            );
    }

    private setPrincipalInSentryUserContext(): void {
        Sentry.setUser({
            id: this.principal.username,
            email: this.principal.email,
            role: this.principal.getCurrentRole(),
            status: this.principal.status,
            mandant: this.principal.mandant
                ? this.principal.mandant.name
                : null,
            traegerschaft: this.principal.currentBerechtigung.traegerschaft
                ? this.principal.currentBerechtigung.traegerschaft.name
                : null,
            institution: this.principal.currentBerechtigung.institution
                ? this.principal.currentBerechtigung.institution.name
                : null
        });
    }

    public clearPrincipal(): void {
        this.principal = undefined;
        this.principalSubject$.next(null);
    }

    public initLoginReturnToTargetState(
        returnTo: TargetState,
        email?: string
    ): void {
        const relayUrl = this.$state.href(
            returnTo.$state(),
            returnTo.params(),
            {absolute: true}
        );

        this.initLogin(relayUrl, email);
    }

    public initLogin(returnPath?: string, email?: string): void {
        const queryParams = new URLSearchParams({
            return_path: returnPath ?? window.location.hash.slice(1)
        });
        if (email) {
            queryParams.append('login_hint', email);
        }

        const url = new URL(
            `${CONSTANTS.REST_API}auth/login?${queryParams}`,
            window.location.origin
        );
        window.location.assign(url);
    }

    public async initLogout(): Promise<LogoutResponse> {
        const logoutApiUrl = new URL(
            `${CONSTANTS.REST_API}auth/logout`,
            window.location.origin
        );

        try {
            const resp = await this.$http.get<LogoutResponse>(
                logoutApiUrl.href,
                {
                    withCredentials: true,
                    headers: {Accept: 'application/json'}
                }
            );

            const logoutSuccess: boolean = resp.data?.logoutSuccess ?? false;
            const useDefaultLogoutRedirect: boolean =
                resp.data?.useDefaultLogoutRedirect ?? false;
            const logoutRedirect =
                resp.data?.logoutRedirect ?? window.location.origin;

            if (logoutSuccess) {
                if (useDefaultLogoutRedirect) {
                    window.location.assign(window.location.origin);
                } else {
                    window.location.assign(logoutRedirect);
                }
            }

            return resp.data;
        } catch (err: any) {
            console.error('Logout failed', err);

            const message =
                `The logout request failed because of an error. ` +
                `The error message was: ${err?.message}`;

            return new LogoutResponse('', false, message);
        }
    }

    public isLoggedIn(): boolean {
        return !!this.getPrincipal();
    }

    public initConnectGSZPV(relayPath: string): IPromise<string> {
        return this.initSSO(
            `${CONSTANTS.REST_API}auth/init-connect-gs-zpv`,
            relayPath
        );
    }

    private initSSO(path: string, relayPath: string): IPromise<string> {
        return this.$http
            .get(path, {params: {relayPath}})
            .then((res: any) => res.data);
    }

    /**
     * Gibt true zurueck, wenn der eingelogte Benutzer die gegebene Role hat.
     * Fuer undefined Werte wird immer false zurueckgegeben.
     */
    public isRole(role: TSRole): boolean {
        if (role && this.principal) {
            return this.principal.hasRole(role);
        }
        return false;
    }

    /**
     * gibt true zurueck wenn der aktuelle Benutzer eine der uebergebenen
     * Rollen innehat
     */
    public isOneOfRoles(roles: ReadonlyArray<TSRole>): boolean {
        if (roles !== undefined && roles !== null && this.principal) {
            return this.principal.hasOneOfRoles(roles);
        }
        return false;
    }

    public getVisibleRolesForPrincipal(): ReadonlyArray<TSRole> {
        if (EbeguUtil.isNullOrUndefined(this.getPrincipal())) {
            return [];
        }
        const isTagesschuleEnabled = this.angebotTSEnabled;
        switch (this.getPrincipalRole()) {
            case TSRole.SUPER_ADMIN:
                return TSRoleUtil.getAllRolesButAnonymous();

            case TSRole.ADMIN_INSTITUTION:
                return PERMISSIONS[Permission.ROLE_INSTITUTION];

            case TSRole.ADMIN_TRAEGERSCHAFT:
                return PERMISSIONS[Permission.ROLE_INSTITUTION].concat(
                    PERMISSIONS[Permission.ROLE_TRAEGERSCHAFT]
                );

            case TSRole.ADMIN_MANDANT:
                return PERMISSIONS[Permission.ROLE_MANDANT];

            case TSRole.ADMIN_BG:
                return isTagesschuleEnabled
                    ? PERMISSIONS[Permission.BENUTZER_EINLADEN_AS_GEMEINDE]
                    : PERMISSIONS[Permission.ROLE_BG];

            case TSRole.ADMIN_TS:
                return isTagesschuleEnabled
                    ? PERMISSIONS[Permission.BENUTZER_EINLADEN_AS_GEMEINDE]
                    : PERMISSIONS[Permission.ROLE_TS];
            case TSRole.ADMIN_GEMEINDE:
                return PERMISSIONS[Permission.BENUTZER_EINLADEN_AS_GEMEINDE];
            case TSRole.REVISOR:
                return PERMISSIONS[Permission.ROLE_GEMEINDE];
            case TSRole.ADMIN_SOZIALDIENST:
                return PERMISSIONS[Permission.ROLE_SOZIALDIENST];
            case TSRole.ADMIN_FERIENBETREUUNG:
                return PERMISSIONS[
                    Permission.BENUTZER_FERIENBETREUUNG_EINLADEN
                ];

            default:
                // by default the role of the user itself. the user can always see his role
                return [this.getPrincipalRole()];
        }
    }

    public initRegistration({
        gemeindenId,
        gemeindeBGId
    }: {
        gemeindenId: string[];
        gemeindeBGId: string;
    }): void {
        const queryParams = new URLSearchParams({
            return_path: `/registration/${gemeindeBGId}/${gemeindenId.join(',')}`
        });
        const url = new URL(
            `${CONSTANTS.REST_API}auth/register?${queryParams}`,
            window.location.origin
        );
        window.location.assign(url);
    }
}
