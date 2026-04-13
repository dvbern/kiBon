import {
    ChangeDetectionStrategy,
    Component,
    computed,
    effect,
    inject,
    input,
    model
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {EditEinstellungComponent} from '@admin/einstellungen';
import {SharedModule} from '../../app/shared/shared.module';
import {rxResource} from '@angular/core/rxjs-interop';
import {TSGesuchsperiode} from '../../models/entity/TSGesuchsperiode';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import {LogFactory} from '../../utils/log-factory/LogFactory';
import {EinstellungRS} from '../service/einstellungRS.rest';
import {MatTableDataSource} from '@angular/material/table';
import {TSEinstellung} from '../einstellungen/TSEinstellung';
import {TranslateService} from '@ngx-translate/core';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {GesuchsperiodeRS} from '../../app/core/service/gesuchsperiodeRS.rest';
import {GlobalCacheService} from '../../gesuch/service/globalCacheService';
import {TSEinstellungKey} from '../einstellungen/TSEinstellungKey';
import {DokumenteZuUebernehmenEinstellungGroupComponent} from './dokumente-zu-uebernehmen-einstellung-group/dokumente-zu-uebernehmen-einstellung-group.component';

const LOG = LogFactory.createLog('GesuchsperiodeViewXComponent');

@Component({
    selector: 'lib-admin-gesuchsperioden-einstellungen',
    imports: [
        CommonModule,
        SharedModule,
        EditEinstellungComponent,
        DokumenteZuUebernehmenEinstellungGroupComponent
    ],
    templateUrl: './gesuchsperioden-einstellungen.component.html',
    styleUrl: './gesuchsperioden-einstellungen.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchsperiodenEinstellungenComponent {
    private readonly einstellungenRS = inject(EinstellungRS);
    private readonly translate = inject(TranslateService);
    private readonly authService = inject(AuthServiceRS);
    private readonly gesuchsperiodenService = inject(GesuchsperiodeRS);
    private readonly globalCacheService = inject(GlobalCacheService);
    private readonly groupedEinstellungen = [
        TSEinstellungKey.DOKUMENT_ZU_UEBERNEHMEN_TYPS
    ];

    gesuchsperiode = input.required<TSGesuchsperiode>();

    filter = model<string>('');

    einstellungenRes = rxResource({
        params: () => ({id: this.gesuchsperiode().id}),
        stream: ({params}) =>
            this.einstellungenRS.getAllEinstellungenActiveForMandantBySystem(
                params.id
            )
    });

    tableData = computed(() => {
        const value = this.einstellungenRes.value();
        if (!value) {
            return new MatTableDataSource<TSEinstellung>([]);
        }
        const tableEinstellungen = value.filter(
            einstellung => !this.groupedEinstellungen.includes(einstellung.key)
        );
        tableEinstellungen.sort((a, b) =>
            this.translate
                .instant(a.key.toString())
                .localeCompare(this.translate.instant(b.key.toString()))
        );
        return new MatTableDataSource<TSEinstellung>(tableEinstellungen);
    });

    dokumenteZuUebernehmenEinstellung = computed(() => {
        return this.einstellungenRes
            .value()
            ?.find(
                einstellung =>
                    einstellung.key ===
                    TSEinstellungKey.DOKUMENT_ZU_UEBERNEHMEN_TYPS
            );
    });

    readonly displayedColumns: string[] = ['key', 'value'];

    constructor() {
        effect(() => {
            const filter = this.filter();
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
        const allEinstellungen = this.tableData().data.concat(
            this.dokumenteZuUebernehmenEinstellung() ?? []
        );
        allEinstellungen.forEach((param: TSEinstellung) => {
            if (param.value != 'null') {
                this.einstellungenRS.saveEinstellung(param).subscribe({
                    next: () => {},
                    error: error => LOG.error(error)
                });
            }
        });
        this.globalCacheService
            .getCache(TSCacheTyp.EBEGU_EINSTELLUNGEN)
            .removeAll();
        this.gesuchsperiodenService.updateActiveGesuchsperiodenList();
        this.gesuchsperiodenService.updateNichtAbgeschlosseneGesuchsperiodenList();
    }

    public isReadonly(): boolean {
        return !this.authService.isOneOfRoles(
            TSRoleUtil.getJAAdministratorRoles()
        );
    }
}
