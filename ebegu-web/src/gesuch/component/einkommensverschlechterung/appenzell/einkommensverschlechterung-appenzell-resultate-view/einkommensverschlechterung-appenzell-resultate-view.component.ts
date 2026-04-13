/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {EinstellungRS} from '../../../../../admin/service/einstellungRS.rest';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractEinkommensverschlechterungResultat} from '../../AbstractEinkommensverschlechterungResultat';

@Component({
    selector: 'dv-einkommensverschlechterung-appenzell-resultate-view',
    templateUrl:
        './einkommensverschlechterung-appenzell-resultate-view.component.html',
    styleUrls: [
        './einkommensverschlechterung-appenzell-resultate-view.component.less'
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class EinkommensverschlechterungAppenzellResultateViewComponent extends AbstractEinkommensverschlechterungResultat {
    gesuchModelManager: GesuchModelManager;
    protected wizardStepManager: WizardStepManager;
    protected berechnungsManager: BerechnungsManager;
    protected ref: ChangeDetectorRef;
    protected readonly einstellungRS: EinstellungRS;
    protected readonly $transition$: Transition;
    private readonly translate = inject(TranslateService);

    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;

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
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL,
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

    public hasSecondAntragstellende(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(
                this.gesuchModelManager.getGesuch().gesuchsteller2
            ) || this.isSpezialFallAR()
        );
    }

    public getAntragsteller2Name(): string {
        if (this.isSpezialFallAR()) {
            return this.translate.instant('GS2_VERHEIRATET');
        }
        return super.getAntragsteller2Name();
    }

    public getGemeinsameFullname(): string {
        if (this.isSpezialFallAR()) {
            return `${this.getAntragsteller1Name()} + ${this.getAntragsteller2Name()}`;
        }
        return super.getGemeinsameFullname();
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }
        // we can't use the temp calculation, because we need the famSit to determine the spezialfall
        this.berechnungsManager
            .calculateEinkommensverschlechterung(
                this.getGesuch(),
                this.model.getBasisJahrPlus()
            )
            .then(() => {
                this.resultatProzent = this.calculateVeraenderung();
            });
    }
}
