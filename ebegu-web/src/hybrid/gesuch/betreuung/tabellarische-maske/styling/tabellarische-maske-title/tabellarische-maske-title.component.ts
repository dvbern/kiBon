import {Component, ChangeDetectionStrategy} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';

@Component({
    selector: 'lib-tabellarische-maske-title',
    imports: [TranslateModule],
    templateUrl: './tabellarische-maske-title.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './tabellarische-maske-title.component.less'
})
export class TabellarischeMaskeTitleComponent {}
