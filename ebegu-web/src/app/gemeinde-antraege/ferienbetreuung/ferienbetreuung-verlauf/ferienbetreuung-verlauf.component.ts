import {Location} from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import moment from 'moment';
import {CONSTANTS} from '@models/constants';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '@utils/log';
import {DvSimpleTableColumnDefinition} from '../../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {DvSimpleTableConfig} from '../../../shared/component/dv-simple-table/dv-simple-table-config';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';
import {FerienbetreuungStatusHistory} from '../../../../models/gemeindeantrag/ferienbetreuung/dto/FerienbetreuungStatusHistory';

const LOG = LogFactory.createLog('FerienbetreuungVerlaufComponent');

@Component({
    selector: 'dv-ferienbetreuung-verlauf',
    templateUrl: './ferienbetreuung-verlauf.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FerienbetreuungVerlaufComponent implements OnInit {
    private readonly ferienbetreuungService = inject(FerienbetreuungService);
    private readonly errorService = inject(ErrorService);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly location = inject(Location);
    private readonly uiRouterGlobals = inject(UIRouterGlobals);

    public history: {timestampVon: number; status: string; benutzer: string}[];
    public columns: DvSimpleTableColumnDefinition[] = [
        {
            displayedName: 'DATUM',
            attributeName: 'timestampVon',
            displayFunction: (d: number) =>
                moment(d).format(CONSTANTS.DATE_TIME_FORMAT)
        },
        {
            displayedName: 'AKTION',
            attributeName: 'status'
        },
        {
            displayedName: 'BENUTZER',
            attributeName: 'benutzer'
        }
    ];
    public tableConfig = new DvSimpleTableConfig('timestampVon', 'desc', false);

    public ngOnInit(): void {
        const containerId = this.uiRouterGlobals.params.id;
        if (!containerId) {
            throw new Error('Invalid state: No containerId found in route');
        }

        this.ferienbetreuungService.updateFerienbetreuungContainerStores(
            containerId
        );

        this.ferienbetreuungService
            .getHistory({id: containerId} as any)
            .subscribe({
                next: data => {
                    this.mapHistoryForSimpleTable(data);
                    this.cd.markForCheck();
                },
                error: error => {
                    LOG.error(error);
                    this.errorService.handleError(error);
                }
            });
    }

    private mapHistoryForSimpleTable(
        data: FerienbetreuungStatusHistory[]
    ): void {
        this.history = data.map(d => ({
            timestampVon: d.timestampVon.getTime(),
            status: d.status,
            benutzer: d.benutzer.getFullName()
        }));
    }

    public navigateBack(): void {
        this.location.back();
    }
}
