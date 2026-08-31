import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject,
    OnInit,
    ViewChild
} from '@angular/core';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {
    TranslateDirective,
    TranslatePipe,
    TranslateService
} from '@ngx-translate/core';
import {AnchorUISref, StateService, UISref} from '@uirouter/angular';
import {LogFactory} from '@utils/log';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {InstitutionRS} from '../../../../core/service/institutionRS.rest';
import {GemeindeRS} from '../../../../../gesuch/service/gemeindeRS.rest';
import {GesuchsperiodeRS} from '../../../../core/service/gesuchsperiodeRS.rest';
import {ApplicationPropertyRsService} from '../../../../../utils/application-property-rs';
import {TSGemeinde} from '../../../../../models/entity/TSGemeinde';
import {TSInstitution} from '../../../../../models/entity/TSInstitution';
import {TSBetreuungsangebotTyp} from '../../../../../models/enums/TSBetreuungsangebotTyp';
import {getTSBetreuungsangebotTypValuesForMandant} from '../../../../../utils/betreuungsangebot-typ/betreuungsangebot-typ';
import {SharedModule} from '../../../../shared/shared.module';
import {DVPendenzenBetreuungenAntragList} from '../../../../../hybrid/gesuch/betreuung/pendenz/pendenzen-betreuungen-antrag-list.interface';
import {DVPendenzenBetreuungenFilter} from '../../../../../hybrid/gesuch/betreuung/pendenz/pendenzen-betreuungen-filter.interface';
import {PendenzBetreuungenService} from '../../../../../hybrid/gesuch/betreuung/pendenz/pendenzenBetreuungen.service';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {CommonModule} from '@angular/common';

const LOG = LogFactory.createLog('PendenzenBetreuungenListView');

@Component({
    selector: 'dv-pendenzen-betreuungen-list-view',
    imports: [
        AnchorUISref,
        SharedModule,
        TranslateDirective,
        TranslatePipe,
        UISref,
        MatTableModule,
        MatSortModule,
        MatInputModule,
        MatSelectModule,
        CommonModule
    ],
    templateUrl: './pendenzen-betreuungen-list-view.component.html',
    styleUrl: './pendenzen-betreuungen-list-view.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PendenzenBetreuungenListView implements OnInit {
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly institutionRS = inject(InstitutionRS);
    private readonly gemeindeRS = inject(GemeindeRS);
    private readonly gesuchsperiodeRS = inject(GesuchsperiodeRS);
    private readonly applicationPropertyRS = inject(
        ApplicationPropertyRsService
    );
    private readonly pendenzBetreuungenService = inject(
        PendenzBetreuungenService
    );
    private readonly translateService = inject(TranslateService);
    private readonly stateService = inject(StateService);
    private readonly ebeguUtil = inject(EbeguUtil);
    private readonly cd = inject(ChangeDetectorRef);

    @ViewChild(MatSort) public sort!: MatSort;

    public hasInstitutionenInStatusAngemeldet: boolean | undefined;
    public datasource =
        new MatTableDataSource<DVPendenzenBetreuungenAntragList>([]);
    public displayedColumns: string[] = [
        'betreuungsNummer',
        'gemeinde',
        'name',
        'vorname',
        'geburtsdatum',
        'antragTyp',
        'periodenString',
        'eingangsdatum',
        'angebote',
        'institutionen'
    ];
    public filterColumns: string[] = [
        'betreuungsNummer-filter',
        'gemeinde-filter',
        'name-filter',
        'vorname-filter',
        'geburtsdatum-filter',
        'antragTyp-filter',
        'periodenString-filter',
        'eingangsdatum-filter',
        'angebote-filter',
        'institutionen-filter'
    ];
    public filterPredicate: DVPendenzenBetreuungenFilter = {};
    public totalItems = 0;

    public gemeindenList: TSGemeinde[] = [];
    public gesuchsperiodenList: string[] = [];
    public angeboteList: TSBetreuungsangebotTyp[] = [];
    public institutionenList: TSInstitution[] = [];

    public ngOnInit(): void {
        this.initHasInstitutionenInStatusAngemeldet();
        this.initTable();
        this.initFilterOptions();
        this.loadData();
    }

    private initFilterOptions(): void {
        this.gemeindeRS.getGemeindenForPrincipal$().subscribe({
            next: gemeinden => {
                this.gemeindenList = gemeinden.sort((a, b) =>
                    a.name.localeCompare(b.name)
                );
                this.cd.markForCheck();
            },
            error: err => LOG.error(err)
        });

        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            this.gesuchsperiodenList = response.map(
                p => p.gesuchsperiodeString
            );
            this.cd.markForCheck();
        });

        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(res => {
                this.angeboteList = getTSBetreuungsangebotTypValuesForMandant(
                    res.angebotTSActivated,
                    res.angebotMittagstischActivated
                );
                this.cd.markForCheck();
            });

        this.institutionRS
            .getInstitutionenReadableForCurrentBenutzer()
            .subscribe({
                next: response => {
                    this.institutionenList = response.sort((a, b) =>
                        a.name.localeCompare(b.name)
                    );
                    this.cd.markForCheck();
                },
                error: error => LOG.error(error)
            });
    }

    private initTable(): void {
        this.datasource.filterPredicate = (
            data: DVPendenzenBetreuungenAntragList,
            filter: string
        ) => {
            const searchFilter = JSON.parse(
                filter
            ) as DVPendenzenBetreuungenFilter;

            const betreuungsNummerMatch =
                !searchFilter.betreuungsNummer ||
                data.betreuungsNummer
                    ?.toLowerCase()
                    .includes(searchFilter.betreuungsNummer.toLowerCase());
            const gemeindeMatch =
                !searchFilter.gemeinde ||
                data.gemeinde
                    ?.toLowerCase()
                    .includes(searchFilter.gemeinde.toLowerCase());
            const nameMatch =
                !searchFilter.name ||
                data.name
                    ?.toLowerCase()
                    .includes(searchFilter.name.toLowerCase());
            const vornameMatch =
                !searchFilter.vorname ||
                data.vorname
                    ?.toLowerCase()
                    .includes(searchFilter.vorname.toLowerCase());
            const periodenStringMatch =
                !searchFilter.periodenString ||
                data.periodenString === searchFilter.periodenString;
            const geburtsdatumMatch =
                !searchFilter.geburtsdatum ||
                (data.geburtsdatum &&
                    this.formatDate(data.geburtsdatum!)
                        .toLowerCase()
                        .includes(searchFilter.geburtsdatum.toLowerCase()));
            const antragTypMatch =
                !searchFilter.antragTyp ||
                this.getAntragTypBezeichnung(data)
                    .toLowerCase()
                    .includes(searchFilter.antragTyp.toLowerCase());
            const eingangsdatumMatch =
                !searchFilter.eingangsdatum ||
                (data.eingangsdatum &&
                    this.formatDate(data.eingangsdatum!)
                        .toLowerCase()
                        .includes(searchFilter.eingangsdatum.toLowerCase()));
            const angeboteMatch =
                !searchFilter.angebote ||
                data.angebote?.some(a => a === searchFilter.angebote);
            const institutionenMatch =
                !searchFilter.institutionen ||
                data.institutionen?.some(i => i === searchFilter.institutionen);

            return (
                !!betreuungsNummerMatch &&
                !!gemeindeMatch &&
                !!nameMatch &&
                !!vornameMatch &&
                !!periodenStringMatch &&
                !!geburtsdatumMatch &&
                !!antragTypMatch &&
                !!eingangsdatumMatch &&
                !!angeboteMatch &&
                !!institutionenMatch
            );
        };
    }

    public filterBetreuung(query: string): void {
        this.filterPredicate.betreuungsNummer = query;
        this.applyFilter();
    }

    public filterGemeinde(query: string): void {
        this.filterPredicate.gemeinde = query;
        this.applyFilter();
    }

    public filterName(query: string): void {
        this.filterPredicate.name = query;
        this.applyFilter();
    }

    public filterVorname(query: string): void {
        this.filterPredicate.vorname = query;
        this.applyFilter();
    }

    public filterGeburtsdatum(query: string): void {
        this.filterPredicate.geburtsdatum = query;
        this.applyFilter();
    }

    public filterAntragTyp(query: string): void {
        this.filterPredicate.antragTyp = query;
        this.applyFilter();
    }

    public filterEingangsdatum(query: string): void {
        this.filterPredicate.eingangsdatum = query;
        this.applyFilter();
    }

    public filterAngebot(query: string): void {
        this.filterPredicate.angebote = query;
        this.applyFilter();
    }

    public filterInstitution(query: string): void {
        this.filterPredicate.institutionen = query;
        this.applyFilter();
    }

    public filterPeriode(query: string): void {
        this.filterPredicate.periodenString = query;
        this.applyFilter();
    }

    private applyFilter(): void {
        this.datasource.filter = JSON.stringify(this.filterPredicate);
        this.totalItems = this.datasource.filteredData.length;
        this.cd.markForCheck();
    }

    public loadData(): void {
        this.pendenzBetreuungenService.getPendenzenBetreuungenList().subscribe({
            next: response => {
                this.datasource.data = response.map(
                    item =>
                        ({
                            betreuungsNummer: item.betreuungsNummer,
                            gemeinde: item.gemeindeName,
                            name: item.name,
                            vorname: item.vorname,
                            geburtsdatum: item.geburtsdatum
                                ? item.geburtsdatum.toDate()
                                : undefined,
                            antragTyp: item.typ,
                            periodenString: item.gesuchsperiodeString,
                            eingangsdatum: item.eingangsdatum
                                ? item.eingangsdatum.toDate()
                                : undefined,
                            angebote: item.betreuungsangebotTyp
                                ? [item.betreuungsangebotTyp]
                                : [],
                            institutionen: item.institutionName
                                ? [item.institutionName]
                                : [],
                            gesuchId: item.gesuchId
                        }) as DVPendenzenBetreuungenAntragList
                );
                this.datasource.sort = this.sort;
                this.sort.active = 'eingangsdatum';
                this.sort.direction = 'desc';
                this.totalItems = this.datasource.data.length;
                this.cd.markForCheck();
            },
            error: error => LOG.error(error)
        });
    }

    public resetFilter(): void {
        this.filterPredicate = {};
        this.datasource.filter = '';
        this.totalItems = this.datasource.data.length;
        this.cd.markForCheck();
    }

    public openBetreuung(element: DVPendenzenBetreuungenAntragList): void {
        if (!element.betreuungsNummer) {
            return;
        }
        const numberParts = this.ebeguUtil.splitBetreuungsnummer(
            element.betreuungsNummer
        );
        if (!numberParts) {
            return;
        }

        const navObj = {
            betreuungNumber: parseInt(numberParts.betreuungsnummer, 10),
            kindNumber: parseInt(numberParts.kindnummer, 10),
            gesuchId: (element as any).gesuchId
        };

        this.stateService.go('gesuch.betreuung', navObj);
    }

    public createAngeboteString(angebote: any[] | undefined): string {
        if (!angebote || angebote.length === 0) {
            return '';
        }
        const translatedAngebote = angebote.map(angebot =>
            this.translateService.instant(angebot)
        );
        return translatedAngebote.join(', ');
    }

    public getAntragTypBezeichnung(
        element: DVPendenzenBetreuungenAntragList
    ): string {
        return this.translateService.instant(element.antragTyp || '');
    }

    private formatDate(date: Date): string {
        const d = new Date(date);
        let month = '' + (d.getMonth() + 1);
        let day = '' + d.getDate();
        const year = d.getFullYear();

        if (month.length < 2) {
            month = '0' + month;
        }
        if (day.length < 2) {
            day = '0' + day;
        }

        return [day, month, year].join('.');
    }

    private initHasInstitutionenInStatusAngemeldet(): void {
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getInstitutionProfilEditRoles()
            )
        ) {
            return;
        }
        this.institutionRS.hasInstitutionenInStatusAngemeldet().subscribe({
            next: result => {
                this.hasInstitutionenInStatusAngemeldet = result;
            },
            error: error => LOG.error(error)
        });
    }
}
