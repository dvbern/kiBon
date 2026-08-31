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

import {
    Component,
    computed,
    DestroyRef,
    effect,
    inject,
    OnInit,
    signal,
    viewChild,
    ChangeDetectionStrategy
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import moment from 'moment';
import {UebersichtVersendeteMailsRS} from '../../../app/core/service/uebersichtVersendeteMailsRS';
import {rxResource, takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {VersendeteMailDetail} from './versendete-mail-detail/versendete-mail-detail';
import {VersendeteMailsFilter} from './versendete-mail-filter/versendete-mail-filter';

const initialSort: Sort = {active: 'zeitpunktVersand', direction: 'desc'};

@Component({
    selector: 'dv-uebersicht-Versendete-Mails',
    templateUrl: './uebersichtVersendeteMails.component.html',
    styleUrls: ['./uebersichtVersendeteMails.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class UebersichtVersendeteMailsComponent implements OnInit {
    private readonly uebersichtVersendeteMailsRS = inject(
        UebersichtVersendeteMailsRS
    );
    private readonly destroyRef = inject(DestroyRef);
    private readonly dialog = inject(MatDialog);

    private sort = viewChild(MatSort);
    private paginator = viewChild(MatPaginator);

    readonly DEFAULT_PAGE_SIZE = 10;
    readonly DEFAULT_PAGE = 0;
    readonly DISPLAYED_COLUMNS: string[] = [
        'zeitpunktVersand',
        'empfaengerAdresse',
        'betreff'
    ];
    readonly DATA_SOURCE: MatTableDataSource<TableUebersichtVersendeteMails> =
        new MatTableDataSource([]);

    private sortValue = signal<Sort>(initialSort);
    private paginationValue = signal<{page: number; size: number}>({
        page: this.DEFAULT_PAGE,
        size: this.DEFAULT_PAGE_SIZE
    });
    filter = signal<VersendeteMailsFilter>({
        subjectOrReceiver: '',
        endDate: null,
        startDate: null
    });
    private resource = rxResource({
        params: () => ({
            sort: this.sortValue(),
            pagination: this.paginationValue(),
            filter: this.filter()
        }),
        stream: ({params: {sort, pagination, filter}}) =>
            this.uebersichtVersendeteMailsRS.getAllMails({
                filter,
                active: sort.active,
                direction: sort.direction,
                ...pagination
            })
    });
    totalResults = computed(
        () => {
            return this.resource.hasValue()
                ? this.resource.value().totalCount
                : undefined;
        },
        {equal: (prev, cur) => (cur === undefined ? true : prev === cur)}
    );

    constructor() {
        effect(() => {
            // when the params change, the value is emptied. We return early to avoid flickering due to an empty array
            if (this.resource.isLoading()) {
                return;
            }
            this.DATA_SOURCE.data = this.resource.hasValue()
                ? this.resource.value().resultList.map(
                      mail =>
                          ({
                              ...mail,
                              zeitpunktVersand: this.parseMomentToString(
                                  mail.zeitpunktVersand
                              )
                          }) satisfies TableUebersichtVersendeteMails
                  )
                : [];
        });
    }

    public ngOnInit(): void {
        this.sort()
            .sortChange.pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(sort => this.sortValue.set(sort));
        this.paginator()
            .page.pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(event =>
                this.paginationValue.set({
                    page: event.pageIndex,
                    size: event.pageSize
                })
            );
    }

    private parseMomentToString(versand: moment.Moment): string {
        return versand.format('DD.MM.YYYY HH:mm:ss');
    }

    openMail(row: TableUebersichtVersendeteMails): void {
        const dialogConfig =
            new MatDialogConfig<TableUebersichtVersendeteMails>();
        dialogConfig.data = row;
        dialogConfig.autoFocus = 'dialog';
        this.dialog.open(VersendeteMailDetail, dialogConfig);
    }

    showWarning(): boolean {
        return this.filter()?.subjectOrReceiver?.length > 0;
    }

    preventDefaultAndOpenMail(
        event: Event,
        row: TableUebersichtVersendeteMails
    ) {
        event.preventDefault();
        this.openMail(row);
    }
}

export interface TableUebersichtVersendeteMails {
    zeitpunktVersand: string;
    empfaengerAdresse: string;
    betreff: string;
    body: string;
}
