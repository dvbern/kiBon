import {
    Component,
    Input,
    OnInit,
    inject,
    ChangeDetectionStrategy
} from '@angular/core';
import {KiBonMandant} from '@models/mandant';
import {TSAdresse} from '../../../../models/entity/TSAdresse';
import {TSGemeindeStammdaten} from '../../../../models/TSGemeindeStammdaten';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {LogFactory} from '../../../../utils/log-factory/LogFactory';
import {MandantService} from '../../../../utils/mandant-service/mandant.service';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {SharedModule} from '../../../../app/shared/shared.module';

const LOG = LogFactory.createLog('GemeindeKontaktdatenComponent');

@Component({
    selector: 'gemeinde-kontaktdaten',
    imports: [SharedModule],
    templateUrl: './gemeinde-kontaktdaten.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
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
