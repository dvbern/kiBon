import {
    ChangeDetectionStrategy,
    Component,
    inject,
    output
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {GesuchDokumenteUebernehmenSelectionComponent} from '@kibon/gesuch-dokumente-uebernehmen-selection';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'lib-gesuch-open-dokumente-uebernehmen-button',
    imports: [CommonModule, SharedModule],
    templateUrl: './gesuch-open-dokumente-uebernehmen-button.component.html',
    styleUrl: './gesuch-open-dokumente-uebernehmen-button.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchOpenDokumenteUebernehmenButtonComponent {
    readonly dokumenteUebernehmen = output<void>();

    private readonly dialog = inject(MatDialog);
    openDialog() {
        this.dialog
            .open(GesuchDokumenteUebernehmenSelectionComponent)
            .afterClosed()
            .subscribe(dokumenteErneuert => {
                if (dokumenteErneuert) {
                    this.dokumenteUebernehmen.emit();
                }
            });
    }
}
