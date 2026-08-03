import {Component, inject, ChangeDetectionStrategy} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {MeldungsfensterService} from '../../../utils/meldungsfenster/meldungsfenster.service';
import {MeldungsfensterFormComponent} from '../meldungsfenster-form/meldungsfenster-form.component';

@Component({
    imports: [MeldungsfensterFormComponent, TranslatePipe],
    templateUrl: 'meldungsfenster-detail.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: 'meldungsfenster-detail.component.less'
})
export class MeldungsfensterDetailComponent {
    private readonly params = inject(UIRouterGlobals);
    private readonly service = inject(MeldungsfensterService);
    protected readonly stateService = inject(StateService);

    meldungsfensterResourceRef = rxResource({
        stream: () => this.service.getMeldungsfenster(this.params.params.id)
    });
}
