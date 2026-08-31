import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TSEWKAdresse} from '../../models/TSEWKAdresse';

@Component({
    selector: 'dv-ewk-adresse',
    templateUrl: './ewk-adresse.component.html',
    styleUrls: ['./ewk-adresse.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class EwkAdresseComponent {
    @Input()
    public adresse: TSEWKAdresse;
}
