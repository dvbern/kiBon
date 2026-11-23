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

import {Moment} from 'moment';

export const BATCHJOB_STATUSES = ['RUNNING', 'SUCCESS', 'FAILED'] as const;

export type BatchjobStatus = (typeof BATCHJOB_STATUSES)[number];

export type BatchjobHistoryEntry = {
    id: string;
    name: string;
    status: BatchjobStatus;
    timestampStarted: Moment;
    timestampFinished?: Moment;
    result?: string;
};

export type BatchjobHistoryFilter = {
    nameOrResult: string;
    status: BatchjobStatus[];
    startDate: Moment | null;
    endDate: Moment | null;
};

export type BatchjobHistorySortColumn = keyof Omit<BatchjobHistoryEntry, 'id'>;

export type BatchjobHistoryPage = {
    resultList: BatchjobHistoryEntry[];
    totalCount: number;
};
