/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {DateUtil} from './DateUtil';

/* eslint-disable */
describe('DateUtil', () => {
    describe('toNextHalfHour', () => {
        it('should round to 14:30 if 14:01', () => {
            const date = new Date('2024-01-01T14:01');
            const transformed = DateUtil.toNextHalfHour(date);
            expect(transformed.getHours()).toBe(14);
            expect(transformed.getMinutes()).toBe(30);
        });
        it('should round to 00:00 next day if 23:31', () => {
            const date = new Date('2024-01-01T23:35');
            const transformed = DateUtil.toNextHalfHour(date);
            expect(transformed.getHours()).toBe(0);
            expect(transformed.getMinutes()).toBe(0);
            expect(transformed.getDate()).toBe(2);
        });
    });

    describe('min', () => {
        it('should return the earlier date ', () => {
            const earlier = new Date('2024-06-05');
            const later = new Date('2024-06-06');
            expect(DateUtil.min(earlier, later)).toEqual(earlier);
        });
        it('should return the earlier hour if same date ', () => {
            const earlier = new Date('2024-06-05T09:00:00');
            const later = new Date('2024-06-05T10:00:00');
            expect(DateUtil.min(earlier, later)).toEqual(earlier);
        });
        it('should return the earlier minute if same hour ', () => {
            const earlier = new Date('2024-06-05T10:00:00');
            const later = new Date('2024-06-05T10:01:00');
            expect(DateUtil.min(earlier, later)).toEqual(earlier);
        });
        it('should return the earlier second if same minute ', () => {
            const earlier = new Date('2024-06-05T10:00:00');
            const later = new Date('2024-06-05T10:00:01');
            expect(DateUtil.min(earlier, later)).toEqual(earlier);
        });
        it('should return the earlier millisecond if same second ', () => {
            const earlier = new Date('2024-06-05T10:00:00.000');
            const later = new Date('2024-06-05T10:00:00.001');
            expect(DateUtil.min(earlier, later)).toEqual(earlier);
        });
    });
});
