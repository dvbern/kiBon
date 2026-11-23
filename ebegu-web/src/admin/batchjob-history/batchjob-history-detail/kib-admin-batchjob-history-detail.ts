/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle
} from '@angular/material/dialog';
import {MatDivider} from '@angular/material/list';
import {SharedModule} from '../../../app/shared/shared.module';
import {TableBatchjobHistory} from '../kib-admin-batchjob-history';

@Component({
    selector: 'kib-admin-batchjob-history-detail',
    imports: [
        MatDivider,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        SharedModule
    ],
    templateUrl: './kib-admin-batchjob-history-detail.html',
    styleUrls: ['./kib-admin-batchjob-history-detail.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KibAdminBatchjobHistoryDetail {
    protected readonly data = inject<TableBatchjobHistory>(MAT_DIALOG_DATA);
    protected readonly isRunning = this.data.status === 'RUNNING';
}
