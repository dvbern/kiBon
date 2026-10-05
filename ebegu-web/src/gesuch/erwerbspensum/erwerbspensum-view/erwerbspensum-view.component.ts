import {ChangeDetectionStrategy, Component, model} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../app/shared/shared.module';
import {GesuchPageHeadingComponent} from '../../shared/heading';
import {GesuchPageLayoutComponent} from '../../shared/page-layout';
import {KibTooltip} from '../../shared/kib-tooltip/kib-tooltip';

@Component({
    selector: 'lib-gesuch-erwerbspensum-view',
    imports: [
        TranslateModule,
        SharedModule,
        GesuchPageHeadingComponent,
        GesuchPageLayoutComponent,
        KibTooltip
    ],
    templateUrl: './erwerbspensum-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErwerbspensumViewComponent {
    hasBisher = model(false);
}
