import {ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GSRemovalConfirmationDialogData} from '../dv-ng-gs-removal-confirmation-dialog/dv-ng-gs-removal-confirmation-dialog.component';

@Component({
    selector: 'dv-dv-ng-gs-removal-question-dialog',
    templateUrl: './dv-ng-gs-removal-question-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class DvNgGsRemovalQuestionDialogComponent {
    public readonly text: string;
    public readonly frage: string;

    public constructor(
        private readonly $translate: TranslateService,
        private readonly dialogRef: MatDialogRef<DvNgGsRemovalQuestionDialogComponent>,
        @Inject(MAT_DIALOG_DATA)
        private readonly data: GSRemovalConfirmationDialogData
    ) {
        if (
            EbeguUtil.isNullOrUndefined(this.data) ||
            EbeguUtil.isNullOrUndefined(data.gsFullName)
        ) {
            throw new Error('Wrong Dialog configuration');
        }

        this.frage = this.$translate.instant(
            'FAMILIENSITUATION_DELETE_GS2_QUESTION',
            {
                gsfullname: this.data.gsFullName
            }
        );

        this.text = this.$translate.instant(
            'FAMILIENSITUATION_DELETE_GS2_BESCHREIBUNG',
            {
                gsfullname: this.data.gsFullName
            }
        );
    }

    public ja(): void {
        this.dialogRef.close(true);
    }

    public nein(): void {
        this.dialogRef.close(false);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
