import {ChangeDetectionStrategy, Component, inject} from '@angular/core';

import {AdminUiMeldungsfensterFormComponent} from '@kibon/admin-ui-meldungsfenster-form';
import {TranslateModule} from '@ngx-translate/core';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '@kibon/shared-model-meldungsfenster';
import {AdminUtilMeldungsfensterService} from '@kibon/shared-util-meldungsfenster';
import {StateService} from '@uirouter/core';
import {TSRole} from '@kibon/shared/model/enums';
import {DateUtil} from '@kibon/shared/util-fn/date';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-create',
    imports: [AdminUiMeldungsfensterFormComponent, TranslateModule],
    templateUrl: './admin-ui-meldungsfenster-create.component.html',
    styleUrl: './admin-ui-meldungsfenster-create.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUiMeldungsfensterCreateComponent {
    private readonly service = inject(AdminUtilMeldungsfensterService);
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
