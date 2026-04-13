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

import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import * as Sentry from '@sentry/browser';
import {StateService} from '@uirouter/core';
import {element} from 'angular';
import * as angular from 'angular';
import moment from 'moment';
import {take} from 'rxjs/operators';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {environment} from '../../environments/environment';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {GlobalCacheService} from '../../gesuch/service/globalCacheService';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {MandantService} from '@utils/mandant';
import {LogFactory} from '@utils/log';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {GesuchsperiodeRS} from './service/gesuchsperiodeRS.rest';
import {ListResourceRS} from './service/listResourceRS.rest';
import IInjectorService = angular.auto.IInjectorService;
import ILocationService = angular.ILocationService;
import ITimeoutService = angular.ITimeoutService;

const LOG = LogFactory.createLog('appRun');

appRun.$inject = [
    'angularMomentConfig',
    'ListResourceRS',
    '$injector',
    'AuthLifeCycleService',
    'hotkeys',
    '$timeout',
    'AuthServiceRS',
    '$state',
    '$location',
    'GesuchsperiodeRS',
    'GlobalCacheService',
    'GemeindeRS',
    'LOCALE_ID'
];

export function appRun(
    angularMomentConfig: any,
    listResourceRS: ListResourceRS,
    $injector: IInjectorService,
    authLifeCycleService: AuthLifeCycleService,
    hotkeys: any,
    $timeout: ITimeoutService,
    authServiceRS: AuthServiceRS,
    $state: StateService,
    $location: ILocationService,
    gesuchsperiodeRS: GesuchsperiodeRS,
    globalCacheService: GlobalCacheService,
    gemeindeRS: GemeindeRS,
    LOCALE_ID: string
): void {
    const applicationPropertyRS = $injector.get<ApplicationPropertyRsService>(
        'ApplicationPropertyRsService'
    );
    const mandantService = $injector.get<MandantService>('MandantService');
    mandantService.mandant$.pipe(take(1)).subscribe({
        next: () => {
            applicationPropertyRS
                .getPublicPropertiesCached()
                .subscribe(response => {
                    if (environment.test) {
                        return;
                    }

                    Sentry.configureScope(scope => {
                        scope.addEventProcessor(event => {
                            event.environment = response.sentryEnvName;
                            return event;
                        });
                    });
                });
        },
        error: error => LOG.error(error)
    });

    function onNotAuthenticated(): void {
        authServiceRS.clearPrincipal();
        const hash = window.location.hash;
        const pathsExemptFromRedirect = [
            '#/locallogin',
            '#/tutorial/gemeinde',
            '#/tutorial/institution',
            '#/anmeldung',
            '#/mandant',
            '#/',
            '#/neu-benutzer'
        ];

        const startsWithPathExemptFromRedirect = ['#/zpv-gs-success/'];

        if (
            pathsExemptFromRedirect.some(path => hash === path) ||
            startsWithPathExemptFromRedirect.some(path => hash.startsWith(path))
        ) {
            LOG.debug('supressing redirect to ', hash);
        } else {
            authServiceRS.initLogin();
        }
    }

    function onLoginSuccess(): void {
        if (!environment.test) {
            listResourceRS.getLaenderList(); // initial aufruefen damit cache populiert wird
        }
        // muss immer geleert werden
        globalCacheService
            .getCache(TSCacheTyp.EBEGU_INSTITUTIONSSTAMMDATEN_GEMEINDE)
            .removeAll();
        // since we will need these lists anyway we already load on login
        gesuchsperiodeRS.updateActiveGesuchsperiodenList();
        gemeindeRS.getAllGemeinden();
        gesuchsperiodeRS.updateNichtAbgeschlosseneGesuchsperiodenList();
    }

    moment.locale(LOCALE_ID);

    authLifeCycleService
        .get$(TSAuthEvent.LOGIN_SUCCESS)
        .subscribe({next: onLoginSuccess, error: err => LOG.error(err)});

    authLifeCycleService
        .get$(TSAuthEvent.NOT_AUTHENTICATED)
        .subscribe({next: onNotAuthenticated, error: err => LOG.error(err)});

    angularMomentConfig.format = 'DD.MM.YYYY';

    // Attempt to restore a user session upon startup
    authServiceRS.initWithCookie().then(() => {
        LOG.debug('logged in from cookie');
    });

    // Wir meochten eigentlich ueberall mit einem hotkey das formular submitten koennen
    // https://github.com/chieffancypants/angular-hotkeys#angular-hotkeys
    hotkeys.add({
        combo: 'ctrl+shift+x',
        description: 'Press the last button with style class .next',
        callback: () => $timeout(() => element('.next').last().trigger('click'))
    });
}
