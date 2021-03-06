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
import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSGemeindeAntragTyp} from '../../../models/enums/TSGemeindeAntragTyp';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';

@Injectable({
    providedIn: 'root',
})
export class GemeindeAntragService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}gemeindeantrag`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(private readonly http: HttpClient) {
    }

    public getGemeindeAntraege(filter: DVAntragListFilter, sort: {
        predicate?: string,
        reverse?: boolean
    }): Observable<TSGemeindeAntrag[]> {
        let params = new HttpParams();
        if (filter.gemeinde) {
            params = params.append('gemeinde', filter.gemeinde);
        }
        if (filter.gesuchsperiodeString) {
            params = params.append('periode', filter.gesuchsperiodeString);
        }
        if (filter.antragTyp) {
            params = params.append('typ', filter.antragTyp);
        }
        if (filter.status) {
            params = params.append('status', filter.status);
        }
        return this.http.get<TSGemeindeAntrag[]>(this.API_BASE_URL, {
            params,
        }).pipe(
            map(antraege => this.ebeguRestUtil.parseGemeindeAntragList(antraege)),
            map(antraege => this.sortAntraege(antraege, sort)),
        );
    }

    public getTypes(): string[] {
        return [TSGemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN, TSGemeindeAntragTyp.FERIENBETREUUNG];
    }

    public createAntrag(toCreate: { periode: string, antragTyp: string }): Observable<TSGemeindeAntrag[]> {
        return this.http.post<TSGemeindeAntrag[]>(
            `${this.API_BASE_URL}/create/${toCreate.antragTyp}/gesuchsperiode/${toCreate.periode}`,
            toCreate)
            .pipe(map(jaxAntrag => this.ebeguRestUtil.parseGemeindeAntragList(jaxAntrag)));
    }

    private sortAntraege(
        antraege: TSGemeindeAntrag[],
        sort: { predicate?: string; reverse?: boolean },
    ): TSGemeindeAntrag[] {
        switch (sort.predicate) {
            case 'status':
                return sort.reverse ?
                    antraege.sort((a, b) => a.statusString.localeCompare(b.statusString)) :
                    antraege.sort((a, b) => b.statusString.localeCompare(a.statusString));
            case 'gemeinde':
                return sort.reverse ?
                    antraege.sort((a, b) => a.gemeinde.name.localeCompare(b.gemeinde.name)) :
                    antraege.sort((a, b) => b.gemeinde.name.localeCompare(a.gemeinde.name));
            case 'antragTyp':
                return sort.reverse ?
                    antraege.sort((a, b) => a.gemeindeAntragTyp.localeCompare(b.gemeindeAntragTyp)) :
                    antraege.sort((a, b) => b.gemeindeAntragTyp.localeCompare(a.gemeindeAntragTyp));
            case 'gesuchsperiodeString':
                return sort.reverse ?
                    antraege.sort((a, b) =>
                        a.gesuchsperiode.gesuchsperiodeString.localeCompare(b.gesuchsperiode.gesuchsperiodeString)) :
                    antraege.sort((a, b) =>
                        b.gesuchsperiode.gesuchsperiodeString.localeCompare(a.gesuchsperiode.gesuchsperiodeString));
            default:
                return antraege;
        }
    }
}
