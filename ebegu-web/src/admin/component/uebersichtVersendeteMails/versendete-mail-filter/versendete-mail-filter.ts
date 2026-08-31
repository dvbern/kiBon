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
    effect,
    model,
    signal
} from '@angular/core';
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
import {SharedModule} from '../../../../app/shared/shared.module';
import {debounceSignal} from '../../../../utils/signal-helpers/debounce-signal';

export type VersendeteMailsFilter = {
    subjectOrReceiver: string;
    startDate?: Moment;
    endDate?: Moment;
};

const VERSENDETE_MAILS_MAT_DATE_FORMAT: MatDateFormats = {
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

/**
 * A component to filter sent emails based on specific criteria such as subject/receiver, start date, and end date.
 * This component uses Angular Material's date pickers, form fields, and custom debouncing mechanism for
 * efficient filtering operations. It provides a seamless integration with the shared module and uses
 * moment.js for date handling.
 *
 * This component does not make use of the existing date-time-picker component because the styling is not compatible
 * with the desired layout of the versendete-mail-uebersicht component.
 *
 * Responsibilities:
 * - Tracks and manages filter parameters using reactive signals.
 * - Allows users to filter emails by a combination of subject/receiver and date range.
 * - Ensures efficient real-time updates using a 1000ms debounce mechanism on the combined filter values.
 *
 */
@Component({
    selector: 'dv-versendete-mail-filter',
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
        {provide: MAT_DATE_FORMATS, useValue: VERSENDETE_MAILS_MAT_DATE_FORMAT},
        {provide: MAT_TIMEPICKER_CONFIG, useValue: {interval: '1 minute'}},
        provideMomentDateAdapter(VERSENDETE_MAILS_MAT_DATE_FORMAT)
    ],
    templateUrl: './versendete-mail-filter.html',
    styleUrls: ['./versendete-mail-filter.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VersendeteMailFilter {
    filter = model.required<VersendeteMailsFilter>();

    subjectOrReceiver = signal('');
    startDate = signal<Moment | null>(null);
    endDate = signal<Moment | null>(null);

    private readonly DEBOUNCE_TIME_MS = 1000;
    private readonly combined = computed(() => ({
        subjectOrReceiver: this.subjectOrReceiver(),
        startDate: this.startDate(),
        endDate: this.endDate()
    }));

    private debouncedFilter = debounceSignal<VersendeteMailsFilter>(
        this.combined,
        this.DEBOUNCE_TIME_MS
    );

    constructor() {
        effect(() => {
            this.filter.set(this.debouncedFilter());
        });
    }

    setStartDate(update: Moment | null) {
        if (this.startDate() == null) {
            this.startDate.set(update);
            return;
        }
        this.startDate.update(current =>
            moment(current)
                .date(update.date())
                .month(update.month())
                .year(update.year())
        );
    }

    setEndDate(update: Moment | null) {
        if (this.endDate() == null) {
            this.endDate.set(update);
            return;
        }
        this.endDate.update(current =>
            moment(current)
                .date(update.date())
                .month(update.month())
                .year(update.year())
        );
    }
}
