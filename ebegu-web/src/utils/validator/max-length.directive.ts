import {Directive, input} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator
} from '@angular/forms';

@Directive({
    selector: '[dvMaxLength]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: MaxLengthDirective,
            multi: true
        }
    ]
})
export class MaxLengthDirective implements Validator {
    dvMaxLength = input.required<number>();
    validate(control: AbstractControl<string>): ValidationErrors | null {
        return control.value?.length > this.dvMaxLength()
            ? {dvMaxLength: this.dvMaxLength()}
            : null;
    }
}
