import {Directive, inject, input} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator
} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {TSGesuch} from '../../../../../models/TSGesuch';

@Directive({
    selector: '[isValidStekIdentifierInitMail]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: ZpvInitMailUniqueValidator,
            multi: true
        }
    ]
})
export class ZpvInitMailUniqueValidator implements Validator {
    gesuch = input.required<TSGesuch>();
    translate = inject(TranslateService);

    validate(control: AbstractControl<string>): ValidationErrors | null {
        const mail = control.value;
        if (!mail) {
            return null;
        }
        if (this.gesuch().dossier.fall.besitzer === null) {
            throw new Error(
                'Stek-Identifier-Init is only possible in an online gesuch with a besitzer. This validator is either used in the wrong place or there is an issue with the data'
            );
        }
        if (
            mail.toLowerCase() ===
            this.gesuch().dossier.fall.besitzer.email.toLowerCase()
        ) {
            return {
                benutzerMailForStekIdentifierNotAllowed: {
                    email: mail
                }
            };
        }
        return null;
    }
}
