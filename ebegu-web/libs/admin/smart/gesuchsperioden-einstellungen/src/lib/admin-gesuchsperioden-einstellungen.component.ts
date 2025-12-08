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
import {TSGesuchsperiode} from '@kibon/shared/model/entity';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {rxResource} from '@angular/core/rxjs-interop';
import {EinstellungRS} from '../../../../../../src/admin/service/einstellungRS.rest';
import {MatTableDataSource} from '@angular/material/table';
import {TSEinstellung} from '../../../../../../src/admin/einstellungen/TSEinstellung';
import {TranslateService} from '@ngx-translate/core';
import {TSCacheTyp, TSGesuchsperiodeStatus} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../../../../src/utils/TSRoleUtil';
import {AuthServiceRS} from '../../../../../../src/authentication/service/AuthServiceRS.rest';
import {GesuchsperiodeRS} from '../../../../../../src/app/core/service/gesuchsperiodeRS.rest';
import {GlobalCacheService} from '../../../../../../src/gesuch/service/globalCacheService';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {EditEinstellungComponent} from '@kibon/admin-edit-einstellung';
import {TSEinstellungKey} from '../../../../../../src/admin/einstellungen/TSEinstellungKey';
import {ErneuerbareDokumenteEinstellungGroupComponent} from './erneuerbare-dokumente-einstellung-group/erneuerbare-dokumente-einstellung-group.component';

const LOG = LogFactory.createLog('GesuchsperiodeViewXComponent');

@Component({
    selector: 'lib-admin-gesuchsperioden-einstellungen',
    imports: [
        CommonModule,
        SharedModule,
        EditEinstellungComponent,
        ErneuerbareDokumenteEinstellungGroupComponent
    ],
    templateUrl: './admin-gesuchsperioden-einstellungen.component.html',
    styleUrl: './admin-gesuchsperioden-einstellungen.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminGesuchsperiodenEinstellungenComponent {
    private readonly einstellungenRS = inject(EinstellungRS);
    private readonly translate = inject(TranslateService);
    private readonly authService = inject(AuthServiceRS);
    private readonly gesuchsperiodenService = inject(GesuchsperiodeRS);
    private readonly globalCacheService = inject(GlobalCacheService);
    private readonly groupedEinstellungen = [
        TSEinstellungKey.ERNEUERBARE_DOKUMENT_TYPS
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

    erneuerbareDokuementeEinstellungEinstellung = computed(() => {
        return this.einstellungenRes
            .value()
            ?.find(
                einstellung =>
                    einstellung.key ===
                    TSEinstellungKey.ERNEUERBARE_DOKUMENT_TYPS
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
            this.erneuerbareDokuementeEinstellungEinstellung() ?? []
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
