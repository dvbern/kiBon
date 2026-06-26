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

import {TSGesuchsperiode} from '../../../../models/entity/TSGesuchsperiode';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';

export interface DVPendenzenBetreuungenAntragList {
    betreuungsNummer?: string;
    gemeinde?: string;
    name?: string;
    vorname?: string;
    geburtsdatum?: Date;
    antragTyp?: string;
    periode?: TSGesuchsperiode;
    periodenString?: string;
    eingangsdatum?: Date;
    angebote?: TSBetreuungsangebotTyp[];
    institutionen?: string[];
    hasBesitzer?(): boolean;
}
