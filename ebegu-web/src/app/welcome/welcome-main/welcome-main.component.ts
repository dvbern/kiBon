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
import {StateService} from '@uirouter/core';
import {map} from 'rxjs/operators';
import {MandantLogoNameVisitor} from '@kibon/shared-model-mandant';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ITourParams} from '../../../gesuch/gesuch.route';
import {
    navigateToStartPageForRole,
    navigateToStartPageForRoleWithParams
} from '../../../utils/AuthenticationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {KiBonGuidedTourService} from '../../kibonTour/service/KiBonGuidedTourService';

@Component({
    selector: 'dv-welcome-main',
    templateUrl: './welcome-main.component.html',
    styleUrls: ['./welcome-main.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class WelcomeMainComponent {
    private readonly authServiceRs = inject(AuthServiceRS);
    private readonly $state = inject(StateService);
    private readonly kibonGuidedTourService = inject(KiBonGuidedTourService);
    private readonly mandantService = inject(MandantService);

    logoUrl$ = this.mandantService.mandant$.pipe(
        map(mandant => {
            const filename = new MandantLogoNameVisitor().process(mandant);
            return `url("assets/images/${filename}")`;
        })
    );

    public navigateToStartPage(): void {
        const params: ITourParams = {
            tourType: 'startTour'
        };
        navigateToStartPageForRoleWithParams(
            this.authServiceRs.getPrincipal().getCurrentRole(),
            this.$state,
            params
        );
        this.kibonGuidedTourService.emit();
    }

    public cancel(): void {
        navigateToStartPageForRole(
            this.authServiceRs.getPrincipal().getCurrentRole(),
            this.$state
        );
    }

    public isNotSozialdienstRole(): boolean {
        return !this.authServiceRs.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle()
        );
    }
}
