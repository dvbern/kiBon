import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {
    MeldungsfensterData,
    MeldungsfensterStatus
} from '@kibon/shared-model-meldungsfenster';
import {TSSprache} from '@kibon/shared/model/enums';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';

@Component({
    selector: 'lib-shared-ui-meldungsfenster',
    imports: [SharedModule],
    templateUrl: './shared-ui-meldungsfenster.component.html',
    styleUrl: './shared-ui-meldungsfenster.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SharedUiMeldungsfensterComponent {
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
