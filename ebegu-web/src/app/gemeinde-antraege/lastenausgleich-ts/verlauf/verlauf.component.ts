import {Location} from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnInit,
    inject
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {TSLastenausgleichTagesschulenStatusHistory} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschulenStatusHistory';
import {CONSTANTS} from '@models/constants';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {LogFactory} from '@utils/log';
import {DvSimpleTableColumnDefinition} from '../../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {DvSimpleTableConfig} from '../../../shared/component/dv-simple-table/dv-simple-table-config';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

const LOG = LogFactory.createLog('VerlaufComponent');

@Component({
    selector: 'dv-verlauf',
    templateUrl: './verlauf.component.html',
    styleUrls: ['./verlauf.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class VerlaufComponent implements OnInit {
    private readonly lastenausgleichTSService = inject(
        LastenausgleichTSService
    );
    private readonly errorService = inject(ErrorService);
    private readonly $translate = inject(TranslateService);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly location = inject(Location);

    @Input() public lastenausgleichId: string;

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
        this.lastenausgleichTSService
            .getVerlauf(this.lastenausgleichId)
            .subscribe({
                next: data => {
                    console.log(data);
                    this.mapHistoryForSimpleTable(data);
                    this.cd.markForCheck();
                },
                error: error => {
                    LOG.error(error);
                }
            });
    }

    private mapHistoryForSimpleTable(
        data: TSLastenausgleichTagesschulenStatusHistory[]
    ): void {
        this.history = data.map(d => ({
            timestampVon: d.timestampVon.toDate().getTime(),
            status: d.status,
            benutzer: d.benutzer.getFullName()
        }));
    }

    public navigateBack(): void {
        this.location.back();
    }
}
