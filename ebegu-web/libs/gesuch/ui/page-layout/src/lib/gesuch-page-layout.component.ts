import {ChangeDetectionStrategy, Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
    selector: 'lib-gesuch-page-layout',
    imports: [CommonModule],
    templateUrl: './gesuch-page-layout.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchPageLayoutComponent {}
