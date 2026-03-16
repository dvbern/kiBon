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
    NgZone,
    OnInit,
    Output,
    viewChild
} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {TSGesuchsperiode} from '@kibon/shared/model/entity';
import {HybridFormBridgeService} from '@kibon/shared/util/hybrid-form-bridge';
import moment from 'moment';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {SharedModule} from '../../shared.module';

@Component({
    selector: 'dv-date-picker-angularjs-xx',
    template: ` <form>
        <dv-date-picker-x
            [date]="date"
            (dateChange)="emit($event)"
            [required]="required"
            [noFutureDate]="noFutureDate"
            [inputId]="inputId"
            [disabled]="disabled"
            [minDate]="minDate"
            [maxDate]="maxDate"
            [gesuchsperiode]="gesuchsperiode"
            [datePickerEnabled]="datePickerEnabled"
        ></dv-date-picker-x>
    </form>`,
    changeDetection: ChangeDetectionStrategy.Default,
    imports: [FormsModule, SharedModule],
    standalone: true
})
export class DvDatePickerXAngularjswrapperComponent implements OnInit {
    private hybridBridge = inject(HybridFormBridgeService);
    private ngZone = inject(NgZone);

    private form = viewChild(NgForm);

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
    public readonly dateChange: EventEmitter<moment.Moment | null> =
        new EventEmitter<moment.Moment | null>();

    @Input()
    public required: boolean;

    @Input()
    public gesuchsperiode: TSGesuchsperiode;

    public randId = EbeguUtil.generateRandomName(10);

    public emit(event: moment.Moment | null): void {
        this.ngZone.run(() => {
            this.dateChange.emit(event ? event.clone() : null);
        });
    }

    public ngOnInit(): void {
        this.hybridBridge.register(this.form());
    }
}
