/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    emailError,
    pattern,
    SchemaPath,
    validate,
    ValidationError
} from '@angular/forms/signals';
import {CONSTANTS} from '@models/constants';
import {electronicFormatIBAN, isValidBIC, isValidIBAN} from 'ibantools';

/*
 kiBon specific validators for signal forms, using the same rules as the legacy validators.
 Empty values are always valid, combine with `required()` if needed.
 The error kinds are translated by kib-gesuch-form-error (see ERROR_TRANSLATION_KEYS).
 With signal forms, a directive is the wrong place for validation, thats why we add these helper methods.
 Validation lives in the schema and the schema stays the single source of truth.
*/

/**
 * Use instead of the built-in `email()` validator, which accepts addresses like `a@b`.
 * Same pattern as the legacy `dvStrictEmailValidatorDirective`, reported as `email` error.
 */
export function strictEmail(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_EMAIL), {error: emailError()});
}

/** Swiss phone number, e.g. `031 123 45 67` */
export function phone(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_PHONE), {error: {kind: 'phone'}});
}

/** Swiss mobile number (074 - 079), e.g. `079 123 45 67` */
export function mobile(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_MOBILE), {
        error: {kind: 'phone'}
    });
}

/** Website starting with `www.`, e.g. `www.webseite.ch`, see legacy `WebseiteValidatorDirective` */
export function website(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_WEBSITE), {error: {kind: 'url'}});
}

/** ZEMIS-Nummer, e.g. `12345678.9` or `012.345.678-9` */
export function zemisNummer(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_ZEMIS_NUMMER));
}

/** Time of day as `H:MM` or `HH:MM` */
export function timeOfDay(path: SchemaPath<string>): void {
    pattern(path, anchored(CONSTANTS.PATTERN_HHHMM));
}

/** IBAN, whitespace is ignored */
export function iban(path: SchemaPath<string>): void {
    validate(path, ({value}) =>
        isEmpty(value()) || isValidIBAN(electronicFormatIBAN(value()) ?? '')
            ? undefined
            : ({kind: 'iban'} satisfies ValidationError)
    );
}

/** Forbids QR-IBANs (IID 30000 - 31999), see legacy `QrIbanValidatorDirective` */
export function notQrIban(path: SchemaPath<string>): void {
    validate(path, ({value}) =>
        !isEmpty(value()) &&
        CONSTANTS.QR_IBAN_PATTERN.test(stripWhitespace(value()))
            ? ({kind: 'qrIban'} satisfies ValidationError)
            : undefined
    );
}

/** BIC/SWIFT code, whitespace is ignored */
export function bicSwiftCode(path: SchemaPath<string>): void {
    validate(path, ({value}) =>
        isEmpty(value()) || isValidBIC(stripWhitespace(value()))
            ? undefined
            : ({kind: 'bicSwiftCode'} satisfies ValidationError)
    );
}

/** like Angular's `[pattern]` and AngularJS' `ng-pattern`, the whole value has to match */
function anchored(source: string): RegExp {
    return new RegExp(`^(?:${source})$`);
}

function isEmpty(value: string | null | undefined): boolean {
    return value == null || value === '';
}

function stripWhitespace(value: string): string {
    return value.replace(/\s/g, '');
}
