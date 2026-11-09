import {
    ChangeDetectionStrategy,
    Component,
    computed,
    effect,
    inject,
    input,
    model,
    Signal
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    ConfigurableEinstellung,
    EditEinstellungComponent
} from '@admin/einstellungen';
import {combineLatest} from 'rxjs';
import {SharedModule} from '../../app/shared/shared.module';
import {rxResource} from '@angular/core/rxjs-interop';
import {TSGesuchsperiode} from '../../models/entity/TSGesuchsperiode';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import {LogFactory} from '@utils/log';
import {KibonConfigurationGroup} from '../einstellungen/kibon-configuration-group/kibon-configuration-group.component';
import {TSEinstellungWithMetadata} from '../einstellungen/TSEinstellungWithMetadata';
import {EinstellungRS} from '../service/einstellungRS.rest';
import {MatTableDataSource} from '@angular/material/table';
import {TSEinstellung} from '../einstellungen/TSEinstellung';
import {TranslateService} from '@ngx-translate/core';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {GesuchsperiodeRS} from '../../app/core/service/gesuchsperiodeRS.rest';
import {GlobalCacheService} from '../../legacy-gesuch/service/globalCacheService';
import {TSEinstellungKey} from '../einstellungen/TSEinstellungKey';
import {DokumenteZuUebernehmenEinstellungGroupComponent} from './dokumente-zu-uebernehmen-einstellung-group/dokumente-zu-uebernehmen-einstellung-group.component';
import {EinstellungGrouper, EinstellungGroup} from './EinstellungGrouper';
import {sortByKeyOrder} from '../einstellungen/KibonConfigurationOrder';

const LOG = LogFactory.createLog('GesuchsperiodeViewXComponent');

@Component({
    selector: 'lib-admin-gesuchsperioden-einstellungen',
    imports: [
        CommonModule,
        SharedModule,
        EditEinstellungComponent,
        DokumenteZuUebernehmenEinstellungGroupComponent,
        KibonConfigurationGroup
    ],
    templateUrl: './gesuchsperioden-einstellungen.component.html',
    styleUrl: './gesuchsperioden-einstellungen.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchsperiodenEinstellungenComponent {
    private readonly einstellungenRS = inject(EinstellungRS);
    private readonly translate = inject(TranslateService);
    private readonly authService = inject(AuthServiceRS);
    private readonly gesuchsperiodenService = inject(GesuchsperiodeRS);
    private readonly globalCacheService = inject(GlobalCacheService);
    private readonly dokuemenZuUebernehmenEinstellung = [
        TSEinstellungKey.DOKUMENT_ZU_UEBERNEHMEN_TYPS
    ];
    private readonly changedEinstellungen: Map<
        TSEinstellungKey,
        TSEinstellung
    > = new Map();

    gesuchsperiode = input.required<TSGesuchsperiode>();

    filter = model<string>('');

    trimmedAndLocalizedFilter = computed(() =>
        this.filter().trim().toLocaleLowerCase()
    );

    einstellungenRes = rxResource({
        defaultValue: [],
        params: () => ({id: this.gesuchsperiode().id}),
        stream: ({params}) =>
            this.einstellungenRS.getAllEinstellungenActiveForMandantWithMetadata(
                params.id
            )
    });

    filteredAndGroupedEinstellungen = computed(() => {
        const filter = this.trimmedAndLocalizedFilter();
        return EinstellungGrouper.groupEinstellungen(
            filter.length > 0
                ? this.einstellungenRes
                      .value()
                      .filter(einstellung =>
                          this.doesEinstellungMatchFilter(einstellung, filter)
                      )
                : this.einstellungenRes.value()
        );
    });

    ungroupedEinstellungen: Signal<TSEinstellungWithMetadata[]> = computed(
        () => {
            return this.filteredAndGroupedEinstellungen().ungrouped ?? [];
        }
    );

    groupedEinstellungen: Signal<EinstellungGroup[]> = computed(() => {
        return this.filteredAndGroupedEinstellungen().grouped;
    });

    tableData = computed(() => {
        const value = this.ungroupedEinstellungen();
        const tableEinstellungen = value.filter(
            einstellung =>
                !this.dokuemenZuUebernehmenEinstellung.includes(einstellung.key)
        );
        return new MatTableDataSource<TSEinstellung>(
            sortByKeyOrder(tableEinstellungen)
        );
    });

    dokumenteZuUebernehmenEinstellung = computed(() => {
        return this.einstellungenRes
            .value()
            .find(
                einstellung =>
                    einstellung.key ===
                    TSEinstellungKey.DOKUMENT_ZU_UEBERNEHMEN_TYPS
            );
    });

    readonly displayedColumns: string[] = ['key', 'value'];

    constructor() {
        effect(() => {
            const filter = this.trimmedAndLocalizedFilter();
            const dataSource = this.tableData();
            dataSource.filter = filter.trim().toLocaleLowerCase();
        });
    }

    public periodenParamsEditableForPeriode(
        gesuchsperiode: TSGesuchsperiode
    ): boolean {
        if (gesuchsperiode?.status) {
            // Fuer SuperAdmin immer auch editierbar, wenn AKTIV oder INAKTIV, sonst nur ENTWURF
            if (TSGesuchsperiodeStatus.GESCHLOSSEN === gesuchsperiode.status) {
                return false;
            }
            if (
                this.authService.isOneOfRoles(TSRoleUtil.getSuperAdminRoles())
            ) {
                return true;
            }
            return TSGesuchsperiodeStatus.ENTWURF === gesuchsperiode.status;
        }
        return false;
    }

    public saveParameterByGesuchsperiode(): void {
        const possibilityChangeEinstellungen = Array.from(
            this.changedEinstellungen.values()
        ).concat(this.dokumenteZuUebernehmenEinstellung() ?? []);

        combineLatest(
            possibilityChangeEinstellungen
                .filter(e => e.value != 'null')
                .map(e => this.einstellungenRS.saveEinstellung(e))
        ).subscribe({
            next: () => {
                this.globalCacheService
                    .getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN)
                    .removeAll();
                this.gesuchsperiodenService.updateActiveGesuchsperiodenList();
                this.gesuchsperiodenService.updateNichtAbgeschlosseneGesuchsperiodenList();
                this.changedEinstellungen.clear();
                this.einstellungenRes.reload();
            },
            error: error => LOG.error(error)
        });
    }

    public isReadonly(): boolean {
        return !this.authService.isOneOfRoles(
            TSRoleUtil.getJAAdministratorRoles()
        );
    }

    /**
     * Updates the specified changed configurable setting in the collection of changed settings. The related
     * Einstellung is retrieved from the list of grouped and ungrouped entries. If the setting is already present in
     * the collection, its value is updated with the new value from the changed configurable setting. If the setting
     * is not present, it is added to the collection. The Einstellung for dokumenteZuUebernehmen is always saved
     *
     * @param {ConfigurableEinstellung} changedConfigurable - The configurable setting object that has been updated, containing its key and new value.
     * @return {void} No return value.
     */
    protected updateChangedEinstellung(
        changedConfigurable: ConfigurableEinstellung
    ): void {
        const einstellung =
            this.changedEinstellungen.get(
                changedConfigurable.key as TSEinstellungKey
            ) ??
            this.getAllEinstellungen().find(
                groupedEinstellung =>
                    groupedEinstellung.key === changedConfigurable.key
            );
        if (!einstellung) {
            throw new Error(
                `Einstellung with key ${changedConfigurable.key} not found`
            );
        }
        einstellung.value = changedConfigurable.value;
        this.changedEinstellungen.set(
            changedConfigurable.key as TSEinstellungKey,
            einstellung
        );
    }

    private getAllEinstellungen(): TSEinstellung[] {
        return (
            this.groupedEinstellungen().flatMap(
                entry => entry.configurations
            ) as TSEinstellung[]
        ).concat(this.ungroupedEinstellungen());
    }

    private doesEinstellungMatchFilter(
        einstellung: TSEinstellungWithMetadata,
        filter: string
    ): any {
        return (
            einstellung.metadata.keyGroup
                ?.toLocaleLowerCase()
                .includes(filter) ||
            einstellung.erklaerung?.toLocaleLowerCase().includes(filter) ||
            einstellung.key.toLocaleLowerCase().includes(filter) ||
            this.translate
                .instant(einstellung.key)
                .toLocaleLowerCase()
                .includes(filter)
        );
    }
}
