import {Directive} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator,
    ValidatorFn
} from '@angular/forms';
import {isValidBIC} from 'ibantools';

@Directive({
    selector: '[isValidBicSwiftCode]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: BicSwiftValidatorDirective,
            multi: true
        }
    ]
})
export class BicSwiftValidatorDirective implements Validator {
    validate(control: AbstractControl<string>): ValidationErrors | null {
        return bicSwiftValidator()(control);
    }
}

export function bicSwiftValidator(): ValidatorFn {
    return (control: AbstractControl<string>): ValidationErrors | null => {
        const valid = isValidBicSwiftCode(control.value.replace(/\s+/g, ''));
        return valid ? null : {bicSwiftCode: {}};
    };
}

function isValidBicSwiftCode(value: string): boolean {
    return isValidBIC(value);
}
