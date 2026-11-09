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
    maxError,
    minError,
    patternError,
    requiredError,
    ValidationError
} from '@angular/forms/signals';
import {prioritizeError, resolveErrorMessage} from './gesuch-form-error.util';

describe('GesuchFormError', () => {
    describe('prioritizeError', () => {
        it('should return undefined without errors', () => {
            expect(prioritizeError([])).toBeUndefined();
        });

        it('should prefer any other error over required', () => {
            const min = minError(10);
            expect(prioritizeError([requiredError(), min])).toBe(min);
        });

        it('should keep the schema order between non-required errors', () => {
            const pattern = patternError(/a/);
            expect(prioritizeError([pattern, maxError(5)])).toBe(pattern);
        });

        it('should return required if it is the only error', () => {
            const required = requiredError();
            expect(prioritizeError([required])).toBe(required);
        });
    });

    describe('resolveErrorMessage', () => {
        it('should map built-in kinds to translation keys', () => {
            expect(resolveErrorMessage(requiredError())).toEqual({
                key: 'ERROR_REQUIRED',
                params: undefined
            });
        });

        it('should pass min and max as translation params', () => {
            expect(resolveErrorMessage(minError(10))).toEqual({
                key: 'ERROR_MIN',
                params: {value: 10}
            });
            expect(resolveErrorMessage(maxError(100))).toEqual({
                key: 'ERROR_MAX',
                params: {value: 100}
            });
        });

        it('should use the message of an error as translation key', () => {
            const error: ValidationError = {
                kind: 'custom',
                message: 'OWN_ERROR_KEY'
            };
            expect(resolveErrorMessage(error)).toEqual({
                key: 'OWN_ERROR_KEY',
                params: undefined
            });
        });

        it('should prefer an override over the error message', () => {
            const error = requiredError({message: 'ignored'});
            expect(
                resolveErrorMessage(error, {required: 'OWN_REQUIRED_KEY'})
            ).toEqual({key: 'OWN_REQUIRED_KEY', params: undefined});
        });

        it('should keep the params of an overridden error', () => {
            expect(
                resolveErrorMessage(minError(10), {min: 'OWN_MIN_KEY'})
            ).toEqual({key: 'OWN_MIN_KEY', params: {value: 10}});
        });

        it('should ignore an empty message', () => {
            expect(resolveErrorMessage({kind: 'email', message: ''})).toEqual({
                key: 'ERROR_EMAIL',
                params: undefined
            });
        });

        it('should fall back for unknown kinds without message', () => {
            expect(resolveErrorMessage({kind: 'unknown'})).toEqual({
                key: 'ERROR_UNKNOWN',
                params: undefined
            });
        });
    });
});
