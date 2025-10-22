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

import {TSBetreuungsangebotTyp} from '@kibon/shared/model/enums';
import moment from 'moment';
import {TSAntragStatus} from './enums/TSAntragStatus';

export class TSPendenzBetreuung {
    betreuungsNummer: string;

    gemeindeName: string;

    betreuungsId: string;

    gesuchId: string;

    kindId: string;

    name: string;

    vorname: string;

    geburtsdatum: moment.Moment;

    typ: string;

    gesuchsperiodeString: string;

    eingangsdatum: moment.Moment;

    betreuungsangebotTyp: TSBetreuungsangebotTyp;

    institutionName: string;

    institutionId: string;

    gemeindeId: string;

    antragStatus: TSAntragStatus;

    constructor(
        betreuungsNummer?: string,
        gemeindeName?: string,
        betreuungsId?: string,
        gesuchId?: string,
        kindId?: string,
        name?: string,
        vorname?: string,
        geburtsdatum?: moment.Moment,
        typ?: string,
        gesuchsperiodeString?: string,
        eingangsdatum?: moment.Moment,
        betreuungsangebotTyp?: TSBetreuungsangebotTyp,
        institutionName?: string,
        institutionId?: string,
        gemeindeId?: string,
        antragStatus?: TSAntragStatus
    ) {
        this.betreuungsNummer = betreuungsNummer;
        this.gemeindeName = gemeindeName;
        this.betreuungsId = betreuungsId;
        this.gesuchId = gesuchId;
        this.kindId = kindId;
        this.name = name;
        this.vorname = vorname;
        this.geburtsdatum = geburtsdatum;
        this.typ = typ;
        this.gesuchsperiodeString = gesuchsperiodeString;
        this.eingangsdatum = eingangsdatum;
        this.betreuungsangebotTyp = betreuungsangebotTyp;
        this.institutionName = institutionName;
        this.institutionId = institutionId;
        this.gemeindeId = gemeindeId;
        this.antragStatus = antragStatus;
    }
}
