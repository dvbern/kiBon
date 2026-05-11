import {CurrencyPipe} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild,
    inject
} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSZahlungsstatus, TSZahlung} from '@models/zahlung';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '@utils/log';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {StateStoreService} from '../../shared/services/state-store.service';
import {ZahlungService} from '@app/zahlung/service';

const LOG = LogFactory.createLog('ZahlungviewXComponent');

@Component({
    selector: 'dv-zahlungview-x',
    templateUrl: './zahlungview-x.component.html',
    styleUrls: ['./zahlungview-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ZahlungviewXComponent implements OnInit, AfterViewInit {
    private readonly $state = inject(StateService);
    private readonly downloadRS = inject(DownloadRS);
    private readonly reportRS = inject(ReportRS);
    private readonly zahlungRS = inject(ZahlungService);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly routerGlobals = inject(UIRouterGlobals);
    private readonly translate = inject(TranslateService);
    private readonly currency = inject(CurrencyPipe);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly errorService = inject(ErrorService);
    private readonly transition = inject(TransitionService);
    private readonly stateStore = inject(StateStoreService);

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
                this.downloadRS.startDownloadGeneratedFile(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
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
