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
    OnInit,
    ViewChild,
    DestroyRef,
    inject
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, Sort, SortDirection} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import moment from 'moment';
import {of, Subject, timer} from 'rxjs';
import {switchMap, take} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSPaginationResultDTO} from '../../../models/dto/TSPaginationResultDTO';
import {TSPublicAppConfig} from '../../../models/einstellung/TSPublicAppConfig';
import {TSGemeinde} from '../../../models/entity/TSGemeinde';
import {TSGeneratedDokumentTyp} from '../../../models/enums/TSGeneratedDokumentTyp';
import {
    TSZahlungsauftragsstatus,
    TSZahlungsstatus,
    TSZahlungsauftrag,
    TSZahlungslaufTyp
} from '@models/zahlung';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '@app/shared/component/remove-dialog';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '@utils/log';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {DvSimpleTableColumnDefinition} from '../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {StateStoreService} from '../../shared/services/state-store.service';
import {ZahlungService} from '@app/zahlung/service';

const LOG = LogFactory.createLog('ZahlungsauftragViewXComponent');

@Component({
    selector: 'zahlungsauftrag-view',
    templateUrl: './zahlungsauftrag-view-x.component.html',
    styleUrls: ['./zahlungsauftrag-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ZahlungsauftragViewXComponent implements OnInit, AfterViewInit {
    private readonly destroyRef = inject(DestroyRef);
    private readonly zahlungRS = inject(ZahlungService);
    private readonly $state = inject(StateService);
    private readonly downloadRS = inject(DownloadRS);
    private readonly applicationPropertyRS = inject(
        ApplicationPropertyRsService
    );
    private readonly reportRS = inject(ReportRS);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly translate = inject(TranslateService);
    private readonly gemeindeRS = inject(GemeindeRS);
    private readonly uiRouterGlobals = inject(UIRouterGlobals);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly dialog = inject(MatDialog);
    private readonly transition = inject(TransitionService);
    private readonly stateStore = inject(StateStoreService);
    private readonly errorService = inject(ErrorService);

    @ViewChild(MatSort) public sort: MatSort;
    @ViewChild(MatPaginator) private readonly paginator: MatPaginator;

    public datasource: MatTableDataSource<any> = new MatTableDataSource<any>(
        []
    );

    public zahlungsauftragToEdit: TSZahlungsauftrag;
    public zahlungsAuftraege: TSZahlungsauftrag[] = [];

    public zahlungslaufTyp: TSZahlungslaufTyp;
    public beschrieb: string;
    public datumGeneriert: moment.Moment;
    public itemsByPage: number = 12;
    public testMode: boolean = false;
    public checkboxAuszahlungInZukunft: boolean = false;
    public auszahlungInZukunft: boolean = false;
    public gemeinde: TSGemeinde;
    // Anzuzeigende Gemeinden fuer den gewaehlten Zahlungslauftyp
    public gemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die normalen Auftraege
    public berechtigteGemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die Mahlzeitenverguenstigungen
    public berechtigteGemeindenMitMahlzeitenList: Array<TSGemeinde> = [];

    public tableColumns: DvSimpleTableColumnDefinition[] = [];

    public readonly updateZahlungsauftrag$ = new Subject<void>();

    public hasMahlzeitenZahlungslaeufe: boolean = false;
    private isAuszahlungAnElternActive: boolean = false;

    public principal: TSBenutzer;

    public filterGemeinde: TSGemeinde = null;
    public paginationItems: number[];
    public page: number = 0;
    public readonly PAGE_SIZE: number = 20;
    public totalResult: number = 0;
    public hasInfomaZahlung: boolean = false;
    public zahlungAnTyp: string;

    public readonly DEFAULT_SORT = {
        active: 'datumFaellig',
        direction: 'desc'
    };
    private readonly SORT_STORE_KEY = 'zahlungsauftrag-view-sort';
    private readonly FILTER_STORE_KEY = 'zahlungsauftrag-view-filter';

    public ngOnInit(): void {
        const isMahlzeitenzahlungen = EbeguUtil.isNotNullAndTrue(
            this.uiRouterGlobals.params.isMahlzeitenzahlungen
        );
        this.zahlungslaufTyp = isMahlzeitenzahlungen
            ? TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER
            : TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
        this.updateHasMahlzeitenZahlungslaeufe();
        this.updateHasAuszahlungAnElternZahlungslaeufe();
        this.applicationPropertyRS
            .isZahlungenTestMode()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((response: any) => {
                this.testMode = response;
            });
        this.applicationPropertyRS
            .getCheckboxAuszahlungInZukunft()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((response: any) => {
                this.checkboxAuszahlungInZukunft = response;
                this.auszahlungInZukunft = this.checkboxAuszahlungInZukunft;
            });
        this.setupTableColumns();
        this.authServiceRS.principal$
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: user => (this.principal = user),
                error: error => LOG.error(error)
            });
        this.translate.onDefaultLangChange
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(
                () => this.setupTableColumns(),
                (error: any) => LOG.error(error)
            );
        this.transition.onStart({exiting: 'zahlungsauftrag.view'}, () => {
            if (this.sort.active) {
                this.stateStore.store(this.SORT_STORE_KEY, this.sort);
            } else {
                this.stateStore.delete(this.SORT_STORE_KEY);
            }
            if (this.filterGemeinde) {
                this.stateStore.store(
                    this.FILTER_STORE_KEY,
                    this.filterGemeinde
                );
            } else {
                this.stateStore.delete(this.FILTER_STORE_KEY);
            }
        });
        this.initializeZahlungsauftraegeListe();
    }

    private initializeZahlungsauftraegeListe() {
        this.updateZahlungsauftrag$
            .pipe(
                switchMap(() => this.authServiceRS.principal$.pipe(take(1))),
                switchMap(principal => {
                    if (principal) {
                        return this.zahlungRS.getZahlungsauftraegeForRole$(
                            principal.getCurrentRole(),
                            this.sort,
                            this.page,
                            this.PAGE_SIZE,
                            this.filterGemeinde,
                            this.zahlungslaufTyp
                        );
                    }
                    return of(
                        new TSPaginationResultDTO<TSZahlungsauftrag>([], 0)
                    );
                }),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe({
                next: result => {
                    this.zahlungsAuftraege = result.resultList;
                    this.datasource.data = result.resultList;
                    this.updatePagination(result.totalResultSize);
                },
                error: err => LOG.error(err)
            });
    }

    public ngAfterViewInit(): void {
        this.initSort();
        this.initGemeindenListAndFilter();
        this.updateZahlungsauftrag$.next();

        timer(30000, 30000)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => {
                this.updateZahlungsauftrag$.next();
            });
    }

    private initSort(): void {
        if (this.stateStore.has(this.SORT_STORE_KEY)) {
            const stored = this.stateStore.get(this.SORT_STORE_KEY) as MatSort;
            this.sort.active = stored.active;
            this.sort.direction = stored.direction;
        } else {
            this.sort.active = this.DEFAULT_SORT.active;
            this.sort.direction = this.DEFAULT_SORT.direction as SortDirection;
        }
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag): void {
        this.$state.go('zahlung.view', {
            zahlungsauftragId: zahlungsauftrag.id,
            isMahlzeitenzahlungen:
                this.zahlungslaufTyp ===
                TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER,
            zahlungAnTyp: this.zahlungAnTyp
        });
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag): Promise<void> {
        return this.downloadZahlungsfile(
            zahlungsauftrag,
            TSGeneratedDokumentTyp.PAIN001
        );
    }

    public downloadInfoma(zahlungsauftrag: TSZahlungsauftrag): Promise<void> {
        return this.downloadZahlungsfile(
            zahlungsauftrag,
            TSGeneratedDokumentTyp.INFOMA
        );
    }

    private downloadZahlungsfile(
        zahlungsauftrag: TSZahlungsauftrag,
        typ: TSGeneratedDokumentTyp
    ) {
        const win = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS
            .getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id, typ)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownloadGeneratedFile(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
                    win
                );
            })
            .catch(error => {
                this.errorService.addMesageAsError(
                    error?.error?.translatedMessage ||
                        this.translate.instant('ERROR_UNEXPECTED')
                );
                win.close();
            }) as Promise<void>;
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS
            .getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownloadGeneratedFile(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
                    win
                );
            })
            .catch(error => {
                this.errorService.addMesageAsError(
                    error?.error?.translatedMessage ||
                        this.translate.instant('ERROR_UNEXPECTED')
                );
                win.close();
            });
    }

    public ausloesen(zahlungsauftragId: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_AUSLOESEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_AUSLOESEN_INFO')
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe({
                next: result => {
                    // User confirmed removal
                    if (!result) {
                        return;
                    }
                    this.zahlungRS
                        .zahlungsauftragAusloesen(zahlungsauftragId)
                        .subscribe({
                            next: (response: TSZahlungsauftrag) => {
                                const index = EbeguUtil.getIndexOfElementwithID(
                                    response,
                                    this.zahlungsAuftraege
                                );
                                if (index > -1) {
                                    this.zahlungsAuftraege[index] = response;
                                }
                                this.updateZahlungsauftrag$.next();
                                this.cd.markForCheck();
                            },
                            error: error =>
                                this.errorService.addMesageAsError(
                                    error?.error?.translatedMessage ||
                                        this.translate.instant(
                                            'ERROR_UNEXPECTED'
                                        )
                                )
                        });
                },
                error: error => LOG.error(error)
            });
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag): void {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(): void {
        if (!this.isEditValid()) {
            return;
        }

        this.zahlungRS
            .updateZahlungsauftrag(
                this.zahlungsauftragToEdit.beschrieb,
                this.zahlungsauftragToEdit.datumFaellig,
                this.zahlungsauftragToEdit.id
            )
            .subscribe({
                next: (response: TSZahlungsauftrag) => {
                    const index = EbeguUtil.getIndexOfElementwithID(
                        response,
                        this.zahlungsAuftraege
                    );
                    if (index > -1) {
                        this.zahlungsAuftraege[index] = response;
                    }
                    // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                    this.resetEditZahlungsauftrag();
                },
                error: error =>
                    this.errorService.addMesageAsError(
                        error?.error?.translatedMessage ||
                            this.translate.instant('ERROR_UNEXPECTED')
                    )
            });
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        return status === TSZahlungsauftragsstatus.ENTWURF;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        return this.zahlungsauftragToEdit?.id === zahlungsauftragId;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return (
                this.zahlungsauftragToEdit.beschrieb &&
                this.zahlungsauftragToEdit.beschrieb.length > 0 &&
                this.zahlungsauftragToEdit.datumFaellig !== null &&
                this.zahlungsauftragToEdit.datumFaellig !== undefined
            );
        }
        return false;
    }

    private resetEditZahlungsauftrag(): void {
        this.zahlungsauftragToEdit = null;
        this.cd.markForCheck();
    }

    public rowClass(zahlungsauftragId: string): string {
        if (this.isEditMode(zahlungsauftragId) && !this.isEditValid()) {
            return 'errorrow';
        }
        return '';
    }

    public getCalculatedStatus(zahlungsauftrag: TSZahlungsauftrag): any {
        if (
            zahlungsauftrag.status !== TSZahlungsauftragsstatus.BESTAETIGT &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            ) &&
            zahlungsauftrag.zahlungen.every(
                zahlung => zahlung.status === TSZahlungsstatus.BESTAETIGT
            )
        ) {
            return TSZahlungsstatus.BESTAETIGT;
        }
        return zahlungsauftrag.status;
    }

    private initGemeindenListAndFilter(): void {
        this.gemeindeRS
            .getGemeindenForPrincipal$()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
                next: gemeinden => {
                    this.berechtigteGemeindenList = gemeinden;
                    this.berechtigteGemeindenList.sort((a, b) =>
                        a.name.localeCompare(b.name)
                    );
                    this.toggleAuszahlungslaufTyp();
                    this.initFilterFromStore();
                    this.cd.markForCheck();
                },
                error: err => LOG.error(err)
            });
    }

    private initFilterFromStore(): void {
        if (this.stateStore.has(this.FILTER_STORE_KEY)) {
            this.filterGemeinde = this.stateStore.get(
                this.FILTER_STORE_KEY
            ) as TSGemeinde;
            this.updateZahlungsauftrag$.next();
        }
    }

    private updateHasMahlzeitenZahlungslaeufe(): void {
        this.hasMahlzeitenZahlungslaeufe = false;
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        ) {
            this.hasMahlzeitenZahlungslaeufe = false;
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS
            .getGemeindenWithMahlzeitenverguenstigungForBenutzer()
            .then(value => {
                if (value.length <= 0) {
                    return;
                }
                // Sobald mindestens eine Gemeinde in mindestens einer Gesuchsperiode die
                // Mahlzeiten aktiviert hat, wird der Toggle angezeigt
                this.hasMahlzeitenZahlungslaeufe = true;
                this.berechtigteGemeindenMitMahlzeitenList = value;
                this.cd.markForCheck();
            });
    }

    private updateHasAuszahlungAnElternZahlungslaeufe(): void {
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((response: TSPublicAppConfig) => {
                this.isAuszahlungAnElternActive = response.auszahlungAnEltern;
                this.hasInfomaZahlung = response.infomaZahlungen;
            });
    }

    public toggleAuszahlungslaufTyp(): void {
        this.filterGemeinde = null;
        this.gemeindenList =
            TSZahlungslaufTyp.GEMEINDE_INSTITUTION === this.zahlungslaufTyp ||
            !this.hasMahlzeitenZahlungslaeufe
                ? Array.from(this.berechtigteGemeindenList)
                : Array.from(this.berechtigteGemeindenMitMahlzeitenList);
        this.totalResult = 0;
        this.page = 0;
        this.updateZahlungsauftrag$.next();
        this.zahlungAnTyp = this.zahlungslaufTyp;
    }

    public getMsgKeyForToggleRight(): string {
        if (this.hasMahlzeitenZahlungslaeufe) {
            return 'GEMEINDE_MAHLZEITENVERGUENSTIGUNGEN';
        }
        return 'GEMEINDE_ANTRAGSTELLER';
    }

    public sortData($event: Sort): void {
        this.sort.active = $event.active;
        this.sort.direction = $event.direction;
        this.updateZahlungsauftrag$.next();
    }

    private updatePagination(totalResultSize: number): void {
        this.totalResult = totalResultSize;
        this.paginationItems = [];
        for (
            let i = Math.max(1, this.page - 4);
            i <=
            Math.min(
                Math.ceil(totalResultSize / this.PAGE_SIZE),
                this.page + 5
            );
            i++
        ) {
            this.paginationItems.push(i);
        }
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.paginator.pageIndex = this.page;
        this.updateZahlungsauftrag$.next();
    }

    public showAuszahlungsTypToggle(): boolean {
        // Wenn entweder Mahlzeitenzahlungslaeufe oder Auszahlungen an Eltern aktiviert sind,
        // soll der zweite Tab angezeigt werden
        return (
            !!this.hasMahlzeitenZahlungslaeufe ||
            (this.isAuszahlungAnElternActive &&
                this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getZahlungslaufElternRoles()
                ))
        );
    }

    public showInfotext(): boolean {
        return this.zahlungslaufTyp === TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
    }

    private setupTableColumns(): void {
        this.tableColumns = [
            {
                displayedName: this.translate.instant('ZAHLUNG_GENERIERT'),
                attributeName: 'datumGeneriert',
                displayFunction: (date: moment.Moment) =>
                    date.format('DD.MM.YYYY')
            },
            {
                displayedName: this.translate.instant('GEMEINDE'),
                attributeName: 'gemeinde',
                displayFunction: (gemeinde: TSGemeinde) => gemeinde.name
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_STATUS'),
                attributeName: 'status',
                displayFunction: (
                    status: TSZahlungsauftragsstatus,
                    element: TSZahlungsauftrag
                ) => this.getCalculatedStatus(element)
            }
        ];
    }

    public getColumnsAttributeName(): string[] {
        const allColumnNames = this.tableColumns?.map(
            column => column.attributeName
        );
        allColumnNames.splice(0, 0, 'datumFaellig');
        allColumnNames.splice(3, 0, 'zahlungPainExcel');
        allColumnNames.splice(4, 0, `beschrieb`);
        allColumnNames.splice(5, 0, `betragTotalAuftrag`);
        if (
            this.principal?.hasOneOfRoles(
                TSRoleUtil.getAdministratorBgGemeindeRoles()
            )
        ) {
            allColumnNames.splice(3, 0, `zahlungPain`);
            allColumnNames.push('editSave');
            allColumnNames.push('ausloesen');

            if (
                this.hasInfomaZahlung &&
                this.gemeindenList.some(
                    gemeinde => gemeinde.infomaZahlungen === true
                )
            ) {
                allColumnNames.splice(3, 0, `zahlungInfoma`);
            }
        }
        return allColumnNames;
    }

    public getDisplayValue(element: any, column: any): string {
        if (column.displayFunction !== undefined) {
            return column.displayFunction(
                element[column.attributeName],
                element
            );
        }
        return element[column.attributeName];
    }

    public showForm(): boolean {
        return this.principal?.hasOneOfRoles(
            TSRoleUtil.getAdministratorBgGemeindeRoles()
        );
    }

    public showGemeindeFilter(): boolean {
        return this.gemeindenList.length > 1;
    }

    protected readonly TSZahlungslaufTyp = TSZahlungslaufTyp;
}
