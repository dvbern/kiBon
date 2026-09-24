import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../../AbstractFinSitsolothurnView';
import {FinanzielleSituationSolothurnService} from '../../finanzielle-situation-solothurn.service';

@Component({
    selector: 'dv-angaben-gs1',
    templateUrl: '../angaben-gs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class AngabenGs1Component extends AbstractFinSitsolothurnView {
    gesuchModelManager: GesuchModelManager;
    readonly finSitSoService: FinanzielleSituationSolothurnService;
    wizardStepManager: WizardStepManager;

    public constructor() {
        const gesuchModelManager = inject(GesuchModelManager);
        const finSitSoService = inject(FinanzielleSituationSolothurnService);
        const wizardStepManager = inject(WizardStepManager);

        super(gesuchModelManager, wizardStepManager, finSitSoService, 1);

        this.gesuchModelManager = gesuchModelManager;
        this.finSitSoService = finSitSoService;
        this.wizardStepManager = wizardStepManager;
    }

    public getAntragstellerNummer(): number {
        return 1;
    }

    public getSubStepIndex(): number {
        return 1;
    }

    public getSubStepName(): string {
        return TSFinanzielleSituationSubStepName.SOLOTHURN_GS1;
    }

    public prepareSave(
        onResult: (arg: any) => any
    ): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    public isGemeinsam(): boolean {
        return true;
    }

    public steuerveranlagungErhaltenChange(
        steuerveranlagungErhalten: boolean
    ): void {
        if (EbeguUtil.isNotNullAndTrue(steuerveranlagungErhalten)) {
            this.resetBruttoLohn();
        }

        if (EbeguUtil.isNotNullAndFalse(steuerveranlagungErhalten)) {
            this.resetVeranlagungSolothurn();
        }
    }
}
