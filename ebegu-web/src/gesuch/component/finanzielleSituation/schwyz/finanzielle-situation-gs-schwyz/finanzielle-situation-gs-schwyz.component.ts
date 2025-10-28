import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '@kibon/shared/model/enums';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationSchwyzService} from '../finanzielle-situation-schwyz.service';

@Component({
    selector: 'dv-finanzielle-situation-gs-schwyz',
    templateUrl: './finanzielle-situation-gs-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    standalone: false
})
export class FinanzielleSituationGsSchwyzComponent
    extends AbstractGesuchViewX<TSFinanzModel>
    implements OnInit
{
    protected readonly gesuchmodelManager: GesuchModelManager;
    protected readonly wizardStepManager: WizardStepManager;
    private readonly $stateParams = inject(UIRouterGlobals);
    private readonly finSitSchwyzService = inject(
        FinanzielleSituationSchwyzService
    );

    public massgebendesEinkommen = 0;
    public gesuchstellerNumber: number;
    public gesuchsteller: TSGesuchstellerContainer;

    public constructor() {
        const gesuchmodelManager = inject(GesuchModelManager);
        const wizardStepManager = inject(WizardStepManager);

        super(
            gesuchmodelManager,
            wizardStepManager,
            TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ
        );

        this.gesuchmodelManager = gesuchmodelManager;
        this.wizardStepManager = wizardStepManager;
    }

    public ngOnInit(): void {
        this.initGesuchstellerNumber();
        this.initFinanzModel();
    }

    private initGesuchstellerNumber() {
        this.gesuchstellerNumber = parseInt(
            this.$stateParams.params.gesuchstellerNumber,
            10
        );
        this.gesuchModelManager.setGesuchstellerNumber(
            this.gesuchstellerNumber
        );
        this.gesuchsteller =
            this.gesuchstellerNumber === 1
                ? this.gesuchmodelManager.getGesuch().gesuchsteller1
                : this.gesuchmodelManager.getGesuch().gesuchsteller2;
    }

    private initFinanzModel(): void {
        this.model = new TSFinanzModel(
            this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            this.gesuchstellerNumber
        );
        this.model.copyFinSitDataFromGesuch(
            this.gesuchModelManager.getGesuch()
        );
        // this field is not present on schwyz but will be checked in a lot of distributed places. Therefore we set it
        this.model.familienSituation.sozialhilfeBezueger = false;
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public getAntragstellerNameForCurrentStep(): string {
        return this.gesuchsteller.gesuchstellerJA.getFullName();
    }

    public recalculateMassgebendesEinkommen(): void {
        this.finSitSchwyzService.calculateMassgebendesEinkommen(this.model);
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return this.model.getGesuchstellerNumber() === 1
            ? TSFinanzielleSituationSubStepName.SCHWYZ_GS1
            : TSFinanzielleSituationSubStepName.SCHWYZ_GS2;
    }

    public prepareSave(
        onResult: (arg: any) => void
    ): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    private save(
        onResult: (arg: any) => void
    ): Promise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.getGesuch());
        return this.gesuchModelManager
            .saveFinanzielleSituation()
            .then(finSitCon =>
                this.gesuchmodelManager.reloadGesuch().then(() => finSitCon)
            )
            .then((finSitCon: TSFinanzielleSituationContainer) => {
                onResult(finSitCon);
                return finSitCon;
            }) as Promise<TSFinanzielleSituationContainer>;
    }
}
