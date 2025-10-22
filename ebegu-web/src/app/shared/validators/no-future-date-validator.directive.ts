import {Directive, Input} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator,
    ValidatorFn
} from '@angular/forms';
import moment from 'moment';

@Directive({
    selector: '[noFutureDate]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: NoFutureDateValidatorDirective,
            multi: true
        }
    ],
    standalone: false
})
export class NoFutureDateValidatorDirective implements Validator {
    @Input('noFutureDate') active: boolean = false;

    validate(control: AbstractControl): ValidationErrors | null {
        if (this.active) {
            if (control.value && moment(control.value).isValid()) {
                return noFutureDateValidator()(control);
            }
        }
        return null;
    }
}

export function noFutureDateValidator(): ValidatorFn {
    return (control): ValidationErrors | null => {
        const forbidden = moment().isBefore(moment(control.value));
        return forbidden ? {dvNoFutureDateTime: {value: control.value}} : null;
    };
}
