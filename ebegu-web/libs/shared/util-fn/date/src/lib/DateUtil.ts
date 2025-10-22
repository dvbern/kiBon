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

export class DateUtil {
    public static toNextHalfHour(date: Date): Date {
        const halfHourInMS = 1000 * 60 * 30;
        return new Date(
            Math.ceil(date.getTime() / halfHourInMS) * halfHourInMS
        );
    }

    public static toLocalDateFormat(date: Date): string {
        return `${date.getFullYear()}-${this.getPadded(date.getMonth() + 1)}-${this.getPadded(date.getDate())}T${this.getPadded(date.getHours())}:${this.getPadded(date.getMinutes())}:${this.getPadded(date.getSeconds())}.${this.getPadded(date.getMilliseconds())}`;
    }

    private static getPadded(value: number): string {
        if (value >= 10) {
            return value.toString();
        }
        return '0' + value;
    }

    public static min(date1: Date, date2: Date): Date {
        return date1 < date2 ? date1 : date2;
    }
}
