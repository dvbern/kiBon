/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {TSDateRange} from './entity/TSDateRange';
import {TSInstitution} from './entity/TSInstitution';
import {TSTraegerschaft} from './entity/TSTraegerschaft';
import {TSRole} from './enums/TSRole';
import {TSSozialdienst} from './sozialdienst/TSSozialdienst';
import {TSBerechtigung} from './TSBerechtigung';

export class TSFutureBerechtigung extends TSBerechtigung {
    private _predecessor: TSBerechtigung;

    public constructor(
        gueltigkeit?: TSDateRange,
        role?: TSRole,
        traegerschaft?: TSTraegerschaft,
        institution?: TSInstitution,
        sozialdienst?: TSSozialdienst,
        predecessor?: TSBerechtigung
    ) {
        super(gueltigkeit);
        this.role = role;
        this.traegerschaft = traegerschaft;
        this.institution = institution;
        this.sozialdienst = sozialdienst;
        this._predecessor = predecessor;
    }

    public set predecessor(value: TSBerechtigung) {
        this._predecessor = value;
    }

    public get predecessor(): TSBerechtigung {
        return this._predecessor;
    }
}
