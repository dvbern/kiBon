import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
    inject
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-steuerveranlagung-erhalten',
    templateUrl: './steuerveranlagung-erhalten.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    standalone: false
})
export class SteuerveranlagungErhaltenComponent {
    gesuchModelManager = inject(GesuchModelManager);

    @Input() public model: TSFinanzielleSituationContainer;

    @Output()
    public readonly steuerveranlagungErhaltenChange: EventEmitter<boolean> =
        new EventEmitter<boolean>();

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }
    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public setSteuerveranlagungErhalten(value: any): void {
        this.model.finanzielleSituationJA.steuerveranlagungErhalten = value;
        this.steuerveranlagungErhaltenChange.emit(value);
    }

    public showVeranlagungErhalten(): boolean {
        return EbeguUtil.isNotNullOrUndefined(
            this.model.finanzielleSituationJA.momentanSelbststaendig
        );
    }
}
