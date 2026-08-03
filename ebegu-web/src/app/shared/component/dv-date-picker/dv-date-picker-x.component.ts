/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    booleanAttribute,
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    inject,
    Input,
    OnInit,
    Output,
    ViewEncapsulation
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {DateAdapter} from '@angular/material/core';
import moment from 'moment';
import {distinctUntilChanged} from 'rxjs/operators';
import {TSGesuchsperiode} from '../../../../models/entity/TSGesuchsperiode';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {I18nServiceRSRest} from '../../../i18n/services/i18nServiceRS.rest';

@Component({
    selector: 'dv-date-picker-x',
    templateUrl: './dv-date-picker-x.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['dv-date-picker-x.component.less'],
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    standalone: false
})
export class DvDatePickerXComponent implements OnInit {
    private dateAdapter = inject(DateAdapter<any>);
    private i18nServiceRSRest = inject(I18nServiceRSRest);

    @Input()
    public label: string;

    @Input()
    public tooltip?: string;

    @Input()
    public date: moment.Moment;

    @Input()
    public minDate: moment.Moment;

    @Input()
    public maxDate: moment.Moment;

    @Input()
    public noFutureDate: boolean;

    @Input()
    public startView: 'month' | 'year' | 'multi-year' = 'month';

    @Input()
    public placeholderFirstOfMonth: boolean = false;

    @Input()
    public errorMessageOverrides: Record<string, string> = {};

    /**
     * Whether the mat-toggle for opening the calender is enabled. Defaults to true
     */
    @Input({transform: booleanAttribute})
    public datePickerEnabled: boolean = true;

    /**
     * Custom id to be used as id for the input field. Will also be used for the label.for attribute if a label is
     * provided
     */
    @Input()
    public inputId: string;

    @Input()
    public disabled: boolean = false;

    @Output()
    public readonly dateChange: EventEmitter<moment.Moment> =
        new EventEmitter<moment.Moment>();

    @Input()
    public required: boolean;

    @Input()
    public gesuchsperiode: TSGesuchsperiode;

    public randId = EbeguUtil.generateRandomName(10);

    public emit(): void {
        this.dateChange.emit(this.date);
    }

    ngOnInit() {
        this.dateAdapter.setLocale(this.i18nServiceRSRest.currentLanguage());

        this.i18nServiceRSRest.languageChanges$
            .pipe(distinctUntilChanged())
            .subscribe(lang => {
                this.dateAdapter.setLocale(lang);
            });
    }
}
