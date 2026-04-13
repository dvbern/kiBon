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

import {TSBrowserLanguage} from './TSBrowserLanguage';

export enum TSSprache {
    DEUTSCH = 'DEUTSCH',
    FRANZOESISCH = 'FRANZOESISCH'
}

export enum TSBrowserSprache {
    de = 'de',
    de_be = 'de_be',
    fr = 'fr'
}

export function getTSSpracheValues(): Array<TSSprache> {
    return [TSSprache.DEUTSCH, TSSprache.FRANZOESISCH];
}

export function browserLanguageToSprache(
    browserLanguage: TSBrowserLanguage
): TSSprache {
    switch (browserLanguage) {
        case TSBrowserLanguage.DE:
            return TSSprache.DEUTSCH;
        case TSBrowserLanguage.FR:
            return TSSprache.FRANZOESISCH;
        default:
            throw new Error('Unknown Sprache: ' + browserLanguage);
    }
}
