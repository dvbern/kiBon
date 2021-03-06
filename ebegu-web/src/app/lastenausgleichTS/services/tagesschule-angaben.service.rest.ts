/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenInstitution} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitution';
import {TSLastenausgleichTagesschuleAngabenInstitutionContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenInstitutionContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root',
})
export class TagesschuleAngabenRS {

    private readonly ebeguRestUtils = new EbeguRestUtil();

    private readonly apiUrl = `${CONSTANTS.REST_API}gemeindeantrag`;

    public constructor(private readonly http: HttpClient) {
    }

    public getAllVisibleTagesschulenAngabenForTSLastenausgleich(lastenausgleichId: string): Observable<TSLastenausgleichTagesschuleAngabenInstitutionContainer[]> {
        return this.http.get<TSLastenausgleichTagesschuleAngabenInstitution[]>(`${this.apiUrl}/${lastenausgleichId}/tagesschulenantraege`)
            .pipe(
                map(lastenausgleichAngabenList => this.ebeguRestUtils.parseLastenausgleichTagesschuleAngabenInstitutionContainerList(
                    lastenausgleichAngabenList)),
            );
    }
}
