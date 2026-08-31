import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MeldungsfensterService} from '../../../utils/meldungsfenster/meldungsfenster.service';
import {TranslateModule} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {MeldungsfensterData} from '@models/meldungsfenster';
import {MeldungsfensterFormComponent} from '../meldungsfenster-form/meldungsfenster-form.component';

@Component({
    selector: 'lib-admin-ui-meldungsfenster-edit',
    imports: [MeldungsfensterFormComponent, TranslateModule],
    templateUrl: './meldungsfenster-edit.component.html',
    styleUrl: './meldungsfenster-edit.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeldungsfensterEditComponent {
    private readonly params = inject(UIRouterGlobals);
    private readonly service = inject(MeldungsfensterService);
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
