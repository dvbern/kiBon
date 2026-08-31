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
import {TSGemeinde} from '../../../models/entity/TSGemeinde';
import {SharedModule} from '../../shared/shared.module';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {Moment} from 'moment';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {filter, startWith, switchMap, takeWhile} from 'rxjs/operators';
import {LogFactory} from '@utils/log';
import {ZahlungService} from '@app/zahlung/service';
import {TSZahlungslaufTyp} from '@models/zahlung';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {DvNgRemoveDialogComponent} from '@app/shared/component/remove-dialog';
import {interval, Subscription} from 'rxjs';

const LOG = LogFactory.createLog('ZahlungslaufErstellenComponent');

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
    templateUrl: './zahlungslauf-erstellen.component.html',
    styleUrl: './zahlungslauf-erstellen.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ZahlungslaufErstellenComponent {
    gemeindenList = input.required<Array<TSGemeinde>>();
    zahlungslaufTyp = input.required<TSZahlungslaufTyp>();
    settings = input.required<Settings>();

    zahlungslaufErstellen = output();

    form = viewChild.required(NgForm);
    translate = inject(TranslateService);
    dialog = inject(MatDialog);
    errorService = inject(ErrorService);
    zahlungRS = inject(ZahlungService);
    private pollingSub?: Subscription;

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
                    this.zahlungRS
                        .createZahlungsauftrag(
                            this.zahlungslaufTyp(),
                            this.model.gemeinde!,
                            this.model.beschrieb!,
                            this.model.faelligkeitsdatum!,
                            this.model.datumGeneriert!,
                            this.model.auszahlungInZukunft!
                        )
                        .subscribe({
                            next: (res: {workjobId: string}) => {
                                this.errorService.clearAll();
                                this.errorService.addMesageAsInfo(
                                    this.translate.instant(
                                        'ZAHLUNGSLAUF_STARTED'
                                    )
                                );
                                LOG.info(
                                    'batch job started with jobId: ' +
                                        res.workjobId
                                );
                                this.form().resetForm();
                                this.zahlungslaufErstellen.emit();
                                this.startPolling(res.workjobId);
                            },
                            error: error => LOG.error(error)
                        });
                },
                error => LOG.error(error)
            );
    }

    private startPolling(workjobId: string) {
        this.pollingSub = interval(10000)
            .pipe(
                startWith(0),
                switchMap(() =>
                    this.zahlungRS.getZahlungsauftragStatus(workjobId)
                ),
                takeWhile(
                    ({status}) => !['FAILED', 'FINISHED'].includes(status),
                    true
                )
            )
            .subscribe({
                next: (res: {status: string}) => {
                    if (res.status === 'FINISHED') {
                        this.pollingSub?.unsubscribe();
                        // reset the table
                        this.form().resetForm();
                        // tell parent to reload the list
                        this.zahlungslaufErstellen.emit();
                        this.errorService.clearAll();
                        this.errorService.addMesageAsInfo(
                            this.translate.instant('ZAHLUNGSLAUF_ERSTELLT')
                        );
                    }

                    if (res.status === 'FAILED') {
                        this.pollingSub?.unsubscribe();
                        // reset the table
                        this.form().resetForm();
                        this.errorService.clearAll();
                        this.errorService.addMesageAsError(
                            this.translate.instant('ZAHLUNGSLAUF_FAILED')
                        );
                    }
                    return;
                }
            });
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
