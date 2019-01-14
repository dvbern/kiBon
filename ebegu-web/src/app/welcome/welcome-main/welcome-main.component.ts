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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {navigateToStartPageForRole} from '../../../utils/AuthenticationUtil';

@Component({
    selector: 'dv-welcome-main',
    templateUrl: './welcome-main.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WelcomeMainComponent {

    public constructor(
        private readonly authServiceRs: AuthServiceRS,
        private readonly $state: StateService,
    ) {

    }

    public navigateToStartPage(): void {
        navigateToStartPageForRole(this.authServiceRs.getPrincipal().getCurrentRole(), this.$state);
    }

}