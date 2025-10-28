/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
    ChangeDetectionStrategy,
    Component,
    Input,
    OnInit,
    inject
} from '@angular/core';
import {Observable} from 'rxjs';
import {TSWizardStepStatus} from '@kibon/shared/model/enums';
import {TSGemeinde, TSGesuchsperiode} from '@kibon/shared/model/entity';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';

@Component({
    selector: 'dv-wizard-side-nav',
    templateUrl: './wizard-side-nav.component.html',
    styleUrls: ['./wizard-side-nav.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class WizardSideNavComponent implements OnInit {
    private readonly wizardStepXRS = inject(WizardStepXRS);

    @Input() public readonly id: string;
    @Input() public readonly wizardTyp: string;
    @Input() public readonly status: string;
    @Input() public readonly gemeinde: TSGemeinde;
    @Input() public readonly gesuchsperiode: TSGesuchsperiode;
    @Input() public readonly subHeader: string;

    public wizardSteps$: Observable<TSWizardStepX[]>;

    public ngOnInit(): void {
        this.wizardSteps$ = this.wizardStepXRS.getAllSteps();
    }

    public isInBearbeitung(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.IN_BEARBEITUNG;
    }

    public isOK(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.OK;
    }

    public isNOK(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.NOK;
    }
}
