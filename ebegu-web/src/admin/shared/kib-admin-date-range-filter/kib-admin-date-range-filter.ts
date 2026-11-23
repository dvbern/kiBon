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

import {ChangeDetectionStrategy, Component, input, model} from '@angular/core';
import {provideMomentDateAdapter} from '@angular/material-moment-adapter';
import {MAT_DATE_FORMATS, MatDateFormats} from '@angular/material/core';
import {MatDatepicker, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatIcon} from '@angular/material/icon';
import {MatFormField, MatLabel} from '@angular/material/input';
import {
    MAT_TIMEPICKER_CONFIG,
    MatTimepicker,
    MatTimepickerInput,
    MatTimepickerToggle
} from '@angular/material/timepicker';
import moment, {Moment} from 'moment';
import {SharedModule} from '../../../app/shared/shared.module';

export type DateRange = {
    startDate: Moment | null;
    endDate: Moment | null;
};

const DATE_RANGE_MAT_DATE_FORMAT: MatDateFormats = {
    parse: {
        dateInput: 'l',
        timeInput: 'HH:mm:ss'
    },
    display: {
        dateInput: 'l',
        timeInput: 'HH:mm:ss',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'LL',
        monthYearA11yLabel: 'MMMM YYYY',
        timeOptionLabel: 'HH:mm:ss'
    }
};

let nextId = 0;

/**
 * A date range filter (von/bis) consisting of a date and a time input for each boundary.
 * Used by the filter components of the admin overview pages.
 *
 * Changes are propagated immediately to the {@link dateRange} model. Debouncing is the responsibility
 * of the consuming filter component.
 *
 * This component does not make use of the existing date-time-picker component because the styling is not compatible
 * with the desired layout of the admin overview pages.
 */
@Component({
    selector: 'kib-admin-date-range-filter',
    imports: [
        MatDatepicker,
        MatDatepickerToggle,
        MatFormField,
        MatLabel,
        MatTimepickerInput,
        SharedModule,
        MatTimepicker,
        MatTimepickerToggle,
        MatIcon
    ],
    providers: [
        {provide: MAT_DATE_FORMATS, useValue: DATE_RANGE_MAT_DATE_FORMAT},
        {provide: MAT_TIMEPICKER_CONFIG, useValue: {interval: '1 minute'}},
        provideMomentDateAdapter(DATE_RANGE_MAT_DATE_FORMAT)
    ],
    templateUrl: './kib-admin-date-range-filter.html',
    styleUrls: ['./kib-admin-date-range-filter.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    host: {
        class: 'tw:flex tw:items-end tw:gap-4'
    }
})
export class KibAdminDateRangeFilter {
    /** Label above the start inputs. Can be either a translated text or a key to be translated. Defaults to `VON`. */
    readonly fromLabel = input<string>('VON');
    /** Label above the end inputs. Can be either a translated text or a key to be translated Defaults to `BIS`. */
    readonly toLabel = input<string>('BIS');
    /**
     * The DateRange model to bind to
     */
    readonly dateRange = model.required<DateRange>();

    protected readonly id = `kib-admin-date-range-filter-${nextId++}`;

    protected setStartDate(update: Moment | null): void {
        this.dateRange.update(range => ({
            ...range,
            startDate: this.mergeDate(range.startDate, update)
        }));
    }

    protected setStartTime(update: Moment | null): void {
        this.dateRange.update(range => ({...range, startDate: update}));
    }

    protected setEndDate(update: Moment | null): void {
        this.dateRange.update(range => ({
            ...range,
            endDate: this.mergeDate(range.endDate, update)
        }));
    }

    protected setEndTime(update: Moment | null): void {
        this.dateRange.update(range => ({...range, endDate: update}));
    }

    /**
     * Applies the date part of the update to the current value while keeping the time of the current value.
     */
    private mergeDate(
        current: Moment | null,
        update: Moment | null
    ): Moment | null {
        if (current == null || update == null) {
            return update;
        }
        return moment(update).set({
            hour: current.hour(),
            minute: current.minute(),
            second: current.second(),
            millisecond: current.millisecond()
        });
    }
}
