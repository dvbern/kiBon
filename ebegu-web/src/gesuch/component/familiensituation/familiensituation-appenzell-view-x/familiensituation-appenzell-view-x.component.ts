import {Component, OnInit} from '@angular/core';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../admin/einstellungen/TSEinstellungKey';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSEinstellung} from '../../../../admin/einstellungen/TSEinstellung';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';

const LOG = LogFactory.createLog('FamiliensituationAppenzellViewXComponent');

@Component({
    selector: 'dv-familiensituation-appenzell-view-x',
    templateUrl: './familiensituation-appenzell-view-x.component.html',
    styleUrls: ['./familiensituation-appenzell-view-x.component.less'],
    standalone: false
})
export class FamiliensituationAppenzellViewXComponent
    extends AbstractFamiliensitutaionView
    implements OnInit
{
    protected async confirm(onResult: (arg: any) => void): Promise<void> {
        const savedContaier = await this.saveFamiliensituationAndHandleChange();
        onResult(savedContaier);
    }

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        protected readonly familiensituationRS: FamiliensituationRS,
        protected readonly authService: AuthServiceRS,
        private readonly einstellungRS: EinstellungRS
    ) {
        super(
            gesuchModelManager,
            errorService,
            wizardStepManager,
            familiensituationRS,
            authService
        );
        this.getFamiliensituation().familienstatus = TSFamilienstatus.APPENZELL;
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

    public showGemeinsamerHausltMitPartnerFrage(): boolean {
        return (
            EbeguUtil.isNotNullAndFalse(
                this.getFamiliensituation().geteilteObhut
            ) ||
            EbeguUtil.isNotNullAndFalse(
                this.getFamiliensituation()
                    .gemeinsamerHaushaltMitObhutsberechtigterPerson
            )
        );
    }

    public showGemeinsamerHausltMitObhutsberchtigterPersonFrage(): boolean {
        return EbeguUtil.isNotNullAndTrue(
            this.getFamiliensituation().geteilteObhut
        );
    }

    public onGeteilteObhutFrageChange(): void {
        this.getFamiliensituation().gemeinsamerHaushaltMitPartner = undefined;
        this.getFamiliensituation().gemeinsamerHaushaltMitObhutsberechtigterPerson =
            undefined;
    }

    public onGemeinsamerHausaltMitObhutsberechtigerPersionChange(): void {
        this.getFamiliensituation().gemeinsamerHaushaltMitPartner = undefined;
    }
}
