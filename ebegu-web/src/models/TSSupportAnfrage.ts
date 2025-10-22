/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

export class TSSupportAnfrage {
    private _id: string;

    private _beschreibung: string;

    private _betroffeneFaelle: string;

    private _betroffenePeriode: string;

    private _institution: string;

    private _gemeinde: string;

    public get id(): string {
        return this._id;
    }

    public set id(value: string) {
        this._id = value;
    }

    public get beschreibung(): string {
        return this._beschreibung;
    }

    public set beschreibung(value: string) {
        this._beschreibung = value;
    }

    get gemeinde(): string {
        return this._gemeinde;
    }

    set gemeinde(value: string) {
        this._gemeinde = value;
    }
    get institution(): string {
        return this._institution;
    }

    set institution(value: string) {
        this._institution = value;
    }
    get betroffenePeriode(): string {
        return this._betroffenePeriode;
    }

    set betroffenePeriode(value: string) {
        this._betroffenePeriode = value;
    }
    get betroffeneFaelle(): string {
        return this._betroffeneFaelle;
    }

    set betroffeneFaelle(value: string) {
        this._betroffeneFaelle = value;
    }
}
