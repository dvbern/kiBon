import {ChangeDetectionStrategy, Component, model} from '@angular/core';

import {GesuchPageHeadingComponent} from '@kibon/gesuch-heading';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {GesuchPageLayoutComponent} from '@kibon/gesuch-page-layout';

@Component({
    selector: 'lib-gesuch-erwerbspensum-view',
    imports: [
        GesuchPageHeadingComponent,
        TranslateModule,
        SharedModule,
        GesuchPageLayoutComponent
    ],
    templateUrl: './gesuch-erwerbspensumView.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchErwerbspensumViewComponent {
    hasBisher = model(false);
}
