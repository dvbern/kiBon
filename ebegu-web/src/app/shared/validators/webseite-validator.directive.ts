/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import {Directive} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator
} from '@angular/forms';
import {CONSTANTS} from '@models/constants';
/*
Die Eingabe einer Invaldien Webseite soll verboten werden (valide Webseite "www.webseite.ch").
 */
@Directive({
    selector: '[isValidWebseite]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: WebseiteValidatorDirective,
            multi: true
        }
    ]
})
export class WebseiteValidatorDirective implements Validator {
    public validate(control: AbstractControl): ValidationErrors | null {
        const validWebseite = isWebseiteLike(control.value);
        return validWebseite ? null : {url: {value: control.value}};
    }
}

function isWebseiteLike(value: unknown): boolean {
    if (typeof value === 'string' && value.length > 0) {
        return new RegExp(CONSTANTS.PATTERN_WEBSITE).test(value);
    } else {
        return true;
    }
}
