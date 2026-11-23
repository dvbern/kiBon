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
import {Moment} from 'moment';
import {debounceTime, skip} from 'rxjs';
import {SharedModule} from '../../../../app/shared/shared.module';
import {
    DateRange,
    KibAdminDateRangeFilter
} from '../../../shared/kib-admin-date-range-filter/kib-admin-date-range-filter';

const DEBOUNCE_TIME_MS = 1000;

export type VersendeteMailsFilter = {
    subjectOrReceiver: string;
    startDate?: Moment;
    endDate?: Moment;
};

/**
 * A component to filter sent emails based on specific criteria such as subject/receiver, start date, and end date.
 *
 * Responsibilities:
 * - Tracks and manages filter parameters using reactive signals.
 * - Allows users to filter emails by a combination of subject/receiver and date range.
 * - Ensures efficient real-time updates using a 1000ms debounce mechanism on the combined filter values.
 *
 */
@Component({
    selector: 'dv-versendete-mail-filter',
    imports: [SharedModule, KibAdminDateRangeFilter],
    templateUrl: './versendete-mail-filter.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VersendeteMailFilter {
    protected subjectOrReceiver = signal('');
    protected dateRange = signal<DateRange>({
        startDate: null,
        endDate: null
    });

    private readonly combined = computed<VersendeteMailsFilter>(() => ({
        subjectOrReceiver: this.subjectOrReceiver(),
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
