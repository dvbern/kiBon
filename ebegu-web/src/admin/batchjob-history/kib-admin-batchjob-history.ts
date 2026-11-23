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

import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    linkedSignal,
    signal
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {MatDialog} from '@angular/material/dialog';
import {PageEvent} from '@angular/material/paginator';
import {Sort, SortDirection} from '@angular/material/sort';
import {Moment} from 'moment';
import {SharedModule} from '../../app/shared/shared.module';
import {KibAdminBatchjobHistoryDetail} from './batchjob-history-detail/kib-admin-batchjob-history-detail';
import {KibAdminBatchjobHistoryFilter} from './batchjob-history-filter/kib-admin-batchjob-history-filter';
import {
    BatchjobHistoryEntry,
    BatchjobHistoryFilter,
    BatchjobHistorySortColumn,
    BatchjobStatus
} from './batchjob-history';
import {BatchjobHistoryService} from './batchjob-history.service';

type BatchjobHistorySort = {
    active: BatchjobHistorySortColumn;
    direction: SortDirection;
};

type Pagination = {page: number; size: number};

const INITIAL_SORT: BatchjobHistorySort = {
    active: 'timestampStarted',
    direction: 'desc'
};

const DEFAULT_PAGE_SIZE = 10;

const DATE_TIME_FORMAT = 'DD.MM.YYYY HH:mm:ss';

@Component({
    selector: 'kib-admin-batchjob-history',
    imports: [SharedModule, KibAdminBatchjobHistoryFilter],
    templateUrl: './kib-admin-batchjob-history.html',
    styleUrls: ['./kib-admin-batchjob-history.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KibAdminBatchjobHistory {
    private readonly batchjobHistoryService = inject(BatchjobHistoryService);
    private readonly dialog = inject(MatDialog);

    protected readonly INITIAL_SORT = INITIAL_SORT;
    protected readonly PAGE_SIZE_OPTIONS = [10, 25, 100];
    protected readonly DISPLAYED_COLUMNS: BatchjobHistorySortColumn[] = [
        'name',
        'timestampStarted',
        'timestampFinished',
        'status',
        'result'
    ];

    private readonly sort = signal<BatchjobHistorySort>(INITIAL_SORT);
    private readonly filter = signal<BatchjobHistoryFilter>({
        nameOrResult: '',
        status: [],
        startDate: null,
        endDate: null
    });
    // jumps back to the first page whenever the sort order or the filter changes
    protected readonly pagination = linkedSignal<unknown, Pagination>({
        source: () => [this.sort(), this.filter()],
        computation: (_, previous) => ({
            page: 0,
            size: previous?.value.size ?? DEFAULT_PAGE_SIZE
        })
    });

    private readonly resource = rxResource({
        params: () => ({
            sort: this.sort(),
            pagination: this.pagination(),
            filter: this.filter()
        }),
        stream: ({params: {sort, pagination, filter}}) =>
            this.batchjobHistoryService.getAll({
                filter,
                active: sort.active,
                direction: sort.direction,
                ...pagination
            })
    });

    protected readonly totalResults = computed(
        () =>
            this.resource.hasValue()
                ? this.resource.value().totalCount
                : undefined,
        {equal: (prev, cur) => (cur === undefined ? true : prev === cur)}
    );

    // while loading, the previous rows are kept to avoid flickering due to an empty table
    protected readonly rows = linkedSignal<
        TableBatchjobHistory[] | undefined,
        TableBatchjobHistory[]
    >({
        source: () => {
            if (this.resource.hasValue()) {
                return this.resource.value().resultList.map(toTableRow);
            }
            return this.resource.isLoading() ? undefined : [];
        },
        computation: (rows, previous) => rows ?? previous?.value ?? []
    });

    protected onFilterChange(filter: BatchjobHistoryFilter): void {
        this.filter.set(filter);
    }

    protected onSortChange(sort: Sort): void {
        this.sort.set({
            active: sort.active as BatchjobHistorySortColumn,
            direction: sort.direction
        });
    }

    protected onPageChange(event: PageEvent): void {
        this.pagination.set({page: event.pageIndex, size: event.pageSize});
    }

    protected openDetail(row: TableBatchjobHistory): void {
        this.dialog.open(KibAdminBatchjobHistoryDetail, {
            data: row,
            autoFocus: 'dialog',
            minWidth: '50vw',
            minHeight: '30vh'
        });
    }

    protected preventDefaultAndOpenDetail(
        event: Event,
        row: TableBatchjobHistory
    ): void {
        event.preventDefault();
        this.openDetail(row);
    }
}

export interface TableBatchjobHistory {
    name: string;
    timestampStarted: string;
    timestampFinished: string;
    status: BatchjobStatus;
    result: string;
}

function toTableRow(entry: BatchjobHistoryEntry): TableBatchjobHistory {
    return {
        name: entry.name,
        timestampStarted: formatMoment(entry.timestampStarted),
        timestampFinished: formatMoment(entry.timestampFinished),
        status: entry.status,
        result: entry.result ?? ''
    };
}

function formatMoment(value: Moment | undefined): string {
    return value ? value.format(DATE_TIME_FORMAT) : '-';
}
