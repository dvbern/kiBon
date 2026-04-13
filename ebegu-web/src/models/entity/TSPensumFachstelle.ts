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

import {TSFachstellenTyp} from '../enums/TSFachstellenTyp';
import {TSGruendeZusatzleistung} from '../enums/TSGruendeZusatzleistung';
import {TSIntegrationTyp} from '../enums/TSIntegrationTyp';
import {TSAbstractIntegerPensumEntity} from './TSAbstractIntegerPensumEntity';
import {TSFachstelle} from './TSFachstelle';

export class TSPensumFachstelle extends TSAbstractIntegerPensumEntity {
    private _fachstelle: TSFachstelle;
    private _integrationTyp: TSIntegrationTyp;
    private _gruendeZusatzleistung: TSGruendeZusatzleistung;

    /**
     * Von der Gemeinde bezeichnete Fachstelle.
     */
    private _description: string;

    public constructor() {
        super();
    }

    public get fachstelle(): TSFachstelle {
        return this._fachstelle;
    }

    public set fachstelle(value: TSFachstelle) {
        this._fachstelle = value;
    }

    public get integrationTyp(): TSIntegrationTyp {
        return this._integrationTyp;
    }

    public set integrationTyp(value: TSIntegrationTyp) {
        this._integrationTyp = value;
    }

    public get gruendeZusatzleistung(): TSGruendeZusatzleistung {
        return this._gruendeZusatzleistung;
    }

    public set gruendeZusatzleistung(value: TSGruendeZusatzleistung) {
        this._gruendeZusatzleistung = value;
    }

    public get description(): string {
        return this._description;
    }

    public set description(value: string) {
        this._description = value;
    }

    public isComplete(fachstellenTyp: TSFachstellenTyp): boolean {
        if (fachstellenTyp === TSFachstellenTyp.LUZERN) {
            return (
                this.integrationTyp !== null &&
                this.integrationTyp !== undefined &&
                this.gueltigkeit.gueltigAb !== null &&
                this.gueltigkeit.gueltigAb !== undefined
            );
        }

        if (
            this.integrationTyp ===
                TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION &&
            (this.gruendeZusatzleistung === null ||
                this.gruendeZusatzleistung === undefined)
        ) {
            return false;
        }

        return (
            this.fachstelle !== null &&
            this.fachstelle !== undefined &&
            this.integrationTyp !== null &&
            this.integrationTyp !== undefined &&
            this.gueltigkeit.gueltigAb !== null &&
            this.gueltigkeit.gueltigAb !== undefined &&
            this.pensum !== null &&
            this.pensum !== undefined
        );
    }

    /**
     * @return Ob das Attribut "description" einen nicht leeren Text enthält.
     */
    public hasDescription() {
        return (
            null !== this.description &&
            undefined !== this.description &&
            0 < this.description.length
        );
    }
}
