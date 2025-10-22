/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {TransitionService} from '@uirouter/angular';
import {HookMatchCriteria, HookResult, Transition} from '@uirouter/core';
import {TSEinstellungKey} from '../../admin/einstellungen/TSEinstellungKey';
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {OnBeforePriorities} from '../../authentication/state-hooks/onBefore/onBeforePriorities';
import {getRoleBasedTargetState} from '../../utils/AuthenticationUtil';
import {EbeguBetreuungAbweichungenState} from '../gesuch.route';
import {GesuchModelManager} from '../service/gesuchModelManager';
import {firstValueFrom} from 'rxjs';

abortWhenAbweichungenNotEnabled.$inject = [
    '$transitions',
    'EinstellungRS',
    'AuthServiceRS',
    'GesuchModelManager'
];

export function abweichungenEnabledHook(
    $transitions: TransitionService,
    einstellungenRs: EinstellungRS,
    authService: AuthServiceRS,
    gesuchModelManager: GesuchModelManager
): void {
    const navigatesToAbweichungenCriteria: HookMatchCriteria = {
        to: state => state.name === new EbeguBetreuungAbweichungenState().name
    };

    $transitions.onFinish(
        navigatesToAbweichungenCriteria,
        async transition =>
            abortWhenAbweichungenNotEnabled(
                transition,
                einstellungenRs,
                authService,
                gesuchModelManager
            ),
        {priority: OnBeforePriorities.CONFIGURATION}
    );
}

async function abortWhenAbweichungenNotEnabled(
    transition: Transition,
    einstellungenRs: EinstellungRS,
    authService: AuthServiceRS,
    gesuchModelManager: GesuchModelManager
): Promise<HookResult> {
    const gesuchsperiodeId = gesuchModelManager.getGesuchsperiode().id;
    const abweichungEnabled = await firstValueFrom(
        einstellungenRs.getEinstellung(
            gesuchsperiodeId,
            TSEinstellungKey.ABWEICHUNGEN_ENABLED
        )
    ).then(abweichungEnabled => {
        return abweichungEnabled.getValueAsBoolean();
    });

    if (abweichungEnabled) {
        return true;
    }
    return getRoleBasedTargetState(
        authService.getPrincipalRole(),
        transition.router.stateService
    );
}
