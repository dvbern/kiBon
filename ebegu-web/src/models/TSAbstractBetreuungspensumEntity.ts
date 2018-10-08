/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {TSPensumUnits} from './enums/TSPensumUnits';
import {TSAbstractPensumEntity} from './TSAbstractPensumEntity';
import {TSDateRange} from './types/TSDateRange';

export class TSAbstractBetreuungspensumEntity extends TSAbstractPensumEntity {

    private _unitForDisplay: TSPensumUnits;
    private _monatlicheBetreuungskosten: number;

    public constructor(
        monatlicheBetreuungskosten: number,
        unitForDisplay?: TSPensumUnits,
        pensum?: number,
        gueltigkeit?: TSDateRange
    ) {
        super(pensum, gueltigkeit);
        this._unitForDisplay = unitForDisplay;
        this._monatlicheBetreuungskosten = monatlicheBetreuungskosten;
    }

    public get unitForDisplay(): TSPensumUnits {
        return this._unitForDisplay;
    }

    public set unitForDisplay(value: TSPensumUnits) {
        this._unitForDisplay = value;
    }

    public get monatlicheBetreuungskosten(): number {
        return this._monatlicheBetreuungskosten;
    }

    public set monatlicheBetreuungskosten(value: number) {
        this._monatlicheBetreuungskosten = value;
    }
}
