import {
    ChangeDetectionStrategy,
    Component,
    inject,
    output
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {GesuchDokumenteErneuernSelectionComponent} from '@kibon/gesuch-dokumente-erneuern-selection';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'lib-gesuch-open-dokumente-erneuern-button',
    imports: [CommonModule, SharedModule],
    templateUrl: './gesuch-open-dokumente-erneuern-button.component.html',
    styleUrl: './gesuch-open-dokumente-erneuern-button.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchOpenDokumenteErneuernButtonComponent {
    readonly dokumenteErneuert = output<void>();

    private readonly dialog = inject(MatDialog);
    openDialog() {
        this.dialog
            .open(GesuchDokumenteErneuernSelectionComponent)
            .afterClosed()
            .subscribe(dokumenteErneuert => {
                if (dokumenteErneuert) {
                    this.dokumenteErneuert.emit();
                }
            });
    }
}
