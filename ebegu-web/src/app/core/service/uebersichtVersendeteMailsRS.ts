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

import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {VersendeteMailsFilter} from '../../../admin/component/uebersichtVersendeteMails/versendete-mail-filter/versendete-mail-filter';
import {TSVersendeteMail} from '../../../models/TSVersendeteMail';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '@models/constants';
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
        filter: VersendeteMailsFilter;
    }): Observable<{resultList: TSVersendeteMail[]; totalCount: number}> {
        let httpParams = new HttpParams({
            fromObject: {
                active: params.active,
                receiverOrSubject: params.filter.subjectOrReceiver,
                direction: params.direction,
                page: params.page,
                size: params.size
            }
        });

        if (params.filter.startDate) {
            httpParams = httpParams.set(
                'startDate',
                params.filter.startDate.format(
                    CONSTANTS.BACKEND_DATE_TIME_WITH_SECONDS_FORMAT
                )
            );
        }
        if (params.filter.endDate) {
            httpParams = httpParams.set(
                'endDate',
                params.filter.endDate.format(
                    CONSTANTS.BACKEND_DATE_TIME_WITH_SECONDS_FORMAT
                )
            );
        }

        return this.http
            .get<UebersichtVersendeteMailsResponse>(
                `${this.serviceURL}/allMails`,
                {params: httpParams}
            )
            .pipe(
                map(response => ({
                    resultList:
                        this.ebeguRestUtil.parseTSUebersichtVersendeteMailsList(
                            response.resultList
                        ),
                    totalCount: response.totalCount
                }))
            );
    }
}

type UebersichtVersendeteMailsResponse = {
    resultList: any[];
    totalCount: number;
};
