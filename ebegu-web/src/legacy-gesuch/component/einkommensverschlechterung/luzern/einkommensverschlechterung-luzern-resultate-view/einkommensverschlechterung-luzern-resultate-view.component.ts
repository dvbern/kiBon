/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
    ChangeDetectorRef,
    Component,
    inject
} from '@angular/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';
import {AbstractEinkommensverschlechterungResultat} from '../../AbstractEinkommensverschlechterungResultat';

@Component({
    selector: 'dv-einkommensverschlechterung-luzern-resultate-view',
    templateUrl:
        './einkommensverschlechterung-luzern-resultate-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class EinkommensverschlechterungLuzernResultateViewComponent extends AbstractEinkommensverschlechterungResultat {
    gesuchModelManager: GesuchModelManager;
    protected wizardStepManager: WizardStepManager;
    protected finSitLuService = inject(FinanzielleSituationLuzernService);
    protected berechnungsManager: BerechnungsManager;
    protected ref: ChangeDetectorRef;
    protected readonly einstellungRS: EinstellungRS;
    protected readonly $transition$: Transition;

    public constructor() {
        const gesuchModelManager = inject(GesuchModelManager);
        const wizardStepManager = inject(WizardStepManager);
        const berechnungsManager = inject(BerechnungsManager);
        const ref = inject(ChangeDetectorRef);
        const einstellungRS = inject(EinstellungRS);
        const $transition$ = inject(Transition);

        super(
            gesuchModelManager,
            wizardStepManager,
            berechnungsManager,
            ref,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN,
            einstellungRS,
            $transition$
        );

        this.gesuchModelManager = gesuchModelManager;
        this.wizardStepManager = wizardStepManager;
        this.berechnungsManager = berechnungsManager;
        this.ref = ref;
        this.einstellungRS = einstellungRS;
        this.$transition$ = $transition$;
    }

    public save(onResult: (arg: any) => any): IPromise<any> {
        //hier müssen wir nur den WizardStep Updaten. Die EKV ist schon gespeichert.
        this.updateStatus();
        return onResult(true);
    }
}
