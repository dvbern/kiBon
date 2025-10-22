import {Component, Input, OnInit, inject} from '@angular/core';
import {TSAdresse} from '@kibon/shared/model/entity';
import {KiBonMandant} from '@kibon/shared-model-mandant';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {TSGemeindeStammdaten} from '../../../../models/TSGemeindeStammdaten';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {SharedModule} from '../../../../app/shared/shared.module';

const LOG = LogFactory.createLog('GemeindeKontaktdatenComponent');

@Component({
    selector: 'gemeinde-kontaktdaten',
    imports: [SharedModule],
    templateUrl: './gemeinde-kontaktdaten.component.html',
    styleUrl: 'gemeinde-kontaktdaten.component.less'
})
export class GemeindeKontaktdatenComponent implements OnInit {
    @Input({required: true}) stammdaten!: TSGemeindeStammdaten;
    authServiceRS = inject(AuthServiceRS);
    mandantService = inject(MandantService);
    protected readonly TSRoleUtil = TSRoleUtil;
    protected readonly EbeguUtil = EbeguUtil;

    public mandant: KiBonMandant;

    ngOnInit(): void {
        this.mandantService.mandant$.subscribe(
            mandant => {
                this.mandant = mandant;
            },
            error => LOG.error(error)
        );
    }

    public getStrasseHausnummer(adresse: TSAdresse): string {
        return (
            adresse.strasse +
            (EbeguUtil.isNotNullOrUndefined(adresse.hausnummer)
                ? ' ' + adresse.hausnummer
                : '')
        );
    }
}
