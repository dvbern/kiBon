import {
    ChangeDetectionStrategy,
    Component,
    inject,
    SecurityContext
} from '@angular/core';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle
} from '@angular/material/dialog';
import {MatDivider} from '@angular/material/list';
import {DomSanitizer} from '@angular/platform-browser';
import {SharedModule} from '../../../../app/shared/shared.module';
import {TableUebersichtVersendeteMails} from '../uebersichtVersendeteMails.component';

@Component({
    selector: 'dv-versendete-mail-detail',
    imports: [
        MatDivider,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        SharedModule
    ],
    templateUrl: './versendete-mail-detail.html',
    styleUrls: ['./versendete-mail-detail.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VersendeteMailDetail {
    data = inject<TableUebersichtVersendeteMails>(MAT_DIALOG_DATA);
    sanitizer = inject(DomSanitizer);

    sanitize(text: string): string {
        return this.sanitizer.sanitize(SecurityContext.HTML, text);
    }
}
