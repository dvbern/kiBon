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

import {signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {form, SchemaPath} from '@angular/forms/signals';
import {
    bicSwiftCode,
    iban,
    mobile,
    notQrIban,
    phone,
    strictEmail,
    timeOfDay,
    website,
    zemisNummer
} from './gesuch-form-validators';

/** returns the error kinds of a single string field validated by the given validator */
function errorKinds(
    validator: (path: SchemaPath<string>) => void,
    value: string
): string[] {
    const field = TestBed.runInInjectionContext(() =>
        form(signal({value}), p => validator(p.value))
    );
    return field
        .value()
        .errors()
        .map(error => error.kind);
}

describe('GesuchFormValidators', () => {
    it('should accept empty values', () => {
        [
            strictEmail,
            phone,
            mobile,
            website,
            zemisNummer,
            timeOfDay,
            iban,
            notQrIban,
            bicSwiftCode
        ].forEach(validator => {
            expect(errorKinds(validator, '')).toEqual([]);
        });
    });

    describe('strictEmail', () => {
        it('should accept valid addresses', () => {
            expect(errorKinds(strictEmail, 'hans.muster@example.ch')).toEqual(
                []
            );
        });

        it('should reject addresses without top level domain', () => {
            expect(errorKinds(strictEmail, 'a@b')).toEqual(['email']);
        });
    });

    describe('phone', () => {
        it('should accept swiss numbers', () => {
            expect(errorKinds(phone, '031 123 45 67')).toEqual([]);
            expect(errorKinds(phone, '+41311234567')).toEqual([]);
        });

        it('should reject numbers with trailing characters', () => {
            expect(errorKinds(phone, '031 123 45 67 8')).toEqual(['phone']);
        });
    });

    describe('mobile', () => {
        it('should accept swiss mobile numbers', () => {
            expect(errorKinds(mobile, '079 123 45 67')).toEqual([]);
        });

        it('should reject landline numbers', () => {
            expect(errorKinds(mobile, '031 123 45 67')).toEqual(['phone']);
        });
    });

    describe('website', () => {
        it('should accept websites starting with www', () => {
            expect(errorKinds(website, 'www.webseite.ch')).toEqual([]);
        });

        it('should reject websites without www', () => {
            expect(errorKinds(website, 'webseite.ch')).toEqual(['url']);
        });
    });

    describe('zemisNummer', () => {
        it('should accept both formats', () => {
            expect(errorKinds(zemisNummer, '12345678.9')).toEqual([]);
            expect(errorKinds(zemisNummer, '012.345.678-9')).toEqual([]);
        });

        it('should reject other formats', () => {
            expect(errorKinds(zemisNummer, '1234')).toEqual(['pattern']);
        });
    });

    describe('timeOfDay', () => {
        it('should accept valid times', () => {
            expect(errorKinds(timeOfDay, '7:30')).toEqual([]);
            expect(errorKinds(timeOfDay, '23:59')).toEqual([]);
        });

        it('should reject invalid times', () => {
            expect(errorKinds(timeOfDay, '24:00')).toEqual(['pattern']);
        });
    });

    describe('iban', () => {
        it('should accept valid IBANs with whitespace', () => {
            expect(errorKinds(iban, 'CH93 0076 2011 6238 5295 7')).toEqual([]);
        });

        it('should reject an invalid checksum', () => {
            expect(errorKinds(iban, 'CH94 0076 2011 6238 5295 7')).toEqual([
                'iban'
            ]);
        });
    });

    describe('notQrIban', () => {
        it('should accept regular IBANs', () => {
            expect(errorKinds(notQrIban, 'CH93 0076 2011 6238 5295 7')).toEqual(
                []
            );
        });

        it('should reject QR-IBANs', () => {
            expect(errorKinds(notQrIban, 'CH44 3199 9123 0008 8901 2')).toEqual(
                ['qrIban']
            );
        });
    });

    describe('bicSwiftCode', () => {
        it('should accept valid codes', () => {
            expect(errorKinds(bicSwiftCode, 'UBSW CH ZH 80A')).toEqual([]);
        });

        it('should reject invalid codes', () => {
            expect(errorKinds(bicSwiftCode, 'UBS')).toEqual(['bicSwiftCode']);
        });
    });
});
