import {Directive, input} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator
} from '@angular/forms';
import {CONSTANTS} from '@models/constants';
import moment from 'moment';

@Directive({
    selector: '[maxZeitspanne]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: MaxZeitspanneValidatorDirective,
            multi: true
        }
    ]
})
export class MaxZeitspanneValidatorDirective implements Validator {
    referenceDate = input<moment.Moment>();
    maxZeitspanne = input<number>();

    validate(control: AbstractControl): ValidationErrors | null {
        const reference = this.referenceDate();
        const maxYears = this.maxZeitspanne();
        if (!reference || !maxYears || !control.value) {
            return null;
        }
        const inputAsMoment = moment(
            control.value,
            CONSTANTS.ALLOWED_FORMATS_DATEPICKER,
            true
        );
        if (!inputAsMoment.isValid() || !reference.isValid()) {
            return null;
        }
        if (reference.clone().add(maxYears, 'year').isBefore(inputAsMoment)) {
            return {maxZeitspanne: {maxYears}};
        }
        return null;
    }
}
