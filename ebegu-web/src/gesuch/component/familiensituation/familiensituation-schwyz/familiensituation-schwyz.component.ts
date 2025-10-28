import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {mergeMap} from 'rxjs/operators';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../admin/einstellungen/TSEinstellungKey';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSGesuchstellerKardinalitaet} from '../../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSEinstellung} from '../../../../admin/einstellungen/TSEinstellung';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../../models/TSFamiliensituationContainer';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';
import {
    DvNgGsRemovalConfirmationDialogComponent,
    GSRemovalConfirmationDialogData
} from '../dv-ng-gs-removal-confirmation-dialog/dv-ng-gs-removal-confirmation-dialog.component';
import {DvNgGsRemovalQuestionDialogComponent} from '../dv-ng-gs-removal-question-dialog/dv-ng-gs-removal-question-dialog.component';
import {FamiliensituationUtil} from '../FamiliensituationUtil';
import {firstValueFrom} from 'rxjs';

const LOG = LogFactory.createLog('FamiliensituationSchwyzComponent');

@Component({
    selector: 'dv-familiensituation-schwyz',
    templateUrl: './familiensituation-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FamiliensituationSchwyzComponent
    extends AbstractFamiliensitutaionView
    implements OnInit
{
    protected readonly gesuchModelManager: GesuchModelManager;
    protected readonly errorService: ErrorService;
    protected readonly wizardStepManager: WizardStepManager;
    protected readonly familiensituationRS: FamiliensituationRS;
    protected readonly authService: AuthServiceRS;
    private readonly einstellungRS = inject(EinstellungRS);
    private readonly translate = inject(TranslateService);
    private readonly dialog = inject(MatDialog);

    protected readonly TSGesuchstellerKardinalitaet =
        TSGesuchstellerKardinalitaet;
    private readonly initialFamiliensituation: TSFamiliensituation;

    public constructor() {
        const gesuchModelManager = inject(GesuchModelManager);
        const errorService = inject(ErrorService);
        const wizardStepManager = inject(WizardStepManager);
        const familiensituationRS = inject(FamiliensituationRS);
        const authService = inject(AuthServiceRS);

        super(
            gesuchModelManager,
            errorService,
            wizardStepManager,
            familiensituationRS,
            authService
        );
        this.gesuchModelManager = gesuchModelManager;
        this.errorService = errorService;
        this.wizardStepManager = wizardStepManager;
        this.familiensituationRS = familiensituationRS;
        this.authService = authService;

        this.getFamiliensituation().familienstatus = TSFamilienstatus.SCHWYZ;
        this.initialFamiliensituation =
            this.gesuchModelManager.getFamiliensituation();
    }

    public ngOnInit(): void {
        this.einstellungRS
            .getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            )
            .subscribe(
                (response: TSEinstellung[]) => {
                    response
                        .filter(
                            r =>
                                r.key ===
                                TSEinstellungKey.MINIMALDAUER_KONKUBINAT
                        )
                        .forEach(value => {
                            this.getFamiliensituation().minDauerKonkubinat =
                                Number(value.value);
                        });
                },
                error => LOG.error(error)
            );
    }

    public getBisherText(): string {
        return this.translate.instant(
            this.getFamiliensituationGS()?.gesuchstellerKardinalitaet ===
                TSGesuchstellerKardinalitaet.ALLEINE
                ? 'LABEL_NEIN'
                : 'LABEL_JA'
        );
    }

    public hasError(): boolean {
        return false;
    }

    protected async confirm(onResult: (arg: any) => void): Promise<void> {
        if (this.changeDirektResetsGS2()) {
            firstValueFrom(
                this.dialog
                    .open<
                        DvNgGsRemovalConfirmationDialogComponent,
                        GSRemovalConfirmationDialogData
                    >(DvNgGsRemovalConfirmationDialogComponent, {
                        data: {
                            gsFullName: this.getGesuch().gesuchsteller2
                                ? this.getGesuch().gesuchsteller2.extractFullName()
                                : ''
                        }
                    })
                    .afterClosed()
            ).then(async hasConfirmed => {
                if (hasConfirmed) {
                    onResult(await this.saveFamiliensituationAndHandleChange());
                } else {
                    onResult(undefined);
                }
            });
            return;
        }
        if (this.changeFrageResetsGS2()) {
            firstValueFrom(
                this.dialog
                    .open<
                        DvNgGsRemovalQuestionDialogComponent,
                        GSRemovalConfirmationDialogData
                    >(DvNgGsRemovalQuestionDialogComponent, {
                        data: {
                            gsFullName: this.getGesuch().gesuchsteller2
                                ? this.getGesuch().gesuchsteller2.extractFullName()
                                : ''
                        }
                    })
                    .afterClosed()
            ).then(async hasConfirmed => {
                if (hasConfirmed) {
                    onResult(await this.saveFamiliensituation());
                } else if (EbeguUtil.isNotNullAndFalse(hasConfirmed)) {
                    onResult(await this.saveFamiliensituationAndHandleChange());
                } else {
                    onResult(undefined);
                }
            });
            return;
        }
        onResult(await this.saveFamiliensituationAndHandleChange());
    }

    protected saveFamiliensituation(): Promise<TSFamiliensituationContainer> {
        this.errorService.clearAll();
        return firstValueFrom(
            this.familiensituationRS
                .saveFamiliensituationOnly(this.model, this.getGesuch().id)
                .pipe(
                    mergeMap((familienContainerResponse: any) => {
                        this.model = familienContainerResponse;
                        this.getGesuch().familiensituationContainer =
                            familienContainerResponse;
                        // Gesuchsteller may changed...
                        return this.gesuchModelManager
                            .reloadGesuch()
                            .then(() => this.model);
                    })
                )
        );
    }

    private changeDirektResetsGS2(): boolean {
        const gesuchsteller2 =
            this.gesuchModelManager.getGesuch().gesuchsteller2;
        const hasGesuchsteller2 =
            EbeguUtil.isNotNullOrUndefined(gesuchsteller2);

        if (!hasGesuchsteller2) {
            return false;
        }

        const isMutation = this.isMutation();
        const hasNoAenderungPer = EbeguUtil.isNullOrUndefined(
            this.initialFamiliensituation.aenderungPer
        );

        const isChangeFrom2GSTo1GS = FamiliensituationUtil.isChangeFrom2GSTo1GS(
            this.initialFamiliensituation,
            this.getFamiliensituation(),
            this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis
        );

        return (isMutation && hasNoAenderungPer) || isChangeFrom2GSTo1GS;
    }

    private changeFrageResetsGS2(): boolean {
        const gesuchsteller2 =
            this.gesuchModelManager.getGesuch().gesuchsteller2;
        const hasGesuchsteller2 =
            EbeguUtil.isNotNullOrUndefined(gesuchsteller2);

        if (!hasGesuchsteller2) {
            return false;
        }

        const isMutation = this.isMutation();
        const hasAenderungPer = EbeguUtil.isNotNullOrUndefined(
            this.initialFamiliensituation.aenderungPer
        );

        const isNotChangeFrom2GSTo1GS =
            !FamiliensituationUtil.isChangeFrom2GSTo1GS(
                this.initialFamiliensituation,
                this.getFamiliensituation(),
                this.gesuchModelManager.getGesuchsperiode().gueltigkeit
                    .gueltigBis
            );

        return isMutation && hasAenderungPer && isNotChangeFrom2GSTo1GS;
    }
}
