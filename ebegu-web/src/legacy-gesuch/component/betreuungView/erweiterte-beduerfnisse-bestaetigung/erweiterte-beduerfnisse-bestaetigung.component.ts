import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    input,
    Signal
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {SharedModule} from '../../../../app/shared/shared.module';
import {HoehereBeitraegeTyp} from '../../../../models/enums/HoehereBeitraegeTyp';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSErweiterteBetreuung} from '../../../../models/TSErweiterteBetreuung';

export type ErweiterteBeduerfnisseBestaetigenEinstellungen = {
    zuschlagBehinderungProStd: number;
    zuschlagBehinderungProTag: number;
    besondereBeduerfnisseAufwandKonfigurierbar: boolean;
};

export type LabelParameters = {
    betrag?: number;
    einheit?: string;
};

@Component({
    selector: 'dv-erweiterte-beduerfnisse-bestaetigung',
    imports: [SharedModule],
    templateUrl: './erweiterte-beduerfnisse-bestaetigung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErweiterteBeduerfnisseBestaetigungComponent {
    erweiterteBetreuungJA = input.required<TSErweiterteBetreuung>();
    einstellungenSig =
        input.required<ErweiterteBeduerfnisseBestaetigenEinstellungen>();
    angebotTypSig = input.required<TSBetreuungsangebotTyp>();

    readOnly = input.required<boolean>();

    hoehereBeitraegeTyp = input.required<HoehereBeitraegeTyp>();

    labelKey = computed(() => {
        const einstellungen = this.einstellungenSig();
        const angebotTyp = this.angebotTypSig();

        if (einstellungen.besondereBeduerfnisseAufwandKonfigurierbar) {
            return 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITHOUT_BETRAG';
        }
        if (
            angebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN &&
            this.hoehereBeitraegeTyp() !==
                HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
        ) {
            return 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG_PRO_STUNDE';
        }

        if (
            angebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN &&
            this.hoehereBeitraegeTyp() ===
                HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
        ) {
            return 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG_AUSZAHLUNG_INSTITUTION';
        }

        if (
            this.hoehereBeitraegeTyp() ===
            HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
        ) {
            return 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG_AUSZAHLUNG_INSTITUTION';
        }

        return 'BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND_INST_WITH_FIX_BETRAG';
    });

    labelParameters: Signal<LabelParameters> = computed(() => {
        const einstellungen = this.einstellungenSig();
        const angebotTyp = this.angebotTypSig();

        return {
            betrag:
                angebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN
                    ? einstellungen.zuschlagBehinderungProStd
                    : einstellungen.zuschlagBehinderungProTag
        };
    });

    translate = inject(TranslateService);
}
