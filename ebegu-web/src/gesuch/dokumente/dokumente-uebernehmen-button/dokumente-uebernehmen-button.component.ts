import {
    ChangeDetectionStrategy,
    Component,
    inject,
    output
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../app/shared/shared.module';
import {MatDialog} from '@angular/material/dialog';
import {DokumenteUebernehmenSelectionComponent} from '@gesuch/dokumente';

@Component({
    selector: 'lib-gesuch-open-dokumente-uebernehmen-button',
    imports: [CommonModule, SharedModule],
    templateUrl: './dokumente-uebernehmen-button.component.html',
    styleUrl: './dokumente-uebernehmen-button.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DokumenteUebernehmenButtonComponent {
    readonly dokumenteUebernehmen = output<void>();

    private readonly dialog = inject(MatDialog);
    openDialog() {
        this.dialog
            .open(DokumenteUebernehmenSelectionComponent)
            .afterClosed()
            .subscribe(dokumenteErneuert => {
                if (dokumenteErneuert) {
                    this.dokumenteUebernehmen.emit();
                }
            });
    }
}
