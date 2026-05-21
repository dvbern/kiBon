import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {HoehereBeitraegeTyp} from '../../../../../models/enums/HoehereBeitraegeTyp';
import {TSBetreuungsangebotTyp} from '../../../../../models/enums/TSBetreuungsangebotTyp';
import {TSErweiterteBetreuung} from '../../../../../models/TSErweiterteBetreuung';
import {
    ErweiterteBeduerfnisseBestaetigenEinstellungen,
    ErweiterteBeduerfnisseBestaetigungComponent
} from '../erweiterte-beduerfnisse-bestaetigung.component';

/**
 * This component is a pass-through from angularjs to angular. It exists
 * because angularjs cannot handle Signals and would input the actual value
 * instead of a signal containing the value. Using a wrapper with @Input solves
 * this issue
 */
@Component({
    selector: 'dv-erweiterte-beduerfnisse-bestaetigung-wrapper',
    imports: [ErweiterteBeduerfnisseBestaetigungComponent],
    templateUrl:
        './erweiterte-beduerfnisse-bestaetigung-wrapper.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErweiterteBeduerfnisseBestaetigungWrapperComponent {
    @Input()
    erweiterteBetreuungJA: TSErweiterteBetreuung;
    @Input()
    einstellungen: ErweiterteBeduerfnisseBestaetigenEinstellungen;
    @Input()
    angebotTyp: TSBetreuungsangebotTyp;
    @Input()
    readOnly: boolean;
    @Input()
    hoehereBeitraegeTyp: HoehereBeitraegeTyp;
}
