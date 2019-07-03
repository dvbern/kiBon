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

import {TSMandant} from '../TSMandant';

export enum TSBetreuungsangebotTyp {
    KITA = 'KITA',
    TAGESFAMILIEN = 'TAGESFAMILIEN',
    TAGESSCHULE = 'TAGESSCHULE',
    FERIENINSEL = 'FERIENINSEL'
}

export function getTSBetreuungsangebotTypValuesForMandant(mandant: TSMandant): Array<TSBetreuungsangebotTyp> {
    return getTSBetreuungsangebotTypValuesForMandantIfTagesschulanmeldungen(mandant, true);
}

export function getTSBetreuungsangebotTypValuesForMandantIfTagesschulanmeldungen(mandant: TSMandant, hasTagesschulanmeldungen: boolean): Array<TSBetreuungsangebotTyp> {
    let angebote: Array<TSBetreuungsangebotTyp> = [];
    angebote.push(TSBetreuungsangebotTyp.KITA);
    angebote.push(TSBetreuungsangebotTyp.TAGESFAMILIEN);
    if (mandant.tagesschuleEnabled && hasTagesschulanmeldungen) {
        angebote.push(TSBetreuungsangebotTyp.TAGESSCHULE);
    }
    if (mandant.tagesschuleEnabled) {
        angebote.push(TSBetreuungsangebotTyp.FERIENINSEL);
    }
    return angebote;
}

export function getSchulamtBetreuungsangebotTypValues(): Array<TSBetreuungsangebotTyp> {
    return [
        TSBetreuungsangebotTyp.TAGESSCHULE,
        TSBetreuungsangebotTyp.FERIENINSEL,
    ];
}

export function isSchulamt(status: TSBetreuungsangebotTyp): boolean {
    return status === TSBetreuungsangebotTyp.TAGESSCHULE || status === TSBetreuungsangebotTyp.FERIENINSEL;
}

export function isJugendamt(status: TSBetreuungsangebotTyp): boolean {
    return !isSchulamt(status);
}

/**
 * Gibt true zurueck wenn der gegebene betreuungsangebotTyp in der Liste types gefunden wird
 */
export function isOfAnyBetreuungsangebotTyp(
    betreuungsangebotTyp: TSBetreuungsangebotTyp,
    types: TSBetreuungsangebotTyp[],
): boolean {
    return types.filter(type => betreuungsangebotTyp === type).length > 0;
}
