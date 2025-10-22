import {ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogRef,
    MatDialogTitle
} from '@angular/material/dialog';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {TSGesuchsperiode} from '@kibon/shared/model/entity';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {EbeguUtil} from '../../../../../../../src/utils/EbeguUtil';

export interface GesuchUiMutationDialogData {
    periode: TSGesuchsperiode;
}

@Component({
    selector: 'lib-gesuch-ui-mutation-dialog',
    imports: [CommonModule, MatDialogActions, MatDialogTitle, TranslateModule],
    templateUrl: './gesuch-ui-mutation-dialog.component.html',
    styleUrl: './gesuch-ui-mutation-dialog.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchUiMutationDialogComponent {
    public readonly text: string;

    public constructor(
        private readonly $translate: TranslateService,
        private readonly dialogRef: MatDialogRef<GesuchUiMutationDialogComponent>,
        @Inject(MAT_DIALOG_DATA)
        private readonly data: GesuchUiMutationDialogData
    ) {
        if (
            EbeguUtil.isNullOrUndefined(this.data) ||
            EbeguUtil.isNullOrUndefined(data.periode)
        ) {
            throw new Error('Wrong Dialog configuration');
        }

        this.text = this.$translate.instant(
            'MUTATION_VERGANGENE_PERIODE_FRAGE',
            {
                periode: this.data.periode.gesuchsperiodeString
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
