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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSFachstelle} from './TSFachstelle';

export default class TSErweiterteBetreuung extends TSAbstractMutableEntity {

    private _erweiterteBeduerfnisse: boolean = false;
    private _fachstelle: TSFachstelle;
    private _erweiterteBeduerfnisseBestaetigt: boolean = false;
    private _keineKesbPlatzierung: boolean;

    public constructor() {
        super();
    }

    public get erweiterteBeduerfnisse(): boolean {
        return this._erweiterteBeduerfnisse;
    }

    public set erweiterteBeduerfnisse(value: boolean) {
        this._erweiterteBeduerfnisse = value;
    }

    public get fachstelle(): TSFachstelle {
        return this._fachstelle;
    }

    public set fachstelle(value: TSFachstelle) {
        this._fachstelle = value;
    }

    public get erweiterteBeduerfnisseBestaetigt(): boolean {
        return this._erweiterteBeduerfnisseBestaetigt;
    }

    public set erweiterteBeduerfnisseBestaetigt(value: boolean) {
        this._erweiterteBeduerfnisseBestaetigt = value;
    }

    public get keineKesbPlatzierung(): boolean {
        return this._keineKesbPlatzierung;
    }

    public set keineKesbPlatzierung(value: boolean) {
        this._keineKesbPlatzierung = value;
    }
}
