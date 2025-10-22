import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {AdminUtilMeldungsfensterService} from '@kibon/shared-util-meldungsfenster';
import {rxResource} from '@angular/core/rxjs-interop';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {AdminUiMeldungsfensterFormComponent} from '@kibon/admin-ui-meldungsfenster-form';

@Component({
    imports: [CommonModule, SharedModule, AdminUiMeldungsfensterFormComponent],
    templateUrl: 'admin-pattern-meldungsfenster-detail.component.html',
    styleUrl: 'admin-pattern-meldungsfenster-detail.component.less'
})
export class AdminPatternMeldungsfensterDetailComponent {
    private readonly params = inject(UIRouterGlobals);
    private readonly service = inject(AdminUtilMeldungsfensterService);
    protected readonly stateService = inject(StateService);

    meldungsfensterResourceRef = rxResource({
        loader: () => this.service.getMeldungsfenster(this.params.params.id)
    });
}
