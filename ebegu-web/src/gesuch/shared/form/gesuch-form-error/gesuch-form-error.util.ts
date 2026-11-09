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
    MaxValidationError,
    MinValidationError,
    ValidationError
} from '@angular/forms/signals';

export type GesuchFormErrorMessageOverrides = Partial<Record<string, string>>;

export interface GesuchFormErrorMessage {
    key: string;
    params?: Record<string, unknown>;
}

const ERROR_TRANSLATION_KEYS: Record<string, string> = {
    required: 'ERROR_REQUIRED',
    min: 'ERROR_MIN',
    max: 'ERROR_MAX',
    minLength: 'ERROR_MIN_LENG',
    maxLength: 'ERROR_MAX_LENG',
    minDate: 'ERROR_MIN_DATE',
    maxDate: 'ERROR_MAX_DATE',
    pattern: 'ERROR_FORMAT',
    email: 'ERROR_EMAIL',
    phone: 'ERROR_PHONE',
    parse: 'ERROR_PARSE',
    url: 'ERROR_INVALID_URL',
    iban: 'ERROR_IBAN',
    qrIban: 'ERROR_QR_IBAN',
    bicSwiftCode: 'ERROR_BIC_SWIFT_CODE'
};

const FALLBACK_TRANSLATION_KEY = 'ERROR_UNKNOWN';

/**
 * Only one error is shown per field. Any other error is more specific than `required`,
 * otherwise the order given by the form schema is kept.
 */
export function prioritizeError(
    errors: readonly ValidationError[]
): ValidationError | undefined {
    return errors.find(error => error.kind !== 'required') ?? errors[0];
}

/**
 * Resolves the translation key of an error: an override for its kind, else its `message`
 * (used as translation key, untranslatable text is shown as is), else the default of its kind.
 */
export function resolveErrorMessage(
    error: ValidationError,
    overrides: GesuchFormErrorMessageOverrides = {}
): GesuchFormErrorMessage {
    return {
        key:
            overrides[error.kind] ||
            error.message ||
            ERROR_TRANSLATION_KEYS[error.kind] ||
            FALLBACK_TRANSLATION_KEY,
        params: translationParams(error)
    };
}

function translationParams(
    error: ValidationError
): Record<string, unknown> | undefined {
    if (error instanceof MinValidationError) {
        return {value: error.min};
    }
    if (error instanceof MaxValidationError) {
        return {value: error.max};
    }
    return undefined;
}
