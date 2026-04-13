import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HeadingHeaderComponent} from '../internal/heading-header/heading-header.component';

@Component({
    selector: 'lib-gesuch-page-heading',
    imports: [HeadingHeaderComponent],
    templateUrl: './gesuch-page-heading.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchPageHeadingComponent {}
