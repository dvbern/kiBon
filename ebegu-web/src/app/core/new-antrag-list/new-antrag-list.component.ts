import {Component, OnInit, ChangeDetectionStrategy, OnDestroy, ChangeDetectorRef, ViewChild} from '@angular/core';
import {MatPaginator, MatTable, MatTableDataSource, PageEvent} from '@angular/material';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitution} from '../../../models/TSInstitution';
import {LogFactory} from '../logging/LogFactory';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

const LOG = LogFactory.createLog('DVAntragListController');

@Component({
    selector: 'dv-new-antrag-list',
    templateUrl: './new-antrag-list.component.html',
    styleUrls: ['./new-antrag-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NewAntragListComponent implements OnInit, OnDestroy {

    @ViewChild(MatPaginator) public paginator: MatPaginator;
    @ViewChild(MatTable) private table: MatTable<Partial<TSAntragDTO>>;

    public gesuchsperiodenList: Array<string> = [];
    public institutionenList: Array<TSInstitution> = [];
    public gemeindenList: Array<TSGemeinde> = [];

    public datasource: MatTableDataSource<Partial<TSAntragDTO>>;
    public displayedColumns: string[] = [
        'fallNummer',
        'gemeinde',
        'familienName',
        'kinder',
        'antragTyp',
        'periode',
        'aenderungsdatum',
        'status',
        'dokumenteHochgeladen',
        'angebot',
        'institution',
        'verantwortlicheTS',
        'verantwortlicheBG'
    ];

    public filterColumns = {
        fallNummer: {
            type: 'input',
            callback: this.filterFall
        }
    };

    private filterPredicate: {
        fallNummer?: string,
        gemeinde?: string,
        familienName?: string,
        antragTyp?: string,
        gesuchsperiodeString?: string,
        eingangsdatum?: string,
        eingangsdatumSTV?: string,
        aenderungsdatum?: string,
        status?: string,
        dokumenteHochgeladen?: boolean,
        angebote?: string,
        institutionen?: string,
        verantwortlicherBG?: string,
        verantwortlicherTS?: string,
        verantwortlicherGemeinde?: string,
        kinder?: string,
        familienNameForLike?: string,
        kindNameForLike?: string
    } = {};

    private readonly unsubscribe$ = new Subject<void>();

    public totalItems: number = 0;
    public page: number = 0;
    public pageSize: any = 10;

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly searchRS: SearchRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
        this.initTable();
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().then(response => {
            this.institutionenList = response;
        });
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            response.forEach(gesuchsperiode => {
                this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
            });
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err),
            );
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initTable(): void {
        this.datasource = new MatTableDataSource<TSAntragDTO>([]);
        this.loadData();
    }

    private loadData(): void {
        const body = {
            pagination: {
                number: this.pageSize,
                start: this.page * this.pageSize,
            },
            search: {
                predicateObject: this.filterPredicate
            },
        };
        this.searchRS.searchAntraege(body).then((result: TSAntragSearchresultDTO) => {
            const displayedFaelle: Partial<TSAntragDTO>[] =
                result.antragDTOs.map(antragDto => {
                    return {
                        fallNummer: antragDto.fallNummer,
                        gemeinde: antragDto.gemeinde,
                        status: antragDto.status,
                        familienName: antragDto.familienName,
                        kinder: antragDto.kinder,
                        antragTyp: antragDto.antragTyp,
                        periode: antragDto.gesuchsperiodeString,
                        aenderungsdatum: antragDto.aenderungsdatum,
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG
                    };
                });
            this.datasource.data = displayedFaelle;
            this.totalItems = result.totalResultSize;
            // TODO: we need this because the angualarJS Service returns an IPromise. Angular does not detect changes in
            //  these since they are not zone-aware. Remove once the service is migrated
            this.changeDetectorRef.markForCheck();
        });
    }

    public handlePagination(pageEvent: PageEvent): void {
        this.page = pageEvent.pageIndex;
        this.pageSize = pageEvent.pageSize;
        this.loadData();
    }

    public filterFall(query: string): void {
        console.log(query);
        this.filterPredicate.fallNummer = query;
        this.loadData();
    }
}
