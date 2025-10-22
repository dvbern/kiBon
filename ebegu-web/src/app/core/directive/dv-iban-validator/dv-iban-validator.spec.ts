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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {DvIbanValidator} from './dv-iban-validator';
import {IController} from 'angular';

describe('DvIbanValidator', () => {
    const validator = new DvIbanValidator();
    const controller = {$validators: {}} as Partial<IController>;
    validator.link(null, null, null, controller);

    it('should have a iban validator', () => {
        expect(controller.$validators.iban).toBeDefined();
    });

    it('undefined iban should be valid', () => {
        expect(controller.$validators.iban(null, undefined)).toBeTrue();
    });

    it('null iban should be valid', () => {
        expect(controller.$validators.iban(null, null)).toBeTrue();
    });

    it('"abc" iban should be invalid', () => {
        expect(controller.$validators.iban(null, 'abc')).toBeFalse();
    });

    it('empty string should be valid', () => {
        expect(controller.$validators.iban(null, '')).toBeTrue();
    });
});
