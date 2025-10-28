import {CommonModule} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    effect,
    inject,
    viewChild
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSelectChange} from '@angular/material/select';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    MatTableDataSource
} from '@angular/material/table';
import {
    MeldungsfensterData,
    MeldungsfensterDataFilter,
    MeldungsfensterStatus,
    MeldungsfensterTableType
} from '@kibon/shared-model-meldungsfenster';
import {AdminUtilMeldungsfensterService} from '@kibon/shared-util-meldungsfenster';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {DvNgConfirmDialogComponent} from '../../../../../../../src/app/core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';

@Component({
    selector: 'lib-admin-feature-meldungsfenster',
    imports: [
        CommonModule,
        TranslateModule,
        MatCell,
        MatCellDef,
        MatColumnDef,
        MatHeaderCell,
        MatHeaderRow,
        MatHeaderRowDef,
        MatPaginator,
        MatRow,
        MatRowDef,
        MatSort,
        MatSortHeader,
        MatTable,
        MatHeaderCellDef,
        SharedModule,
        UIRouterModule
    ],
    templateUrl: './admin-feature-meldungsfenster.component.html',
    styleUrl: './admin-feature-meldungsfenster.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminFeatureMeldungsfensterComponent implements AfterViewInit {
    private meldungsFensterService = inject(AdminUtilMeldungsfensterService);
    private readonly dialog = inject(MatDialog);
    private readonly translate = inject(TranslateService);

    appPropService = inject(SharedUtilApplicationPropertyRsService);
    newTableSort = viewChild.required(MatSort);
    newTablePaginator = viewChild.required(MatPaginator);

    archivedTableSort = viewChild.required(MatSort);
    archivedTablePaginator = viewChild.required(MatPaginator);

    getMeldungsfenster = rxResource({
        stream: () => this.meldungsFensterService.getAllMeldungsfensters()
    });

    public newDataSource = new MatTableDataSource<MeldungsfensterData>();
    public archivedDataSource = new MatTableDataSource<MeldungsfensterData>();

    public displayedColumns$ = this.appPropService.getFrenchEnabled().pipe(
        map(frenchEnabled => {
            return (frenchEnabled ? ['titleDE', 'titleFR'] : ['title']).concat(
                'gueltigAb',
                'gueltigBis',
                'status',
                'actions'
            );
        })
    );
    public filterColumns$: Observable<string[]> = this.displayedColumns$.pipe(
        map(cols => cols.map(col => col + '-filter'))
    );

    public filterValues: {[key: string]: string} = {};
    protected readonly MeldungsfensterTableType = MeldungsfensterTableType;

    constructor() {
        effect(() => {
            const allData = this.getMeldungsfenster.value() ?? [];
            const now = new Date();

            this.newDataSource.data = allData.filter(
                item => new Date(item.gueltigBis) >= now
            );
            this.archivedDataSource.data = allData.filter(
                item => new Date(item.gueltigBis) < now
            );
        });

        this.newDataSource.filterPredicate = this.createFilterPredicate();
        this.archivedDataSource.filterPredicate = this.createFilterPredicate();
    }

    public ngAfterViewInit(): void {
        this.newDataSource.sort = this.newTableSort();
        this.newDataSource.paginator = this.newTablePaginator();
        this.archivedDataSource.sort = this.archivedTableSort();
        this.archivedDataSource.paginator = this.archivedTablePaginator();

        // Set the custom sorting function
        this.newDataSource.sortingDataAccessor = this.sortingDataAccessor;
        this.archivedDataSource.sortingDataAccessor = this.sortingDataAccessor;
    }

    private createFilterPredicate() {
        return (data: MeldungsfensterDataFilter, filter: string) => {
            const filterObject = JSON.parse(filter);
            return Object.keys(filterObject).every(
                (key: keyof MeldungsfensterDataFilter) => {
                    if (!filterObject[key]) {
                        return true;
                    }

                    return data[key]
                        .toString()
                        .toLowerCase()
                        .includes(filterObject[key].toLowerCase());
                }
            );
        };
    }

    public sortingDataAccessor(
        item: MeldungsfensterData,
        property: keyof MeldungsfensterData
    ): any {
        // dates
        if (property === 'gueltigAb' || property === 'gueltigBis') {
            return item[property] ? new Date(item[property]).getTime() : 0;
        }

        // titles
        if (property === 'titleDe' || property === 'titleFr') {
            return item[property]?.toLowerCase() || ''; // Normalize string for case-insensitive sorting
        }

        // Default case
        return item[property] || '';
    }

    public getMeldungsfensterStatus(): MeldungsfensterStatus[] {
        return Object.values(MeldungsfensterStatus);
    }

    public applyFilter(
        tableType: MeldungsfensterTableType,
        column: keyof MeldungsfensterDataFilter,
        event: Event
    ) {
        const filterValue = (event.target as HTMLInputElement).value.trim();

        if (tableType === MeldungsfensterTableType.NEW) {
            this.filterValues[column] = filterValue;
            this.newDataSource.filter = JSON.stringify(this.filterValues); // Trigger filtering for new table
        } else {
            this.filterValues[column] = filterValue;
            this.archivedDataSource.filter = JSON.stringify(this.filterValues); // Trigger filtering for archive table
        }
    }

    public applyStatusFilter(
        tableType: MeldungsfensterTableType,
        event: MatSelectChange
    ) {
        this.filterValues['status'] = event.value;

        if (tableType === MeldungsfensterTableType.NEW) {
            this.newDataSource.filter = JSON.stringify(this.filterValues); // Trigger filtering for new table
        } else {
            this.archivedDataSource.filter = JSON.stringify(this.filterValues); // Trigger filtering for archive table
        }
    }

    public deleteMeldungsfenster(id: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: this.translate.instant('MELDUNGSFENSTER_DIALOG_DELETE')
        };
        this.dialog
            .open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe((res: boolean) => {
                if (res) {
                    this.meldungsFensterService
                        .deleteMeldungsfenster(id)
                        .subscribe(() => {
                            this.getMeldungsfenster.reload();
                        });
                }
            });
    }

    public getDisabledRowClass(row: MeldungsfensterData): string {
        const now = new Date();
        const gueltigAbDate = row.gueltigAb;

        if (gueltigAbDate > now) {
            return 'disabled-row'; // Future entry -> Greyed out
        }

        return ''; // Active entries remain normal
    }
}
