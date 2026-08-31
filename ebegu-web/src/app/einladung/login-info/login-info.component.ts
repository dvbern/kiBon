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

import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {Transition} from '@uirouter/core';

@Component({
    selector: 'dv-login-info',
    templateUrl: './login-info.component.html',
    styleUrls: ['./login-info.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class LoginInfoComponent {
    transition = inject(Transition);

    public goToLoginWithReturnToState(): void {
        const params = this.transition.params();
        const options = this.transition.options();

        this.transition.router.stateService.go(
            'einladung.abschliessen',
            params,
            options
        );
    }
}
