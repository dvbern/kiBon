import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

export interface GSRemovalConfirmationDialogData {
    gsFullName: string;
}

@Component({
    selector: 'dv-dv-ng-gs-removal-confirmation-dialog',
    templateUrl: './dv-ng-gs-removal-confirmation-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class DvNgGsRemovalConfirmationDialogComponent {
    private readonly $translate = inject(TranslateService);
    private readonly dialogRef =
        inject<MatDialogRef<DvNgGsRemovalConfirmationDialogComponent>>(
            MatDialogRef
        );
    private readonly data =
        inject<GSRemovalConfirmationDialogData>(MAT_DIALOG_DATA);

    public readonly text: string;

    public constructor() {
        const data = this.data;

        if (
            EbeguUtil.isNullOrUndefined(this.data) ||
            EbeguUtil.isNullOrUndefined(data.gsFullName)
        ) {
            throw new Error('Wrong Dialog configuration');
        }

        this.text = this.$translate.instant(
            'FAMILIENSITUATION_WARNING_BESCHREIBUNG',
            {
                gsfullname: this.data.gsFullName
            }
        );
    }

    public ok(): void {
        this.dialogRef.close(true);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
