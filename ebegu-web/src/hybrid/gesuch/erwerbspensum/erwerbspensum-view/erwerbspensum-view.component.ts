import {ChangeDetectionStrategy, Component, model} from '@angular/core';
import {GesuchPageHeadingComponent} from '@gesuch/heading';
import {GesuchPageLayoutComponent} from '@gesuch/page-layout';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../../app/shared/shared.module';

@Component({
    selector: 'lib-gesuch-erwerbspensum-view',
    imports: [
        TranslateModule,
        SharedModule,
        GesuchPageHeadingComponent,
        GesuchPageLayoutComponent
    ],
    templateUrl: './erwerbspensum-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErwerbspensumViewComponent {
    hasBisher = model(false);
}
