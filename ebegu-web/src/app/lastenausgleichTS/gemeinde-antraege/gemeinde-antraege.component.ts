import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, NgForm, Validators} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject, combineLatest, Observable, of} from 'rxjs';
import {catchError, map, mergeMap, tap} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeindeStatus} from '../../../models/enums/TSLastenausgleichTagesschuleAngabenGemeindeStatus';
import {TSGemeindeAntrag} from '../../../models/gemeindeantrag/TSGemeindeAntrag';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {GemeindeAntragService} from '../services/gemeinde-antrag.service';

@Component({
    selector: 'dv-gemeinde-antraege',
    templateUrl: './gemeinde-antraege.component.html',
    styleUrls: ['./gemeinde-antraege.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeAntraegeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public hiddenDVTableColumns = [
        'fallNummer',
        'familienName',
        'kinder',
        'aenderungsdatum',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public antragList$: Observable<DVAntragListItem[]>;
    public gesuchsperioden: TSGesuchsperiode[];
    public formGroup: FormGroup;
    public totalItems: number;

    private readonly filterDebounceSubject: BehaviorSubject<DVAntragListFilter> =
        new BehaviorSubject<DVAntragListFilter>({});

    private readonly sortDebounceSubject: BehaviorSubject<{
        predicate?: string,
        reverse?: boolean
    }> = new BehaviorSubject<{ predicate?: string; reverse?: boolean }>({});

    public constructor(
        public readonly gemeindeAntragService: GemeindeAntragService,
        private readonly gesuchsperiodenService: GesuchsperiodeRS,
        private readonly fb: FormBuilder,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.loadData();
        this.gesuchsperiodenService.getAllActiveGesuchsperioden().then(result => this.gesuchsperioden = result);
        this.formGroup = this.fb.group({
            periode: ['', Validators.required],
            antragTyp: ['', Validators.required],
        });
    }

    private loadData(): void {
        this.antragList$ = combineLatest([this.filterDebounceSubject, this.sortDebounceSubject]).pipe(
            mergeMap(filterAndSort => this.gemeindeAntragService.getGemeindeAntraege(filterAndSort[0], filterAndSort[1])
                .pipe(catchError(() => this.translate.get('DATA_RETRIEVAL_ERROR').pipe(
                    tap(msg => this.errorService.addMesageAsError(msg)),
                    mergeMap(() => of([] as TSGemeindeAntrag[])),
                )))),
            map(gemeindeAntraege => {
                return gemeindeAntraege.map(antrag => {
                    return {
                        antragId: antrag.id,
                        gemeinde: antrag.gemeinde.name,
                        status: antrag.statusString,
                        periode: antrag.gesuchsperiode.gesuchsperiodeString,
                        antragTyp: antrag.gemeindeAntragTyp,
                    };
                });
            }),
            tap(gemeindeAntraege => this.totalItems = gemeindeAntraege.length),
        );
    }

    public createAntrag(): void {
        if (!this.formGroup.valid) {
            return;
        }
        this.gemeindeAntragService.createAntrag(this.formGroup.value).subscribe(() => {
            this.loadData();
        }, error => {
            this.translate.get('CREATE_ANTRAG_ERROR', error).subscribe(message => {
                this.errorService.addMesageAsError(message);
            }, translateError => console.error('Could no translate', translateError));
        });
    }

    public navigate(antrag: DVAntragListItem, event: MouseEvent): void {
        const path = 'LASTENAUSGLEICH_TS';
        const navObj = {
            id: antrag.antragId,
        };
        if (event.ctrlKey) {
            const url = this.$state.href(path, navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go(path, navObj);
        }
    }

    public onFilterChange(filterChange: DVAntragListFilter): void {
        this.filterDebounceSubject.next(filterChange);
    }

    public getStateFilter(): string[] {
        return Object.keys(TSLastenausgleichTagesschuleAngabenGemeindeStatus);
    }

    public onSortChange(sortChange: { predicate?: string; reverse?: boolean }): void {
        this.sortDebounceSubject.next(sortChange);
    }
}
