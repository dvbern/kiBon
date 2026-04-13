/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {Component, Input, inject} from '@angular/core';
import {TargetState} from '@uirouter/core';
import {KiBonMandant} from '@models/mandant';
import {AuthServiceRS} from '../../service/AuthServiceRS.rest';
import {MandantService} from '@utils/mandant';

@Component({
    selector: 'dv-tutorial-institution-login',
    templateUrl: './tutorial-institution-login.component.html',
    styleUrls: ['../tutorial-login.component.less'],
    standalone: false
})
export class TutorialInstitutionLoginComponent {
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly mandantService = inject(MandantService);

    @Input() public returnTo: TargetState;

    private mandant: KiBonMandant;

    public constructor() {
        this.mandantService.mandant$.subscribe(mandant => {
            this.mandant = mandant;
        });
    }

    public logIn(): void {
        const hostname = this.mandant.hostname;

        this.authServiceRS.initLoginReturnToTargetState(
            this.returnTo,
            `sophie.tutorial.${hostname}.persona@mailbucket.dvbern.ch`
        );
    }
}
