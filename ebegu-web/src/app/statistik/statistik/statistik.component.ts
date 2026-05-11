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
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject,
    OnDestroy,
    OnInit
} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatTableDataSource} from '@angular/material/table';
import {MomentUtil} from '@utils/moment';
import {Observable} from 'rxjs';
import {CONSTANTS} from '@models/constants';
import {LogFactory} from '@utils/log';
import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {map, startWith} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {InstitutionNameStammdatenIdDto} from '../../../models/dto/InstitutionNameStammdatenIdDto.interface';
import {TSGemeinde} from '../../../models/entity/TSGemeinde';
import {TSInstitutionStammdaten} from '../../../models/entity/TSInstitutionStammdaten';
import {TSDemoFeature} from '../../../models/enums/TSDemoFeature';
import {TSRole} from '../../../models/enums/TSRole';
import {TSGesuchsperiode} from '../../../models/entity/TSGesuchsperiode';
import {TSInstitution} from '../../../models/entity/TSInstitution';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSStatistikParameterType} from '../../../models/enums/TSStatistikParameterType';
import {TSStatistikParameter} from '../../../models/TSStatistikParameter';
import {TSWorkJob} from '../../../models/TSWorkJob';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '@app/shared/component/remove-dialog';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BatchJobRS} from '../../core/service/batchRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../core/service/reportAsyncRS.rest';
import {LastenausgleichRS} from '../../lastenausgleich/services/lastenausgleichRS.rest';

const LOG = LogFactory.createLog('StatistikComponent');

@Component({
    selector: 'dv-statistik',
    templateUrl: './statistik.component.html',
    styleUrls: ['./statistik.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class StatistikComponent implements OnInit, OnDestroy {
    private readonly gesuchsperiodeRS = inject(GesuchsperiodeRS);
    private readonly institutionStammdatenRS = inject(InstitutionStammdatenRS);
    private readonly institutionRS = inject(InstitutionRS);
    private readonly reportAsyncRS = inject(ReportAsyncRS);
    private readonly downloadRS = inject(DownloadRS);
    private readonly batchJobRS = inject(BatchJobRS);
    private readonly errorService = inject(ErrorService);
    private readonly translate = inject(TranslateService);
    private readonly dialog = inject(MatDialog);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly gemeindeRS = inject(GemeindeRS);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly lastenausgleichRS = inject(LastenausgleichRS);
    readonly applicationPropertyRS = inject(ApplicationPropertyRsService);

    public readonly TSStatistikParameterType = TSStatistikParameterType;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;
    public readonly demoFeature = TSDemoFeature.ZAHLUNGEN_STATISTIK;

    private polling: NodeJS.Timeout;
    public statistikParameter: TSStatistikParameter;
    public gesuchsperioden: Array<TSGesuchsperiode>;
    private readonly DATE_PARAM_FORMAT: string = 'YYYY-MM-DD';
    private readonly TS_ANMELDUNGEN_STATISTIK_MAX_AMOUNT = 50;
    // Statistiken sind nur moeglich ab Beginn der fruehesten Periode bis Ende der letzten Periode
    public maxDate: moment.Moment;
    public minDate: moment.Moment;
    public userjobs: MatTableDataSource<TSWorkJob>;
    public columndefs: string[] = [
        'typ',
        'erstellt',
        'gestartet',
        'beendet',
        'status',
        'icon'
    ];
    public allJobs: Array<TSWorkJob>;
    public years: number[];
    public tagesschulenStammdatenFilterList: InstitutionNameStammdatenIdDto[] =
        [];
    public bgInstitutionen: TSInstitution[];
    public hasBgInstitutionen$: Observable<boolean>;
    public gemeinden: TSGemeinde[];
    public gemeindenMahlzeitenverguenstigungen: TSGemeinde[];
    public flagShowErrorNoGesuchSelected: boolean = false;
    public showKantonStatistik: boolean = false;
    public ferienbetreuungActive: boolean = false;
    public lastenausgleichActive: boolean = false;
    public lastenausgleichTagesschulenActive: boolean = false;
    public tagesschulenActive = false;
    public lastenausgleichYears: number[] = [];

    private static sortByName<T extends {name: string}>(items: T[]): T[] {
        return items.sort((a, b) => a.name.localeCompare(b.name));
    }

    private static handleError(err: Error): void {
        LOG.error(err);
    }

    public ngOnInit(): void {
        this.statistikParameter = new TSStatistikParameter();
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: any) => {
            this.gesuchsperioden = response;
            if (this.gesuchsperioden.length > 0) {
                this.maxDate = this.gesuchsperioden[0].gueltigkeit.gueltigBis;
                this.minDate = MomentUtil.localDateToMoment('2017-01-01');
            }
            this.calculateYears();
            this.cd.markForCheck();
        });

        this.institutionStammdatenRS
            .getTagesschulenFilterListForCurrentBenutzer()
            .then(institutionNameStammdatenIdList => {
                this.tagesschulenStammdatenFilterList =
                    institutionNameStammdatenIdList.data;
                this.tagesschulenStammdatenFilterList =
                    StatistikComponent.sortByName(
                        this.tagesschulenStammdatenFilterList
                    );
                this.cd.markForCheck();
            });

        this.gemeindeRS.getGemeindenForPrincipal$().subscribe(gemeinden => {
            this.gemeinden = gemeinden;
            this.cd.markForCheck();
        });

        if (this.showLastenausgleichBGStatistikAllowedForRole()) {
            this.lastenausgleichRS.getAllLastenausgleiche().subscribe(
                lastenausgleiche => {
                    this.lastenausgleichYears = lastenausgleiche
                        .map(l => l.jahr)
                        .filter(
                            y =>
                                y >=
                                CONSTANTS.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT
                        )
                        .sort((a, b) => a - b);
                    this.cd.markForCheck();
                },
                err => {
                    LOG.error(err);
                }
            );
        }

        this.updateShowMahlzeitenStatistik();
        this.refreshUserJobs();
        this.initBatchJobPolling();

        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(res => {
                this.ferienbetreuungActive = res.ferienbetreuungAktiv;
                this.lastenausgleichActive = res.lastenausgleichAktiv;
                this.lastenausgleichTagesschulenActive =
                    res.lastenausgleichTagesschulenAktiv;
                this.updateShowKantonStatistik();
                this.tagesschulenActive = res.angebotTSActivated;
            });

        // observable is prefered here over signal
        // for auto unsubscribe + async pipe in template call
        this.hasBgInstitutionen$ = this.getInstitutionen().pipe(
            map(institutionen => {
                this.bgInstitutionen =
                    StatistikComponent.sortByName(institutionen);
                return this.bgInstitutionen.length > 0;
            }),
            startWith(false) // renders immediately as false to show in template
        );
    }

    public ngOnDestroy(): void {
        if (this.polling) {
            clearInterval(this.polling);
            LOG.debug('canceled job polling');
        }
    }

    private getInstitutionen(): Observable<TSInstitution[]> {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            return this.institutionRS.getAllBgInstitutionenEditableForCurrentBenutzer();
        }
        return this.institutionRS.getAllBgInstitutionenReadableForCurrentBenutzer();
    }

    private initBatchJobPolling(): void {
        // check all 8 seconds for the state
        const delay = 12000;
        this.polling = setInterval(() => this.refreshUserJobs(), delay);
    }

    private refreshUserJobs(): void {
        this.batchJobRS
            .getBatchJobsOfUser()
            .subscribe((response: TSWorkJob[]) => {
                this.userjobs = new MatTableDataSource(response);
                this.cd.markForCheck();
            }, StatistikComponent.handleError);
    }

    public generateStatistik(form: NgForm, type?: string): void {
        if (!form.valid) {
            return;
        }
        const stichtag = this.statistikParameter.stichtag
            ? this.statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT)
            : undefined;
        switch (type) {
            case TSStatistikParameterType.GESUCH_STICHTAG:
                this.reportAsyncRS
                    .getGesuchStichtagReportExcel(
                        stichtag,
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.GESUCH_ZEITRAUM:
                this.reportAsyncRS
                    .getGesuchZeitraumReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchZeitraumDatumTyp,
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.KINDER:
                this.reportAsyncRS
                    .getKinderReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.GESUCHSTELLER:
                this.reportAsyncRS
                    .getGesuchstellerReportExcel(stichtag)
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.KANTON:
                this.reportAsyncRS
                    .getKantonReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.kantonSelbstbehalt
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.MITARBEITERINNEN:
                this.reportAsyncRS
                    .getMitarbeiterinnenReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        )
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.BENUTZER:
                this.reportAsyncRS
                    .getBenutzerReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.GESUCHSTELLER_KINDER_BETREUUNG:
                this.reportAsyncRS
                    .getGesuchstellerKinderBetreuungReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.gesuchsperiode
                            ? this.statistikParameter.gesuchsperiode
                            : null
                    )
                    .subscribe(
                        (res: {workjobId: string}) => {
                            this.informReportGenerationStarted(res);
                        },
                        () => {
                            LOG.error(
                                'An error occurred downloading the document, closing download window.'
                            );
                        }
                    );
                return;
            case TSStatistikParameterType.ZAHLUNGEN_PERIODE:
                if (this.statistikParameter.gesuchsperiode) {
                    this.reportAsyncRS
                        .getZahlungPeriodeReportExcel(
                            this.statistikParameter.gesuchsperiode
                        )
                        .subscribe((res: {workjobId: string}) => {
                            this.informReportGenerationStarted(res);
                            const startmsg =
                                this.translate.instant('STARTED_GENERATION');
                            this.errorService.addMesageAsInfo(startmsg);
                        }, StatistikComponent.handleError);
                } else {
                    LOG.warn('gesuchsperiode muss gewählt sein');
                }
                return;
            case TSStatistikParameterType.MASSENVERSAND:
                if (!this.isMassenversandValid()) {
                    return;
                }
                if (this.statistikParameter.text) {
                    this.openRemoveDialog$().subscribe(
                        () => {
                            this.createMassenversand();
                        },
                        err => LOG.error(err)
                    );
                } else {
                    this.createMassenversand();
                }
                return;
            case TSStatistikParameterType.INSTITUTIONEN:
                this.reportAsyncRS
                    .getInstitutionenReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.VERRECHNUNG_KIBON:
                this.reportAsyncRS
                    .getVerrechnungKibonReportExcel(
                        this.statistikParameter.doSave,
                        this.statistikParameter.betragProKind
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.TAGESSCHULE_ANMELDUNGEN:
                this.reportAsyncRS
                    .getTagesschuleAnmeldungenReportExcel(
                        this.statistikParameter.selectedTagesschulen
                            .map(
                                institutionNameStammdatenID =>
                                    institutionNameStammdatenID.stammdatenId
                            )
                            .join(','),
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.TAGESSCHULE_RECHNUNGSSTELLUNG:
                this.reportAsyncRS
                    .getTagesschuleRechnungsstellungReportExcel(
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                break;
            case TSStatistikParameterType.MAHLZEITENVERGUENSTIGUNG:
                this.reportAsyncRS
                    .getMahlzeitenverguenstigungReportExcel(
                        this.statistikParameter.von.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter
                            .gemeindeMahlzeitenverguenstigungen
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.GEMEINDEN:
                this.reportAsyncRS
                    .getGemeindenReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.FERIENBETREUUNG:
                this.reportAsyncRS
                    .getFerienbetreuungReportExcel()
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.LASTENAUSGLEICH_TAGESSCHULEN:
                this.reportAsyncRS
                    .getLastenausgleichTagesschulenReportExcel(
                        this.statistikParameter.gesuchsperiode
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.LASTENAUSGLEICH_BG:
                this.reportAsyncRS
                    .getLastenausgleichBGReportExcel(
                        this.statistikParameter.gemeinde,
                        this.statistikParameter.jahr,
                        this.statistikParameter.von?.format(
                            this.DATE_PARAM_FORMAT
                        ),
                        this.statistikParameter.bis?.format(
                            this.DATE_PARAM_FORMAT
                        )
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            case TSStatistikParameterType.ZAHLUNGEN:
                // falls der eingeloggte benutzer eine institution ist, wird das Dropdown mit den Institutinen
                // nicht gezeigt. Wir setzen die BG Institution, weil es in diesem Fall immer nur eine in der Liste
                // hat
                if (
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getInstitutionOnlyRoles()
                    )
                ) {
                    this.statistikParameter.institution =
                        this.bgInstitutionen[0];
                }
                this.reportAsyncRS
                    .getZahlungenReportExcel(
                        EbeguUtil.isNullOrUndefined(this.statistikParameter.von)
                            ? ''
                            : this.statistikParameter.von.format(
                                  this.DATE_PARAM_FORMAT
                              ),
                        EbeguUtil.isNullOrUndefined(this.statistikParameter.bis)
                            ? ''
                            : this.statistikParameter.bis.format(
                                  this.DATE_PARAM_FORMAT
                              ),
                        this.statistikParameter.gesuchsperiode,
                        this.statistikParameter.gemeinde,
                        this.statistikParameter.institution
                    )
                    .subscribe((res: {workjobId: string}) => {
                        this.informReportGenerationStarted(res);
                    }, StatistikComponent.handleError);
                return;
            default:
                throw new Error(`unknown TSStatistikParameterType: ${type}`);
        }
    }

    private openRemoveDialog$(): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant(
                'MASSENVERSAND_ERSTELLEN_CONFIRM_TITLE'
            ),
            text: this.translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_INFO')
        };
        return this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed();
    }

    private createMassenversand(): void {
        LOG.info('Erstelle Massenversand');
        this.reportAsyncRS
            .getMassenversandReportExcel(
                this.statistikParameter.von
                    ? this.statistikParameter.von.format(this.DATE_PARAM_FORMAT)
                    : null,
                this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                this.statistikParameter.gesuchsperiode,
                this.statistikParameter.bgGesuche,
                this.statistikParameter.mischGesuche,
                this.statistikParameter.tsGesuche,
                this.statistikParameter.ohneFolgegesuche,
                this.statistikParameter.text
            )
            .subscribe(
                (res: {workjobId: string}) => {
                    this.informReportGenerationStarted(res);
                },
                () => {
                    LOG.error(
                        'An error occurred downloading the document, closing download window.'
                    );
                }
            );
    }

    private informReportGenerationStarted(res: {workjobId: string}): void {
        LOG.debug(`executionID: ${res.workjobId}`);
        const startmsg = this.translate.instant('STARTED_GENERATION');
        this.errorService.addMesageAsInfo(startmsg);
        this.refreshUserJobs();
    }

    public downloadStatistik(row: TSWorkJob): void {
        if (EbeguUtil.isNullOrUndefined(row)) {
            return;
        }

        if (
            EbeguUtil.isNullOrUndefined(row.batchJobStatus) ||
            row.batchJobStatus !== 'FINISHED'
        ) {
            LOG.info('batch-job is not yet finnished');
            return;
        }

        const win = this.downloadRS.prepareDownloadWindow();
        LOG.debug(`accessToken: ${row.resultData}`);
        this.downloadRS.startDownloadGeneratedFile(
            row.resultData,
            'report.xlsx',
            true,
            win
        );
    }

    /**
     * helper methode die es dem Admin erlaubt alle jobs zu sehen
     */
    public showAllJobs(): void {
        this.batchJobRS.getAllJobs().subscribe((result: TSWorkJob[]) => {
            this.allJobs = result;
            this.cd.markForCheck();
        }, StatistikComponent.handleError);
    }

    /**
     * Takes all years of all Gesuchsperioden and saves them as a string into an array
     */
    private calculateYears(): void {
        this.years = [];
        this.gesuchsperioden.forEach(periode => {
            if (this.years.indexOf(periode.getBasisJahrPlus1()) < 0) {
                this.years.push(periode.getBasisJahrPlus1());
            }
            if (this.years.indexOf(periode.getBasisJahrPlus2()) < 0) {
                this.years.push(periode.getBasisJahrPlus2());
            }
        });

        this.years.sort();
    }

    /**
     * this function is called from the html template and it selects the
     * gesuchsperiode for statistik. since KIBONBE-191 there is a
     * new multi-select for institutionen, so we can safely use the list of
     * institutionen [0] to specify the gesuchsperiode
     * @param stammdaten
     */
    public getGesuchsperiodenForTagesschule(
        stammdaten: TSInstitutionStammdaten
    ): TSGesuchsperiode[] {
        return stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
            .map(d => d.gesuchsperiode)
            .sort((a, b) =>
                b.gesuchsperiodeString.localeCompare(a.gesuchsperiodeString)
            );
    }

    public showMahlzeitenverguenstigungStatistik(): boolean {
        return (
            this.gemeindenMahlzeitenverguenstigungen &&
            this.gemeindenMahlzeitenverguenstigungen.length > 0 &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SACHBEARBEITER_BG,
                TSRole.ADMIN_BG,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.SUPER_ADMIN
            ])
        );
    }

    private updateShowMahlzeitenStatistik(): void {
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        ) {
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS
            .getGemeindenWithMahlzeitenverguenstigungForBenutzer()
            .then(value => {
                // falls es nur eine Gemeinde gibt, wird dropdown nicht angezeigt
                if (value.length === 1) {
                    this.statistikParameter.gemeindeMahlzeitenverguenstigungen =
                        value[0];
                }
                this.gemeindenMahlzeitenverguenstigungen = value;
                this.cd.markForCheck();
            });
    }

    private isMassenversandValid(): boolean {
        // simulate a click in the checkboxes of Verantwortlichkeit
        this.gesuchTypeClicked();
        return !this.flagShowErrorNoGesuchSelected;
    }

    public gesuchTypeClicked(): void {
        this.flagShowErrorNoGesuchSelected =
            !this.statistikParameter.bgGesuche &&
            !this.statistikParameter.mischGesuche &&
            !this.statistikParameter.tsGesuche;
    }

    public updateShowKantonStatistik(): void {
        this.showKantonStatistik = false;
        if (
            this.authServiceRS.isOneOfRoles([
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS
            ])
        ) {
            return;
        }
        if (!this.lastenausgleichActive) {
            return;
        }

        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.showKantonStatistik = true;
            return;
        }
        this.institutionStammdatenRS
            .getBetreuungsangeboteForInstitutionenOfCurrentBenutzer()
            .then(response => {
                response.forEach(angebottyp => {
                    if (angebottyp !== TSBetreuungsangebotTyp.TAGESSCHULE) {
                        this.showKantonStatistik = true;
                    }
                });
                this.cd.markForCheck();
            });
    }

    public showGesucheNachStichtag(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showAllJobsVisible(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public showGesucheNachZeitraum(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showZahlungenNachPeriode(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showKinderStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.SACHBEARBEITER_INSTITUTION,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT
        ]);
    }

    public showGesuchstellerStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showMitarbeiterStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showBenutzerStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_TRAEGERSCHAFT,
            TSRole.ADMIN_INSTITUTION,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT
        ]);
    }

    public showGesuchstellerKinderBetreuungStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_BG,
            TSRole.SUPER_ADMIN,
            TSRole.REVISOR,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE
        ]);
    }

    public showStatistikMassenversand(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_BG,
            TSRole.ADMIN_TS,
            TSRole.ADMIN_GEMEINDE
        ]);
    }

    public showInstitutionenStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles([
            TSRole.SUPER_ADMIN,
            TSRole.ADMIN_MANDANT,
            TSRole.SACHBEARBEITER_MANDANT,
            TSRole.ADMIN_GEMEINDE,
            TSRole.SACHBEARBEITER_GEMEINDE,
            TSRole.ADMIN_BG,
            TSRole.SACHBEARBEITER_BG,
            TSRole.ADMIN_TS,
            TSRole.SACHBEARBEITER_TS
        ]);
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public showTagesschuleAnmeldungenStatistik(): boolean {
        return (
            this.tagesschulenStammdatenFilterList?.length &&
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS,
                TSRole.ADMIN_INSTITUTION,
                TSRole.SACHBEARBEITER_INSTITUTION,
                TSRole.ADMIN_TRAEGERSCHAFT,
                TSRole.SACHBEARBEITER_TRAEGERSCHAFT
            ]) &&
            this.tagesschulenActive
        );
    }

    public showRechnungsstellungStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles([
                TSRole.SUPER_ADMIN,
                TSRole.ADMIN_MANDANT,
                TSRole.SACHBEARBEITER_MANDANT,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SACHBEARBEITER_GEMEINDE,
                TSRole.ADMIN_TS,
                TSRole.SACHBEARBEITER_TS
            ]) && this.tagesschulenActive
        );
    }

    public showMandantStatistik(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showFerienbetreuungStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) &&
            this.ferienbetreuungActive
        );
    }

    public showLastenausgleichTagesschulenStatistik(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles()) &&
            this.lastenausgleichTagesschulenActive
        );
    }

    public showLastenausgleichBGStatistikAllowedForRole() {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeOrBGRoles().concat(
                TSRoleUtil.getMandantRoles()
            )
        );
    }

    public showZahlungenStatistikAllowedForRoles() {
        // die Statistik wird nur gezeigt, falls der User für mindestens eine BG Institution berechtigt ist.
        // ansonsten handelt es sich allenfalls um einen TS Institution User
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeOrBGRoles()
                .concat(TSRoleUtil.getMandantRoles())
                .concat(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())
        );
    }

    public gemeindenVisibleZahlungenStatistik(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getInstitutionOnlyRoles()
        );
    }

    public institutionenVisibleZahlungenStatistik(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getInstitutionOnlyRoles()
        );
    }

    public requiredIfAlleGemeinden(): boolean {
        return (
            EbeguUtil.isNullOrUndefined(this.statistikParameter.gemeinde) ||
            this.statistikParameter.gemeinde === ('ALLE' as any)
        );
    }

    public requiredIfAlleInstitutionen(): boolean {
        return (
            EbeguUtil.isNullOrUndefined(this.statistikParameter.institution) ||
            this.statistikParameter.institution === ('ALLE' as any)
        );
    }

    public getActiveGesuchsperioden(): TSGesuchsperiode[] {
        return this.gesuchsperioden?.filter(gp => gp.isAktiv());
    }

    public unselectAllTagesschulen() {
        this.statistikParameter.selectedTagesschulen = [];
    }

    /**
     * toggle dis- and enabled state for the "generieren" button
     */
    public disableTagesschuleAnmeldungenStatistikGenerierenButton(): boolean {
        return (
            !this.isTagesschulenSelectionInRange() ||
            EbeguUtil.isEmptyStringNullOrUndefined(
                this.statistikParameter.gesuchsperiode
            )
        );
    }

    /**
     * this method checks if the selected amount of tagesschulen institutionen
     * does not surpass 50 for excel statistik memory limitations
     * @private
     */
    private isTagesschulenSelectionInRange() {
        const count = this.statistikParameter.selectedTagesschulen.length;
        return count > 0 && count <= this.TS_ANMELDUNGEN_STATISTIK_MAX_AMOUNT;
    }

    /**
     * checks everytime the selection of tagesschulen changes in dropdown
     * of "Tagesschulen Anmeldungen" if maxLimit has been reached and sets
     * an error accordingly or removes the error.
     */
    public onTagesschulenSelectChange(model: NgModel) {
        const selected =
            this.statistikParameter.selectedTagesschulen?.length ?? 0;

        if (selected > this.TS_ANMELDUNGEN_STATISTIK_MAX_AMOUNT) {
            model.control.setErrors({maxLimit: true});
        } else {
            // Remove error if user goes back under limit
            if (model.control.hasError('maxLimit')) {
                model.control.updateValueAndValidity({
                    onlySelf: true,
                    emitEvent: false
                });
                model.control.setErrors(null);
            }
        }
    }

    protected readonly ApplicationPropertyRsService =
        ApplicationPropertyRsService;

    public bgZahlungenStatistikGemeindeAndInstitutionAlleSelected(): any {
        return (
            (EbeguUtil.isNullOrUndefined(this.statistikParameter.gemeinde) ||
                this.statistikParameter.gemeinde === ('ALLE' as any)) &&
            (EbeguUtil.isNullOrUndefined(this.statistikParameter.institution) ||
                this.statistikParameter.institution === ('ALLE' as any))
        );
    }
}
