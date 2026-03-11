/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    inject,
    signal,
    computed,
    effect,
    linkedSignal
} from '@angular/core';
import {rxResource, toSignal} from '@angular/core/rxjs-interop';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PageEvent} from '@angular/material/paginator';
import {MatSort, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TSGemeinde} from '@kibon/shared/model/entity';
import {TSRole} from '@kibon/shared/model/enums';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {from, Subject} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSPagination} from '../../../models/dto/TSPagination';
import {DVErrorMessageCallback} from '../../../models/DVErrorMessageCallback';
import {
    getTSMitteilungsStatusForFilter,
    TSMitteilungStatus
} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTyp} from '../../../models/enums/TSMitteilungTyp';
import {TSMitteilungTypes} from '../../../models/enums/TSMitteilungTypes';
import {TSVerantwortung} from '../../../models/enums/TSVerantwortung';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';

import {TSMitteilung} from '../../../models/TSMitteilung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgMitteilungResultDialogComponent} from '../../core/component/dv-ng-mitteilung-result-dialog/dv-ng-mitteilung-result-dialog.component';
import {TSDemoFeature} from '@kibon/shared/model/enums';
import {ErrorServiceX} from '../../core/errors/service/ErrorServiceX';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {MitteilungRS} from '../../core/service/mitteilungRS.rest';
import {DVPosteingangFilter} from '../../shared/interfaces/DVPosteingangFilter';
import {StateStoreService} from '../../shared/services/state-store.service';
import {PosteingangService} from '../service/posteingang.service';

@Component({
    selector: 'posteingang-view',
    templateUrl: './posteingang-view.component.html',
    styleUrls: ['./posteingang-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PosteingangViewComponent
    implements OnInit, OnDestroy, AfterViewInit
{
    private readonly mitteilungRS = inject(MitteilungRS);
    private readonly $state = inject(StateService);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly gemeindeRS = inject(GemeindeRS);
    private readonly stateStore = inject(StateStoreService);
    private readonly benutzerRS = inject(BenutzerRSX);
    private readonly changeDetectorRef = inject(ChangeDetectorRef);
    private readonly posteingangService = inject(PosteingangService);
    private readonly dialog = inject(MatDialog);
    private readonly translate = inject(TranslateService);
    private readonly errorService = inject(ErrorServiceX);

    @ViewChild(MatSort) private readonly matSort: MatSort;

    private readonly unsubscribe$ = new Subject<void>();
    public page = signal(0);
    public pageSize = signal(20);

    public sender = signal(null);
    public gemeinde = signal(null);
    public fallNummer = signal(null);
    public familienName = signal(null);
    public subject = signal(null);
    public sentDatum = signal(null);
    public empfaenger = signal(null);
    public empfaengerVerantwortung = signal(null);
    public mitteilungStatus = signal(null);

    public filterPredicate = linkedSignal<DVPosteingangFilter>(() => ({
        messageTypes: [
            TSMitteilungTypes.BETREUUNGSMITTEILUNG,
            TSMitteilungTypes.MITTEILUNG,
            TSMitteilungTypes.NEUEVERANLAGUNGMITTEILUNG
        ],
        sender: this.sender(),
        gemeinde: this.gemeinde(),
        fallNummer: this.fallNummer(),
        familienName: this.familienName(),
        subject: this.subject(),
        sentDatum: this.sentDatum(),
        empfaenger: this.empfaenger(),
        empfaengerVerantwortung: this.empfaengerVerantwortung(),
        mitteilungStatus: this.mitteilungStatus()
    }));
    public includeClosed = signal(false);
    public displayedCollection: MatTableDataSource<TSMitteilung>;

    private mitteilungenParams = computed(() => ({
        pagination: {
            number: this.pageSize(),
            start: this.page() * this.pageSize()
        },
        search: {predicateObject: this.filterPredicate()},
        sort: this.sort()
    }));

    mitteilungen = rxResource({
        params: () => ({
            mitteilung: this.mitteilungenParams(),
            include: this.includeClosed(),
            filter: this.filterPredicate()
        }),
        stream: ({params}) =>
            from(
                this.mitteilungRS.searchMitteilungen(
                    {
                        ...params.mitteilung,
                        search: {predicateObject: params.filter}
                    },
                    params.include
                )
            )
    });

    private principal = toSignal(this.authServiceRS.principal$, {
        initialValue: null
    });

    resetEmpfaengerFilter(): void {
        const principal = this.principal();
        if (!principal) {
            return;
        }

        if (principal.hasOneOfRoles([TSRole.SUPER_ADMIN])) {
            return;
        }

        this.empfaenger.set(principal.getFullName());
    }

    private debouncedFilter = signal<DVPosteingangFilter>(
        this.filterPredicate()
    );
    private filterDebounceTimeout: any;

    public gemeindenList = toSignal(
        this.gemeindeRS.getGemeindenForPrincipal$(),
        {initialValue: [] as TSGemeinde[]}
    );

    public sortedGemeindenList = computed(() => {
        return [...this.gemeindenList()].sort((a, b) =>
            a.name.localeCompare(b.name)
        );
    });

    private readonly timeoutMS = 700;
    public readonly allColumns = [
        'sender',
        'gemeinde',
        'fallNummer',
        'familienName',
        'subject',
        'sentDatum',
        'empfaenger',
        'empfaengerVerantwortung',
        'mitteilungStatus',
        'actions'
    ];

    public filterColumns: string[] = [
        'sender-filter',
        'gemeinde-filter',
        'fallNummer-filter',
        'familienName-filter',
        'subject-filter',
        'sentDatum-filter',
        'empfaenger-filter',
        'empfaengerVerantwortung-filter',
        'mitteilungStatus-filter',
        'actions-filter'
    ];

    private readonly hiddenColumnsUDInstituion: string[] = [
        'empfaenger',
        'empfaengerVerantwortung'
    ];

    private readonly hiddenColumnsUD: string[] = ['familienName'];

    // Liste die im Gui angezeigt wird
    public displayedColumns: string[];
    public pagination: TSPagination = new TSPagination();

    public totalResultCount: number = 0;

    public numberOfPages: number = 1;
    public paginationItems: number[];
    public initialEmpfaenger: TSBenutzerNoDetails;

    // StateStore Properties
    public initialFilter: DVPosteingangFilter = {
        messageTypes: [
            TSMitteilungTypes.BETREUUNGSMITTEILUNG,
            TSMitteilungTypes.MITTEILUNG,
            TSMitteilungTypes.NEUEVERANLAGUNGMITTEILUNG
        ]
    };
    private readonly sortId = 'posteingangId-sort';
    private readonly filterId = 'posteingangId-filter';
    private sort = signal<{predicate?: string; reverse?: boolean}>({});

    public readonly mutationsMeldungDemoFeature =
        TSDemoFeature.ALLE_MUTATIONSMELDUNGEN_VERFUEGEN;

    constructor() {
        effect(() => {
            const filter = this.filterPredicate();

            this.benutzerRS.getAllBenutzerBgTsOrGemeinde().then(response => {
                this.initialEmpfaenger = EbeguUtil.findUserByNameInList(
                    filter?.empfaenger,
                    response
                );
                this.changeDetectorRef.markForCheck();
            });
        });

        effect(() => {
            const filter = this.filterPredicate();

            clearTimeout(this.filterDebounceTimeout);
            this.filterDebounceTimeout = setTimeout(() => {
                this.debouncedFilter.set(filter);
            }, this.timeoutMS);
        });

        effect(() => {
            const result = this.mitteilungen.value();
            if (!this.mitteilungen) {
                return;
            }
            if (!this.displayedCollection) {
                return;
            }
            this.displayedCollection.data = result?.mitteilungen ?? [];
        });
    }

    public ngOnInit(): void {
        this.initDisplayedColumns();
        this.initSort();
        this.initFilter();
        this.resetEmpfaengerFilter();
    }

    public ngAfterViewInit(): void {
        this.displayedCollection = new MatTableDataSource<TSMitteilung>([]);
        this.initMatSort();
    }

    public ngOnDestroy(): void {
        this.storeFilterSortStates();
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public showAlleMutationenBearbeitenButton() {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeOrBGOrTSRoles().concat(TSRole.SUPER_ADMIN)
        );
    }

    public addZerosToFallNummer(fallnummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallnummer);
    }

    public gotoMitteilung(mitteilung: TSMitteilung): void {
        this.$state.go('mitteilungen.view', {
            dossierId: mitteilung.dossier.id,
            fallId: mitteilung.dossier.fall.id
        });
    }

    public getVerantwortungList(): Array<string> {
        return [
            TSVerantwortung.VERANTWORTUNG_BG,
            TSVerantwortung.VERANTWORTUNG_TS
        ];
    }

    public getMitteilungsStatus(): Array<TSMitteilungStatus> {
        return getTSMitteilungsStatusForFilter();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isSozialdienst(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle()
        );
    }

    public isSozialdienstOrInstitution(): boolean {
        return (
            this.isSozialdienst() ||
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        );
    }

    public isSuperAdminOrGemeinde(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtRole()
        );
    }

    public filterEmpfaenger(empfaenger: TSBenutzerNoDetails | null): void {
        if (empfaenger != null) {
            this.empfaenger.set(empfaenger.getFullName());
        } else {
            this.empfaenger.set(null);
        }
    }

    public checkbox($event: boolean): void {
        this.includeClosed.set($event);
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page.set(pageEvent.pageIndex);
        this.pageSize.set(pageEvent.pageSize);
        this.pagination.number = pageEvent.pageSize;
        this.pagination.start = this.page() * pageEvent.pageSize;
    }

    public sortData(sortEvent: Sort): void {
        const predicate =
            sortEvent.direction.length > 0 ? sortEvent.active : null;
        const reverse = sortEvent.direction === 'asc';

        this.sort.set({predicate, reverse});
    }

    private initFilter(): void {
        const initial =
            this.filterId && this.stateStore.has(this.filterId)
                ? (this.stateStore.get(this.filterId) as DVPosteingangFilter)
                : {...this.initialFilter};

        this.sender.set(initial.sender);
        this.gemeinde.set(initial.gemeinde);
        this.fallNummer.set(initial.fallNummer);
        this.familienName.set(initial.familienName);
        this.subject.set(initial.subject);
        this.sentDatum.set(initial.sentDatum);
        this.empfaenger.set(initial.empfaenger);
        this.empfaengerVerantwortung.set(initial.empfaengerVerantwortung);
        this.mitteilungStatus.set(initial.mitteilungStatus);
        // this.filterPredicate.set(initial);
    }

    private storeFilterSortStates() {
        if (this.sort().predicate) {
            this.stateStore.store(this.sortId, this.sort());
        } else {
            this.stateStore.delete(this.sortId);
            this.stateStore.delete(this.filterId);
        }

        this.stateStore.store(this.filterId, this.filterPredicate());
    }

    private initSort(): void {
        if (this.stateStore.has(this.sortId)) {
            const stored = this.stateStore.get(this.sortId) as {
                predicate?: string;
                reverse?: boolean;
            };
            this.sort.set({
                predicate: stored.predicate,
                reverse: stored.reverse
            });
        }
    }

    private initMatSort(): void {
        const currentSort = this.sort();

        this.matSort.active = currentSort.predicate ?? '';
        this.matSort.direction = currentSort.reverse ? 'asc' : 'desc';
    }

    private initDisplayedColumns(): void {
        if (!this.isSozialdienstOrInstitution()) {
            this.displayedColumns = this.allColumns;
            return;
        }
        this.displayedColumns = this.allColumns.filter(
            column => !this.hiddenColumnsUDInstituion.includes(column)
        );
        if (this.isSozialdienst()) {
            this.displayedColumns = this.displayedColumns.filter(
                column => !this.hiddenColumnsUD.includes(column)
            );
        }
        this.filterColumns = this.displayedColumns.map(
            column => `${column}-filter`
        );
    }

    public resetFilter(): void {
        this.sender.set(null);
        this.gemeinde.set(null);
        this.fallNummer.set(null);
        this.familienName.set(null);
        this.subject.set(null);
        this.sentDatum.set(null);
        this.empfaengerVerantwortung.set(null);
        this.mitteilungStatus.set(null);
        this.page.set(0);
        this.sort.set({predicate: null, reverse: false});
        this.resetEmpfaengerFilter();
        this.mitteilungen.reload();
    }

    public setUngelesen(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS.setMitteilungUngelesen(mitteilung.id).then(() => {
            this.getMitteilungenCount();
        });
    }

    public setIgnoriert(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS
            .setMitteilungIgnoriert(mitteilung.id)
            .then(() => {
                this.getMitteilungenCount();
            })
            .then(() => {
                const errorMessageCallback = new DVErrorMessageCallback(
                    this.translate.instant('RUECKGAENGIG_MACHEN'),
                    () => this.setGelesen(mitteilung)
                );
                this.errorService.addMesageAsInfo(
                    this.translate.instant('MESSAGE_IGNORED'),
                    errorMessageCallback
                );
            });
    }

    private resetMitteilungRevertInfo() {
        this.errorService.clearAll();
    }

    public setGelesen(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS.setMitteilungGelesen(mitteilung.id).then(() => {
            this.getMitteilungenCount();
        });
    }

    public isStatusGelesen(mitteilung: TSMitteilung): boolean {
        return mitteilung.mitteilungStatus === TSMitteilungStatus.GELESEN;
    }

    private getMitteilungenCount(): void {
        this.posteingangService.posteingangChanged();
    }

    public alleMutationsmeldungVerfuegen(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: 'ALLE_MUTATIONSMELDUNGEN_BEARBEITEN_FRAGE'
        };
        this.dialog
            .open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(answer => {
                if (answer !== true) {
                    return;
                }
                dialogConfig.data =
                    this.getOpenTsBetreuungsmitteilungenOfTable();
                dialogConfig.disableClose = true;
                this.dialog
                    .open(DvNgMitteilungResultDialogComponent, dialogConfig)
                    .afterClosed()
                    .subscribe(
                        () => {
                            this.getMitteilungenCount();
                        },
                        () => {}
                    );
            });
    }

    private getOpenTsBetreuungsmitteilungenOfTable(): TSBetreuungsmitteilung[] {
        return this.displayedCollection.data
            .filter(
                mitteilung =>
                    mitteilung.mitteilungTyp ===
                        TSMitteilungTyp.BETREUUNGSMITTEILUNG &&
                    mitteilung.mitteilungStatus !== TSMitteilungStatus.ERLEDIGT
            )
            .map(mitteilung => mitteilung as TSBetreuungsmitteilung);
    }

    public canBeIgnored(mitteilung: TSMitteilung): boolean {
        return (
            !mitteilung.isErledigt() &&
            !mitteilung.isIgnoriert() &&
            mitteilung.isNeueVeranlagung()
        );
    }

    public canMitteilungStatusBeReverted(mitteilung: TSMitteilung): boolean {
        return mitteilung.isIgnoriert();
    }
}
