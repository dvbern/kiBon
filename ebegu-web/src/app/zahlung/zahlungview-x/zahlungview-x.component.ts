import {CurrencyPipe} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild
} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '@kibon/shared/model/enums';
import {TSZahlungsstatus, TSZahlung} from '@kibon/zahlung/model/entity';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {StateStoreService} from '../../shared/services/state-store.service';
import {ZahlungUtilZahlungService} from '@kibon/zahlung/util/zahlung-service';

const LOG = LogFactory.createLog('ZahlungviewXComponent');

@Component({
    selector: 'dv-zahlungview-x',
    templateUrl: './zahlungview-x.component.html',
    styleUrls: ['./zahlungview-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ZahlungviewXComponent implements OnInit, AfterViewInit {
    @ViewChild(MatSort) public sort: MatSort;

    private zahlungen: TSZahlung[] = [];
    private isMahlzeitenzahlungen: boolean = false;
    public datasource: MatTableDataSource<TSZahlung> =
        new MatTableDataSource<TSZahlung>([]);
    public zahlungAnTyp: string;

    public itemsByPage: number = 20;
    public tableColumns: any[];
    private readonly SORT_STORE_KEY = 'zahlungview-x-sort';
    private principal: TSBenutzer;

    public constructor(
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly reportRS: ReportRS,
        private readonly zahlungRS: ZahlungUtilZahlungService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly routerGlobals: UIRouterGlobals,
        private readonly translate: TranslateService,
        private readonly currency: CurrencyPipe,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly transition: TransitionService,
        private readonly stateStore: StateStoreService
    ) {}

    public ngOnInit(): void {
        if (this.routerGlobals.params.isMahlzeitenzahlungen) {
            this.isMahlzeitenzahlungen = true;
        }

        if (this.routerGlobals.params.zahlungAnTyp) {
            this.zahlungAnTyp = this.routerGlobals.params.zahlungAnTyp;
        }
        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        this.principal = principal;
                        const zahlungsauftragId =
                            this.routerGlobals.params.zahlungsauftragId;
                        if (this.routerGlobals.params.zahlungsauftragId) {
                            return this.zahlungRS.getZahlungsauftragForRole$(
                                principal.getCurrentRole(),
                                zahlungsauftragId
                            );
                        }
                    }

                    return of(null);
                }),
                map(zahlungsauftrag =>
                    zahlungsauftrag ? zahlungsauftrag.zahlungen : []
                )
            )
            .subscribe({
                next: zahlungen => {
                    this.zahlungen = zahlungen;
                    this.datasource.data = zahlungen;
                    this.datasource.sort = this.sort;
                    this.cd.markForCheck();
                },
                error: err => LOG.error(err)
            });
        this.setupTableColumns();

        this.transition.onStart({exiting: 'zahlung.view'}, () => {
            if (this.sort.active) {
                this.stateStore.store(this.SORT_STORE_KEY, this.sort);
            } else {
                this.stateStore.delete(this.SORT_STORE_KEY);
            }
        });
    }

    public ngAfterViewInit(): void {
        if (this.stateStore.has(this.SORT_STORE_KEY)) {
            const stored = this.stateStore.get(this.SORT_STORE_KEY) as MatSort;
            this.sort.active = stored.active;
            this.sort.direction = stored.direction;
        }
    }

    public gotToUebersicht(): void {
        this.$state.go('zahlungsauftrag.view', {
            isMahlzeitenzahlungen: this.isMahlzeitenzahlungen
        });
    }

    public downloadDetails(zahlung: TSZahlung): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS
            .getZahlungReportExcel(zahlung.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownloadGeneratedPDF(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    false,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public bestaetigen(zahlung: TSZahlung): void {
        this.zahlungRS.zahlungBestaetigen(zahlung.id).subscribe({
            next: (response: TSZahlung) => {
                const index = EbeguUtil.getIndexOfElementwithID(
                    response,
                    this.zahlungen
                );
                if (index < 0) {
                    return;
                }
                this.zahlungen[index] = response;
                this.datasource.data = this.zahlungen;
                this.cd.markForCheck();
            },
            error: error =>
                this.errorService.addMesageAsError(
                    error?.translatedMessage ||
                        this.translate.instant('ERROR_UNEXPECTED')
                )
        });
    }

    public canBestaetigen(zahlungsstatus: TSZahlungsstatus): boolean {
        return (
            zahlungsstatus === TSZahlungsstatus.AUSGELOEST &&
            this.principal.hasOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionRoles()
            ) &&
            !this.isMahlzeitenzahlungen
        );
    }

    private setupTableColumns(): void {
        this.tableColumns = [
            {
                displayedName: this.zahlungAnTyp.includes('INSTITUTION')
                    ? this.translate.instant('ZAHLUNG_INSTITUTION')
                    : this.translate.instant('ZAHLUNG_ELTERNTEIL'),
                attributeName: 'empfaengerName'
            },
            {
                displayedName: this.translate.instant(
                    'ZAHLUNG_BETREUUNGSANGEBOTTYP'
                ),
                attributeName: 'betreuungsangebotTyp',
                displayFunction: (angebotTyp: TSBetreuungsangebotTyp) =>
                    this.translate.instant(angebotTyp)
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_TOTAL'),
                attributeName: 'betragTotalZahlung',
                displayFunction: (betrag: number) =>
                    this.currency.transform(betrag, '', '')
            }
        ];
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

    public getColumnsAttributeName(): string[] {
        const mapped = this.tableColumns.map(column => column.attributeName);
        mapped.splice(1, 0, 'zahlungPainExcel');
        mapped.push('status');
        return mapped;
    }
}
