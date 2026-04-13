import {Directive, forwardRef} from '@angular/core';
import {
    NG_VALIDATORS,
    Validator,
    AbstractControl,
    ValidationErrors
} from '@angular/forms';
import {CONSTANTS} from '@models/constants';

@Directive({
    selector: '[email][ngModel],[email][formControl],[email][formControlName]', // overrides angulars email directives
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => dvStrictEmailValidatorDirective),
            multi: true
        }
    ]
})
export class dvStrictEmailValidatorDirective implements Validator {
    private strictEmailRegex = new RegExp(CONSTANTS.PATTERN_EMAIL);

    validate(control: AbstractControl): ValidationErrors | null {
        if (!control.value) {
            return null;
        }
        return this.strictEmailRegex.test(control.value) ? null : {email: true};
    }
}
