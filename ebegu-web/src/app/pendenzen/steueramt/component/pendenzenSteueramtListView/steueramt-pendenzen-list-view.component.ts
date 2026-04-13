/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
    OnDestroy,
    OnInit,
    inject
} from '@angular/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject, Subject, Subscription} from 'rxjs';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {SearchRS} from '../../../../../gesuch/service/searchRS.rest';
import {TSAntragDTO} from '../../../../../models/TSAntragDTO';
import {LogFactory} from '../../../../../utils/log-factory/LogFactory';
import {DVAntragListFilter} from '../../../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../../../shared/interfaces/DVAntragListItem';
import {DVPaginationEvent} from '../../../../shared/interfaces/DVPaginationEvent';
import {SharedModule} from '../../../../shared/shared.module';

const LOG = LogFactory.createLog('PendenzenListViewComponent');

@Component({
    selector: 'steueramt-pendenzen-list-view',
    templateUrl: './steueramt-pendenzen-list-view.component.html',
    imports: [SharedModule],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SteueramtPendenzenListViewComponent implements OnInit, OnDestroy {
    private readonly gesuchModelManager = inject(GesuchModelManager);
    private readonly $state = inject(StateService);
    private readonly searchRS = inject(SearchRS);

    public hasGemeindenInStatusAngemeldet: boolean = false;

    public data$: BehaviorSubject<DVAntragListItem[]> = new BehaviorSubject<
        DVAntragListItem[]
    >([]);
    public pagination: {
        number: number;
        totalItemCount: number;
        start: number;
    } = {
        number: 20,
        totalItemCount: 0,
        start: 0
    };
    private readonly search: {predicateObject: DVAntragListFilter} = {
        predicateObject: {}
    };

    private sort: {
        predicate?: string;
        reverse?: boolean;
    } = {};

    public initialFilter: DVAntragListFilter = {};

    private readonly unsubscribe$ = new Subject<void>();

    // used to cancel the previous subscription so we don't have two data loads racing each other
    private dataLoadingSubscription: Subscription;

    public ngOnInit(): void {
        this.countData();
        this.loadData();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private countData(): void {
        this.searchRS
            .countAntraege({
                pagination: this.pagination,
                search: this.search,
                sort: this.sort
            })
            .subscribe({
                next: response =>
                    (this.pagination.totalItemCount = response ? response : 0),
                error: error => LOG.error(error)
            });
    }

    private loadData(): void {
        // cancel previous subscription if not closed
        this.dataLoadingSubscription?.unsubscribe();
        this.dataLoadingSubscription = this.searchRS
            .searchAntraege({
                pagination: this.pagination,
                search: this.search,
                sort: this.sort
            })
            .subscribe({
                next: response => {
                    // we lose the "this" if we don't map here
                    this.data$.next(
                        response.antragDTOs.map(antragDto => ({
                            fallNummer: antragDto.fallNummer,
                            antragId: antragDto.antragId,
                            dossierId: antragDto.dossierId,
                            gemeinde: antragDto.gemeinde,
                            status: antragDto.status,
                            familienName: antragDto.familienName,
                            antragTyp: antragDto.antragTyp,
                            periodenString: antragDto.gesuchsperiodeString,
                            hasBesitzer: () => antragDto.hasBesitzer(),
                            isSozialdienst: antragDto.isSozialdienst,
                            eingangsdatumSTV: antragDto.eingangsdatumSTV
                        }))
                    );
                },
                error: error => LOG.error(error)
            });
    }

    public onFilterChange(listFilter: DVAntragListFilter): void {
        this.search.predicateObject = {
            ...listFilter
        };
        this.loadData();
        this.countData();
    }

    public editpendenzJA(pendenz: TSAntragDTO, event: any): void {
        if (pendenz) {
            const isCtrlKeyPressed: boolean = event && event.ctrlKey;
            this.openPendenz(pendenz, isCtrlKeyPressed);
        }
    }

    private openPendenz(pendenz: TSAntragDTO, isCtrlKeyPressed: boolean): void {
        this.gesuchModelManager.clearGesuch();
        const navObj: any = {
            gesuchId: pendenz.antragId
        };
        if (isCtrlKeyPressed) {
            const url = this.$state.href('gesuch.familiensituation', navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go('gesuch.familiensituation', navObj);
        }
    }
    public onPagination(paginationEvent: DVPaginationEvent): void {
        this.pagination.number = paginationEvent.pageSize;
        this.pagination.start = paginationEvent.page * paginationEvent.pageSize;

        this.loadData();
    }

    public onSort(sort: {predicate?: string; reverse?: boolean}): void {
        this.sort = sort;

        this.loadData();
    }

    public calculatePage(): number {
        return Math.floor(this.pagination.start / this.pagination.number);
    }
}
