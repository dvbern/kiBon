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

import {HttpClient, HttpParams} from '@angular/common/http';
import {inject, Service} from '@angular/core';
import {SortDirection} from '@angular/material/sort';
import {CONSTANTS} from '@models/constants';
import {MomentUtil} from '@utils/moment';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {
    BatchjobHistoryEntry,
    BatchjobHistoryFilter,
    BatchjobHistoryPage,
    BatchjobHistorySortColumn,
    BatchjobStatus
} from './batchjob-history';

@Service()
export class BatchjobHistoryService {
    private readonly http = inject(HttpClient);
    private readonly serviceURL = `${CONSTANTS.REST_API}batchjobHistory`;

    public getAll(params: {
        active: BatchjobHistorySortColumn;
        direction: SortDirection;
        page: number;
        size: number;
        filter: BatchjobHistoryFilter;
    }): Observable<BatchjobHistoryPage> {
        let httpParams = new HttpParams({
            fromObject: {
                active: params.active,
                direction: params.direction,
                nameOrResult: params.filter.nameOrResult,
                status: params.filter.status,
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
            .get<BatchjobHistoryResponse>(`${this.serviceURL}/all`, {
                params: httpParams
            })
            .pipe(
                map(response => ({
                    resultList: response.resultList.map(this.parseEntry),
                    totalCount: response.totalCount
                }))
            );
    }

    private parseEntry(
        entry: BatchjobHistoryEntryFromServer
    ): BatchjobHistoryEntry {
        return {
            id: entry.id,
            name: entry.name,
            status: entry.status,
            timestampStarted: MomentUtil.localDateTimeToMoment(
                entry.timestampStarted
            ),
            timestampFinished: MomentUtil.localDateTimeToMoment(
                entry.timestampFinished
            ),
            result: entry.result
        };
    }
}

type BatchjobHistoryResponse = {
    resultList: BatchjobHistoryEntryFromServer[];
    totalCount: number;
};

type BatchjobHistoryEntryFromServer = {
    id: string;
    name: string;
    status: BatchjobStatus;
    timestampStarted: string;
    timestampFinished?: string;
    result?: string;
};
