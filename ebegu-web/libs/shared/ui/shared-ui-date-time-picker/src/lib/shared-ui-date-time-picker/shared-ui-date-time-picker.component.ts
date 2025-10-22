import {
    ChangeDetectionStrategy,
    Component,
    computed,
    input,
    model
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';
import {
    MatTimepicker,
    MatTimepickerInput,
    MatTimepickerToggle
} from '@angular/material/timepicker';
import {ValidationErrors} from '@angular/forms';
import {Moment} from 'moment';
import {DateUtil} from '@kibon/shared/util-fn/date';
import {EbeguUtil} from '../../../../../../../src/utils/EbeguUtil';

@Component({
    selector: 'lib-shared-ui-date-time-picker',
    imports: [
        CommonModule,
        SharedModule,
        MatTimepickerToggle,
        MatTimepicker,
        MatTimepickerInput
    ],
    templateUrl: './shared-ui-date-time-picker.component.html',
    styleUrl: './shared-ui-date-time-picker.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SharedUiDateTimePickerComponent {
    min = input<Date>();
    disabled = input<boolean>(false);
    label = input.required<string>();
    required = input<boolean>(false);
    dateTime = model.required<Date | null | undefined>();

    nextHalfHourMin = computed(() => {
        const min = this.min();
        if (min === undefined) {
            return null;
        }
        return DateUtil.toNextHalfHour(min);
    });

    combine(
        dateErrors: ValidationErrors | null,
        timeErrors: ValidationErrors | null
    ): ValidationErrors | null {
        return {
            ...dateErrors,
            ...timeErrors
        };
    }

    // we import the SharedModule => we import the MatMomentDateModule => datepicker outputs are moments
    setDate(fromDatepicker: Moment) {
        this.dateTime.update(date => {
            if (EbeguUtil.isNullOrUndefined(date)) {
                date = new Date();
            }
            date!.setDate(fromDatepicker.toDate().getDate());
            date!.setMonth(fromDatepicker.toDate().getMonth());
            date!.setFullYear(fromDatepicker.toDate().getFullYear());
            return date;
        });
    }

    // we import the SharedModule => we import the MatMomentDateModule => timepicker outputs are moments
    setTime(fromTimePicker: Moment) {
        this.dateTime.update(date => {
            if (EbeguUtil.isNullOrUndefined(date)) {
                date = new Date();
            }
            date!.setHours(fromTimePicker.toDate().getHours());
            date!.setMinutes(fromTimePicker.toDate().getMinutes());
            return date;
        });
    }
}
