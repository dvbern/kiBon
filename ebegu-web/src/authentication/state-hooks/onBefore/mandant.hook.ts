/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
    HookResult,
    StateService,
    Transition,
    TransitionService
} from '@uirouter/core';
import {combineLatest, firstValueFrom} from 'rxjs';
import {map} from 'rxjs/operators';
import {KiBonMandant, MANDANTS} from '@kibon/shared-model-mandant';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {OnBeforePriorities} from './onBeforePriorities';

const LOG = LogFactory.createLog('mandantHook');

mandantCheck.$inject = ['$transitions', 'MandantService', '$state'];

let alreadyAlerted = false;

/**
 * This file contains a Transition Hook which checks if there is a redirect
 * to a mandant required
 *
 * A redirect to a mandant is required, if there is no mandant set, but the
 * redirect cookie has a mandant value
 *
 * This hook redirects to /mandant-redirect, where the redirect to the mandant
 * is performed. We need to do this in its own state to ensure that no transition
 * is aborted
 */

export function mandantCheck($transitions: TransitionService): void {
    $transitions.onBefore(
        {
            to: state => !state.name.includes('mandant')
        },
        checkMandant,
        {priority: OnBeforePriorities.AUTHENTICATION}
    );

    $transitions.onSuccess({to: 'mandant-redirect'}, performMandantRedirect(), {
        priority: OnBeforePriorities.AUTHENTICATION
    });
}

function checkMandant(transition: Transition): HookResult {
    const mandantService: MandantService = transition
        .injector()
        .get('MandantService');
    const $state: StateService = transition.injector().get('$state');

    return firstValueFrom(
        combineLatest([
            mandantService.mandant$,
            mandantService.isMultimandantActive$()
        ]).pipe(
            map(([mandant, isMultimandanActive]) => {
                const mandantFromHostname =
                    mandantService.parseHostnameForMandant();
                let mandantRedirectFromCookie =
                    mandantService.getMandantRedirect();
                if (!isMultimandanActive) {
                    setDefaultCookies(
                        mandantFromHostname,
                        mandant,
                        mandantService,
                        mandantRedirectFromCookie
                    );
                    return true;
                }

                LOG.debug('checking mandant', mandant);
                const path = transition.router.stateService.href(
                    transition.to(),
                    transition.params()
                );

                if (mandantFromHostname === MANDANTS.NONE) {
                    if (mandantRedirectFromCookie === MANDANTS.NONE) {
                        if (mandantService.isMandantTransitionForLuzern()) {
                            mandantRedirectFromCookie = MANDANTS.LUZERN;
                        } else {
                            return $state.target('onboarding.mandant', {path});
                        }
                    }

                    return $state.target('mandant-redirect', {
                        mandant: mandantRedirectFromCookie,
                        returnTo: path
                    });
                }

                // continue the original transition
                return true;
            })
        )
    );
}

function performMandantRedirect() {
    return (t: Transition) => {
        const mandantService: MandantService = t
            .injector()
            .get('MandantService');
        const $state: StateService = t.injector().get('$state');
        const {mandant, returnTo} = t.params();
        if (mandant === null || returnTo === null) {
            $state.go('onboarding.start');
            // return value is ignored in onSuccess
            return true;
        }
        mandantService.redirectToMandantSubdomain(mandant, returnTo);
        return true;
    };
}

function setDefaultCookies(
    mandantFromHostname: KiBonMandant,
    mandant: KiBonMandant,
    mandantService: MandantService,
    mandantRedirectFromCookie: KiBonMandant
): void {
    if (!alreadyAlerted && mandantFromHostname !== MANDANTS.NONE) {
        alert('Multimandant ist nicht aktiviert');
        alreadyAlerted = true;
    }
    if (mandant !== MANDANTS.BERN) {
        mandantService.setMandantCookie(MANDANTS.BERN);
    }
    if (mandantRedirectFromCookie !== MANDANTS.BERN) {
        mandantService.setMandantRedirectCookie(MANDANTS.BERN);
    }
}
