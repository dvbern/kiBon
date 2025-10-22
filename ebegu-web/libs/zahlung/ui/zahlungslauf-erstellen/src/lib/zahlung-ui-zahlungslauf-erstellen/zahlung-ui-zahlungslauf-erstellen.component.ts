import {
    ChangeDetectionStrategy,
    Component,
    inject,
    input,
    output,
    viewChild
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgForm, ReactiveFormsModule} from '@angular/forms';
import {
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle
} from '@angular/material/datepicker';
import {MatSuffix} from '@angular/material/form-field';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {TSGemeinde} from '@kibon/shared/model/entity';
import {Moment} from 'moment';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {filter} from 'rxjs/operators';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {ZahlungUtilZahlungService} from '@kibon/zahlung/util/zahlung-service';
import {
    TSZahlungsauftrag,
    TSZahlungslaufTyp
} from '@kibon/zahlung/model/entity';
import {ErrorService} from '../../../../../../../src/app/core/errors/service/ErrorService';
import {DvNgRemoveDialogComponent} from '@kibon/shared/ui/remove-dialog';

const LOG = LogFactory.createLog('ZahlungUiZahlungslaufErstellenComponent');

@Component({
    selector: 'lib-zahlung-ui-zahlungslauf-erstellen',
    standalone: true,
    imports: [
        CommonModule,
        MatDatepicker,
        MatDatepickerInput,
        MatDatepickerToggle,
        MatSuffix,
        SharedModule,
        TranslateModule,
        ReactiveFormsModule
    ],
    templateUrl: './zahlung-ui-zahlungslauf-erstellen.component.html',
    styleUrl: './zahlung-ui-zahlungslauf-erstellen.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ZahlungUiZahlungslaufErstellenComponent {
    gemeindenList = input.required<Array<TSGemeinde>>();
    zahlungslaufTyp = input.required<TSZahlungslaufTyp>();
    settings = input.required<Settings>();

    zahlungslaufErstellen = output<TSZahlungsauftrag>();

    form = viewChild.required(NgForm);
    translate = inject(TranslateService);
    dialog = inject(MatDialog);
    errorService = inject(ErrorService);
    zahlungRS = inject(ZahlungUtilZahlungService);

    model: ZahlungslaufErstellen = {
        gemeinde: null,
        faelligkeitsdatum: null,
        auszahlungInZukunft: null,
        beschrieb: null,
        datumGeneriert: null
    };

    public createZahlungsauftrag(): void {
        if (this.form().invalid) {
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_ERSTELLEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_ERSTELLEN_INFO')
        };

        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(filter(result => !!result))
            .subscribe(
                () => {
                    this.errorService.addMesageAsInfo(
                        this.translate.instant('ZAHLUNG_AUSGELOEST_INFO')
                    );
                    this.zahlungRS
                        .createZahlungsauftrag(
                            this.zahlungslaufTyp(),
                            this.model.gemeinde!,
                            this.model.beschrieb!,
                            this.model.faelligkeitsdatum!,
                            this.model.datumGeneriert!,
                            this.model.auszahlungInZukunft!
                        )
                        .subscribe(
                            (response: TSZahlungsauftrag) => {
                                this.errorService.clearAll();
                                this.errorService.addMesageAsInfo(
                                    this.translate.instant('ZAHLUNG_ERSTELLT')
                                );
                                this.form().resetForm();
                                this.zahlungslaufErstellen.emit(response);
                            },
                            error => LOG.error(error)
                        );
                },
                error => LOG.error(error)
            );
    }

    public getLabelZahlungslaufErstellen(): string {
        if (
            this.zahlungslaufTyp() ===
                TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER &&
            this.settings().hasMahlzeitenZahlungslaeufe
        ) {
            return this.translate.instant(
                'BUTTON_GEMEINDE_ZAHLUNGSLAUF_MAHLZEITEN'
            );
        }
        return this.translate.instant('BUTTON_GEMEINDE_ZAHLUNGSLAUF_GUTSCHEIN');
    }
}

type ZahlungslaufErstellen = {
    gemeinde: TSGemeinde | null;
    faelligkeitsdatum: Moment | null;
    datumGeneriert: Moment | null;
    beschrieb: string | undefined | null;
    auszahlungInZukunft: boolean | null;
};

export type Settings = {
    testMode: boolean;
    checkboxAuszahlungInZukunft: boolean;
    hasMahlzeitenZahlungslaeufe: boolean;
};
