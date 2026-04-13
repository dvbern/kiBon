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

import {TargetState} from '@uirouter/core';
import {IComponentOptions, IController} from 'angular';
import {MandantService} from '@utils/mandant';
import {DvDialog} from '../../app/core/directive/dv-dialog/dv-dialog';
import {RedirectWarningDialogController} from '../redirect-warning-dialog/RedirectWarningDialogController';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

// eslint-disable-next-line @typescript-eslint/naming-convention,
// no-underscore-dangle, id-blacklist, id-match
export const LoginConfig: IComponentOptions = {
    transclude: false,
    template: require('./login.html'),
    controllerAs: 'vm',
    bindings: {
        returnTo: '<'
    }
};

const dialogTemplate = require('../redirect-warning-dialog/redirectWarningDialogTemplate.html');

export class LoginController implements IController {
    public static $inject: string[] = [
        'AuthServiceRS',
        'DvDialog',
        'MandantService'
    ];

    private isBern: boolean = false;
    public countdown: number = 0;

    public returnTo: TargetState;

    public constructor(
        private readonly authService: AuthServiceRS,
        private readonly dvDialog: DvDialog,
        private readonly mandantsService: MandantService
    ) {}

    public $onInit(): void {
        this.initMandant();

        if (this.isBern) {
            this.dvDialog
                .showDialog(dialogTemplate, RedirectWarningDialogController, {})
                .then(() =>
                    this.authService.initLoginReturnToTargetState(this.returnTo)
                );
        } else {
            this.authService.initLoginReturnToTargetState(this.returnTo);
        }
    }

    private initMandant() {
        this.mandantsService.mandant$.subscribe(res => {
            if (res.hostname === 'be') {
                this.isBern = true;
            }
        });
    }

    public isLoggedId(): boolean {
        return this.authService.isLoggedIn();
    }
}

LoginConfig.controller = LoginController;
