import {
    ChangeDetectionStrategy,
    Component,
    Input,
    ViewEncapsulation
} from '@angular/core';

@Component({
    selector: 'dv-radio-container-x',
    templateUrl: './dv-radio-container-x.component.html',
    styleUrls: ['./dv-radio-container-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class DvRadioContainerXComponent {
    @Input() horizontal: boolean = false;
}
