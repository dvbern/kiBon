import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {LogFactory} from '../../../../../utils/log-factory/LogFactory';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {
    FinanzielleSituationSchwyzService,
    MassgebendesEinkommenResultate
} from '../../../finanzielleSituation/schwyz/finanzielle-situation-schwyz.service';

const LOG = LogFactory.createLog(
    'EinkommensverschlechterungSchwyzResultateComponent'
);

@Component({
    selector: 'dv-einkommensverschlechterung-schwyz-resultate',
    templateUrl: './einkommensverschlechterung-schwyz-resultate.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class EinkommensverschlechterungSchwyzResultateComponent
    extends AbstractGesuchViewX<TSFinanzModel>
    implements OnInit
{
    protected readonly gesuchmodelManager: GesuchModelManager;
    protected readonly wizardstepManager: WizardStepManager;
    private readonly finanzielleSituationSchwyzService = inject(
        FinanzielleSituationSchwyzService
    );
    private readonly cd = inject(ChangeDetectorRef);

    public resultate?: MassgebendesEinkommenResultate;

    private readonly BASISJAHR = 1;

    public constructor() {
        const gesuchmodelManager = inject(GesuchModelManager);
        const wizardstepManager = inject(WizardStepManager);

        super(
            gesuchmodelManager,
            wizardstepManager,
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ
        );

        this.gesuchmodelManager = gesuchmodelManager;
        this.wizardstepManager = wizardstepManager;
    }

    public ngOnInit(): void {
        this.finanzielleSituationSchwyzService.massgebendesEinkommenStore.subscribe(
            resultate => {
                this.resultate = resultate;
                this.cd.detectChanges();
            },
            error => LOG.error(error)
        );
        this.initModel();
        this.finanzielleSituationSchwyzService.calculateEinkommensverschlechterung(
            this.model,
            this.BASISJAHR
        );
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(
            this.model
        );
    }

    private initModel() {
        this.model = new TSFinanzModel(
            this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            this.BASISJAHR
        );
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(
            this.gesuchModelManager.getGesuch()
        );
        this.gesuchModelManager.setBasisJahrPlusNumber(this.BASISJAHR);
        this.cd.detectChanges();
    }

    public save(onResult: (arg: any) => any): void {
        this.wizardStepManager
            .updateCurrentWizardStepStatusSafe(
                this.wizardStepManager.getCurrentStepName(),
                TSWizardStepStatus.OK
            )
            .then(() => {
                onResult(true);
            });
    }

    public hasMultipleFinSits(): boolean {
        return (
            this.gesuchmodelManager.isGesuchsteller2Required() &&
            EbeguUtil.isNotNullAndFalse(
                this.getGesuch().extractFamiliensituation()
                    .gemeinsameSteuererklaerung
            )
        );
    }
}
