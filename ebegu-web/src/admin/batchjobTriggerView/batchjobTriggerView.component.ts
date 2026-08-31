/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Component, inject, ChangeDetectionStrategy} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {LogFactory} from '@utils/log';
import {DvNgOkDialogComponent} from '../../app/core/component/dv-ng-ok-dialog/dv-ng-ok-dialog.component';
import {DailyBatchService} from './dailyBatch.service';
import {YearlyBatchService} from './yearlyBatch.service';
import {ExportBatchRS} from './exportBatchRS.service';
import {DatabaseMigrationRS} from '../service/databaseMigrationRS.rest';

const LOG = LogFactory.createLog('BatchjobTriggerViewComponent');

@Component({
    selector: 'dv-batchjob-trigger-view',
    templateUrl: './batchjobTriggerView.component.html',
    styleUrls: ['./batchjobTrigger.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class BatchjobTriggerViewComponent {
    private readonly dialog = inject(MatDialog);
    private readonly databaseMigrationRS = inject(DatabaseMigrationRS);
    private readonly exportBatchRS = inject(ExportBatchRS);
    private readonly dailyBatchRS = inject(DailyBatchService);
    private readonly yearlyBatchRS = inject(YearlyBatchService);

    public processScript(script: string): void {
        this.databaseMigrationRS.processScript(script);
    }

    public runBatchCleanDownloadFiles(): void {
        this.dailyBatchRS.runBatchCleanDownloadFiles().subscribe({
            next: response => {
                const title = response
                    ? 'CLEANDOWNLOADFILES_BATCH_EXECUTED_OK'
                    : 'CLEANDOWNLOADFILES_EXECUTED_ERROR';
                this.createAndOpenDialog(title);
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
    }

    public runBatchMahnungFristablauf(): void {
        this.dailyBatchRS.runBatchMahnungFristablauf().subscribe({
            next: response => {
                const title = response
                    ? 'MAHNUNG_BATCH_EXECUTED_OK'
                    : 'MAHNUNG_BATCH_EXECUTED_ERROR';
                this.createAndOpenDialog(title);
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
    }

    public runBatchUpdateGemeindeForBGInstitutionen(): void {
        this.dailyBatchRS.runBatchUpdateGemeindeForBGInstitutionen().subscribe({
            next: response => {
                const title = response
                    ? 'Gemeinden erfolgreich aktualisiert'
                    : 'Fehler beim aktualisieren der Gemeinden';
                this.createAndOpenDialog(title);
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
    }

    public runBatchPublishExistingGemeinden(): void {
        this.exportBatchRS.runBatchPublishExistingGemeinden().subscribe({
            next: () => {
                this.createAndOpenDialog(
                    'Publish existing Gemeinden erfolgreich durchgeführt'
                );
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
        this.createAndOpenDialog('Publish existing Gemeinden gestarted');
    }

    public runBatchPublishExistingInstitutionen(): void {
        this.exportBatchRS.runBatchPublishExistingInstitutionen().subscribe({
            next: () => {
                this.createAndOpenDialog(
                    'Publish existing Institution erfolgreich durchgeführt'
                );
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
        this.createAndOpenDialog('Publish existing Institution gestarted');
    }

    public runBatchPublishWartendeAnmeldungen(): void {
        this.exportBatchRS.runBatchPublishWartendeAnmeldungen().subscribe({
            next: () => {
                this.createAndOpenDialog(
                    'Publish wartende Anmeldungen erfolgreich durchgeführt'
                );
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
        this.createAndOpenDialog('Publish wartende Anmeldungen gestarted');
    }

    public runBatchPublishWartendeBetreuung(): void {
        this.exportBatchRS.runBatchPublishWartendeBetreuung().subscribe({
            next: () => {
                this.createAndOpenDialog(
                    'Publish wartende Betreuungen erfolgreich durchgeführt'
                );
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
        this.createAndOpenDialog('Publish wartende Betreuungen gestarted');
    }

    public runBatchPublishExistingGemeindeKennzahlen(): void {
        this.exportBatchRS
            .runBatchPublishExistingGemeindeKennzahlen()
            .subscribe({
                next: () => {
                    this.createAndOpenDialog(
                        'Publish existing Gemeinde Kennzahlen erfolgreich durchgeführt'
                    );
                },
                error: () => LOG.error('Batch Job beendet mit Fehlern')
            });
        this.createAndOpenDialog(
            'Publish existing Gemeinde Kennzahlen gestarted'
        );
    }

    public runBatchMigrateVerfuegung(): void {
        this.exportBatchRS.runBatchMigrateVerfuegung().subscribe({
            next: () => {
                this.createAndOpenDialog(
                    'Migrate Verfuegung erfolgreich durchgeführt'
                );
            },
            error: () => LOG.error('Batch Job beendet mit Fehlern')
        });
        this.createAndOpenDialog('Migrate Verfuegung gestarted');
    }

    public runBatchCreateGemeindeKennzahlenAndSendReminder(): void {
        this.yearlyBatchRS
            .runBatchCreateGemeindeKennzahlenAndSendReminder()
            .subscribe({
                next: res => {
                    this.createAndOpenDialog(
                        'GEMEINDE_KENNZAHLEN_BATCHJOB_RESULT_' + res
                    );
                },
                error: err => this.createAndOpenDialog(err)
            });
    }

    public runBatchGemeindeKenzahlenSendSecondReminder(): void {
        this.yearlyBatchRS
            .runBatchGemeindeKennzahlenSendSecondReminder()
            .subscribe({
                next: res => {
                    this.createAndOpenDialog(
                        'GEMEINDE_KENNZAHLEN_BATCHJOB_RESULT_' + res
                    );
                },
                error: err => this.createAndOpenDialog(err)
            });
    }

    public runBatchInfoOffenePendenzenNeueMitteilungGemeinde(): void {
        this.dailyBatchRS
            .runBatchInfoOffenePendenzenNeueMitteilungGemeinde()
            .subscribe({
                next: response => {
                    const title = response
                        ? 'GEMEINDE_INFO_OFFENE_PENDENZEN_BATCHJOB_EXECUTED_OK'
                        : 'GEMEINDE_INFO_OFFENE_PENDENZEN_BATCHJOB_EXECUTED_ERROR';
                    this.createAndOpenDialog(title);
                },
                error: error => LOG.error(error)
            });
    }

    private createAndOpenDialog(title: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {title};

        this.dialog.open(DvNgOkDialogComponent, dialogConfig);
    }
}
