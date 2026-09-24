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

import {
    ChangeDetectionStrategy,
    Component,
    signal,
    inject
} from '@angular/core';
import {SharedModule} from '../../../../app/shared/shared.module';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {TSFreigabe} from '../../../../models/TSFreigabe';
import {TSGesuch} from '../../../../models/TSGesuch';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {FreigabeService} from '../../freigabe.service';

interface Model {
    userConfirmedCorrectness: boolean;
}

const STEP_NAME = TSWizardStepName.FREIGABE;

@Component({
    templateUrl: './online-freigabe.component.html',
    selector: 'dv-online-freigabe',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [SharedModule]
})
export class OnlineFreigabeComponent {
    private readonly gesuchModelManager = inject(GesuchModelManager);
    private readonly wizardStepManager = inject(WizardStepManager);
    protected readonly freigabeService = inject(FreigabeService);

    public alreadyFreigegeben = signal<boolean>(null);
    public model: Model;

    public constructor() {
        const gesuchModelManager = this.gesuchModelManager;
        const wizardStepManager = this.wizardStepManager;

        const unbesucht =
            wizardStepManager.getStepByName(STEP_NAME).wizardStepStatus ===
            TSWizardStepStatus.UNBESUCHT;
        this.wizardStepManager.setCurrentStep(STEP_NAME);
        this.alreadyFreigegeben.set(
            isAtLeastFreigegeben(gesuchModelManager.getGesuch().status)
        );
        this.model = {userConfirmedCorrectness: this.alreadyFreigegeben()};

        this.updatedWizardStepManagerAndStep(unbesucht);
    }

    private updatedWizardStepManagerAndStep(unbesucht: boolean): void {
        this.wizardStepManager.isTransitionInProgress = false;

        if (!this.alreadyFreigegeben() && unbesucht) {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.IN_BEARBEITUNG
            );
        } else if (this.alreadyFreigegeben()) {
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.OK
            );
        }
    }

    public async freigeben(): Promise<TSGesuch | void> {
        if (!this.model.userConfirmedCorrectness) {
            return null;
        }
        const freigabeDto = new TSFreigabe(
            null,
            null,
            this.model.userConfirmedCorrectness
        );
        try {
            return await this.gesuchModelManager
                .antragFreigeben(
                    this.gesuchModelManager.getGesuch().id,
                    freigabeDto
                )
                .then(() => {
                    this.alreadyFreigegeben.set(true);
                });
        } catch {
            return this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                STEP_NAME,
                TSWizardStepStatus.NOK
            );
        }
    }

    public freigebenButtonDisabled() {
        return (
            !this.model.userConfirmedCorrectness ||
            this.alreadyFreigegeben() ||
            this.cannotBeFreigegeben()
        );
    }

    public checkboxDisabled() {
        return this.alreadyFreigegeben() || this.cannotBeFreigegeben();
    }

    public getReason(): string {
        return this.freigabeService.getTextForFreigebenNotAllowed();
    }

    public showReason(): boolean {
        return this.cannotBeFreigegeben() && !this.alreadyFreigegeben();
    }

    public cannotBeFreigegeben(): boolean {
        return !this.freigabeService.canBeFreigegeben();
    }
}
