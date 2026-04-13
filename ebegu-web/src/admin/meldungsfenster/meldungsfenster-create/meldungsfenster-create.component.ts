import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {TSRole} from '../../../models/enums/TSRole';
import {DateUtil} from '../../../utils/date/DateUtil';
import {MeldungsfensterService} from '../../../utils/meldungsfenster/meldungsfenster.service';
import {TranslateModule} from '@ngx-translate/core';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '@models/meldungsfenster';
import {StateService} from '@uirouter/core';
import {MeldungsfensterFormComponent} from '../meldungsfenster-form/meldungsfenster-form.component';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-create',
    imports: [MeldungsfensterFormComponent, TranslateModule],
    templateUrl: './meldungsfenster-create.component.html',
    styleUrl: './meldungsfenster-create.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeldungsfensterCreateComponent {
    private readonly service = inject(MeldungsfensterService);
    protected readonly stateService = inject(StateService);
    toCreate = {
        zielgruppe: [] as TSRole[],
        titleDe: '',
        titleFr: '',
        inhaltDe: '',
        inhaltFr: '',
        status: MeldungsfensterStatus.INFO,
        gueltigAb: DateUtil.toNextHalfHour(new Date()),
        gueltigBis: DateUtil.toNextHalfHour(new Date())
    } satisfies MeldungsfensterData;

    createMeldungsfenster(data: MeldungsfensterData) {
        this.service
            .createNewMeldungsfenster(data)
            .subscribe(() => this.stateService.go('admin.meldungfenster'));
    }
}
