import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '../../../models/meldungsfenster';
import {TSSprache} from '../../../models/enums/TSSprache';

@Component({
    selector: 'lib-shared-ui-meldungsfenster',
    templateUrl: './ui-meldungsfenster.component.html',
    styleUrl: './ui-meldungsfenster.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class UiMeldungsfensterComponent {
    meldungsfenster = input.required<MeldungsfensterData>();
    maxCount = input.required<number>();
    currentLanguage = input.required<TSSprache>();
    meldungsfensterClosed = output<MeldungsfensterData>();

    public closeMeldungsfenster(meldung: MeldungsfensterData) {
        this.meldungsfensterClosed.emit(meldung);
    }

    protected readonly TSSprache = TSSprache;
    protected readonly MeldungsfensterStatus = MeldungsfensterStatus;
}
