import {ChangeDetectionStrategy, Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
    selector: 'lib-gesuch-page-heading',
    imports: [CommonModule],
    templateUrl: './gesuch-page-heading.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchPageHeadingComponent {}
