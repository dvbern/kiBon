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
 *
 */

import {Ng1StateDeclaration, StateParams} from '@uirouter/angularjs';
import {StateService} from '@uirouter/core';
import {ILogService} from 'angular';
import {first} from 'rxjs/operators';
import {TSEinstellung} from '../../admin/einstellungen/TSEinstellung';
import {TSEinstellungKey} from '../../admin/einstellungen/TSEinstellungKey';
import {EinstellungRS} from '../../admin/service/einstellungRS.rest';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSGesuch} from '../../models/TSGesuch';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {getGesuchPromise} from '../gesuch.route';
import {OnlineFreigabeComponent} from './component/onlineFreigabe/online-freigabe.component';
import {firstValueFrom} from 'rxjs';

const kommentarView = '<kommentar-view>';

class EbeguFreigabeState implements Ng1StateDeclaration {
    public name = 'gesuch.freigabe';
    public url = '/freigabe/:gesuchId';
    public resolve = {
        gesuch: getGesuchPromise
    };
    public onEnter = redirectToConfiguredFreigabeView;
}

const redirectToConfiguredFreigabeView = (
    einstellungRS: EinstellungRS,
    gesuch: TSGesuch,
    $state: StateService,
    $stateParams: StateParams,
    $log: ILogService
) => {
    einstellungRS
        .getEinstellung(
            gesuch.gesuchsperiode.id,
            TSEinstellungKey.GESUCHFREIGABE_ONLINE
        )
        .pipe(first())
        .subscribe(
            (onlineFreigabe: TSEinstellung) => {
                const route = onlineFreigabe.getValueAsBoolean()
                    ? freigabeOnlineState.name
                    : freigabeMitQuittungState.name;

                if (checkEingangsArtOnline(gesuch)) {
                    $state.go(
                        route,
                        {gesuchId: $stateParams.gesuchId},
                        {reload: true}
                    );
                }
            },
            (error: any) => $log.error(error)
        );
};
redirectToConfiguredFreigabeView.$inject = [
    'EinstellungRS',
    'gesuch',
    '$state',
    '$stateParams',
    '$log'
];

const assertOnlinefreigabeEinstellungIs = (onlineFreigabeAktiv: boolean) => {
    const fn = (einstellungRS: EinstellungRS, gesuch: TSGesuch) =>
        firstValueFrom(
            einstellungRS.getEinstellung(
                gesuch.gesuchsperiode.id,
                TSEinstellungKey.GESUCHFREIGABE_ONLINE
            )
        ).then(async einstellung => {
            return (
                einstellung.getValueAsBoolean() === onlineFreigabeAktiv &&
                checkEingangsArtOnline(gesuch)
            );
        });
    fn.$inject = ['EinstellungRS', 'gesuch'];
    return fn;
};

const checkEingangsArtOnline = (gesuch: TSGesuch) => {
    return gesuch.eingangsart === TSEingangsart.ONLINE;
};
checkEingangsArtOnline.$inject = ['gesuch'];

export const freigabeRedirectState = new EbeguFreigabeState();

class EbeguFreigabeMitQuittungState implements Ng1StateDeclaration {
    public name = 'gesuch.freigabe-mitQuittung';
    public url = '/freigabe-mit-quittung/:gesuchId';

    public views: {[name: string]: Ng1StateDeclaration} = {
        gesuchViewPort: {
            template: '<freigabe-view>'
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuch: getGesuchPromise
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };

    public onEnter = assertOnlinefreigabeEinstellungIs(false);
}

export const freigabeMitQuittungState = new EbeguFreigabeMitQuittungState();

class EbeguFreigabeOnlineState implements Ng1StateDeclaration {
    public name = 'gesuch.freigabe-online';
    public url = '/freigabe-online/:gesuchId';

    public views: any = {
        gesuchViewPort: {
            component: OnlineFreigabeComponent
        },
        kommentarViewPort: {
            template: kommentarView
        }
    };

    public resolve = {
        gesuch: getGesuchPromise
    };

    public data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };

    public onEnter = assertOnlinefreigabeEinstellungIs(true);
}

export const freigabeOnlineState = new EbeguFreigabeOnlineState();
