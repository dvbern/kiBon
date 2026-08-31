import {ChangeDetectionStrategy, Component} from '@angular/core';
import {HeadingHeaderComponent} from '../internal/heading-header/heading-header.component';

@Component({
    selector: 'lib-gesuch-popup-heading',
    imports: [HeadingHeaderComponent],
    templateUrl: './gesuch-popup-heading.component.html',
    styleUrl: './gesuch-popup-heading.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchPopupHeadingComponent {}
