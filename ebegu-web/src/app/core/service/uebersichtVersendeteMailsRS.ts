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

import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSVersendeteMail} from '../../../models/TSVersendeteMail';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {SortDirection} from '@angular/material/sort';

@Injectable({
    providedIn: 'root'
})
export class UebersichtVersendeteMailsRS {
    http = inject(HttpClient);
    public readonly serviceURL = `${CONSTANTS.REST_API}versendeteMails`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public getAllMails(params: {
        active: string;
        direction: SortDirection;
        page: number;
        size: number;
        filter: string;
    }): Observable<{resultList: TSVersendeteMail[]; totalCount: number}> {
        return this.http.get(`${this.serviceURL}/allMails`, {params}).pipe(
            map((response: any) => ({
                resultList:
                    this.ebeguRestUtil.parseTSUebersichtVersendeteMailsList(
                        response.resultList
                    ),
                totalCount: response.totalCount
            }))
        );
    }
}
