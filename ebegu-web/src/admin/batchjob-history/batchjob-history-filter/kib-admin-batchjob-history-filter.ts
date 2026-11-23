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

import {
    ChangeDetectionStrategy,
    Component,
    computed,
    signal
} from '@angular/core';
import {outputFromObservable, toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, skip} from 'rxjs';
import {SharedModule} from '../../../app/shared/shared.module';
import {
    DateRange,
    KibAdminDateRangeFilter
} from '../../shared/kib-admin-date-range-filter/kib-admin-date-range-filter';
import {
    BATCHJOB_STATUSES,
    BatchjobHistoryFilter,
    BatchjobStatus
} from '../batchjob-history';

const DEBOUNCE_TIME_MS = 1000;

/**
 * Filters the batchjob history by name/result, status (multiple selection) and a date range on the start time.
 * The combined filter values are debounced by 1000ms.
 */
@Component({
    selector: 'kib-admin-batchjob-history-filter',
    imports: [SharedModule, KibAdminDateRangeFilter],
    templateUrl: './kib-admin-batchjob-history-filter.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KibAdminBatchjobHistoryFilter {
    protected readonly STATUSES = BATCHJOB_STATUSES;

    protected readonly nameOrResult = signal('');
    protected readonly status = signal<BatchjobStatus[]>([]);
    protected dateRange = signal<DateRange>({
        startDate: null,
        endDate: null
    });

    private readonly combined = computed<BatchjobHistoryFilter>(() => ({
        nameOrResult: this.nameOrResult(),
        status: this.status(),
        ...this.dateRange()
    }));

    /**
     * Emits the debounced filter. The initial (empty) value is not emitted, consumers start with an empty filter.
     */
    readonly filterChange = outputFromObservable(
        toObservable(this.combined).pipe(
            skip(1),
            debounceTime(DEBOUNCE_TIME_MS)
        )
    );
}
