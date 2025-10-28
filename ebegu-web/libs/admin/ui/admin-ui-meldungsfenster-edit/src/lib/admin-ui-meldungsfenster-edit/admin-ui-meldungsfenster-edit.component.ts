import {ChangeDetectionStrategy, Component, inject} from '@angular/core';

import {AdminUiMeldungsfensterFormComponent} from '@kibon/admin-ui-meldungsfenster-form';
import {TranslateModule} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {AdminUtilMeldungsfensterService} from '@kibon/shared-util-meldungsfenster';
import {rxResource} from '@angular/core/rxjs-interop';
import {MeldungsfensterData} from '@kibon/shared-model-meldungsfenster';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-edit',
    imports: [AdminUiMeldungsfensterFormComponent, TranslateModule],
    templateUrl: './admin-ui-meldungsfenster-edit.component.html',
    styleUrl: './admin-ui-meldungsfenster-edit.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUiMeldungsfensterEditComponent {
    private readonly params = inject(UIRouterGlobals);
    private readonly service = inject(AdminUtilMeldungsfensterService);
    protected readonly stateService = inject(StateService);

    meldungsfensterResourceRef = rxResource({
        stream: () => this.service.getMeldungsfenster(this.params.params.id)
    });

    updateMeldungsfenster(data: MeldungsfensterData) {
        this.service.updateMeldungsfenster(data).subscribe(() => {
            this.stateService.go('admin.meldungfenster');
        });
    }
}
