import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogRef,
    MatDialogTitle
} from '@angular/material/dialog';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {TSGesuchsperiode} from '../../../models/entity/TSGesuchsperiode';
import {EbeguUtil} from '../../../utils/EbeguUtil';

export interface GesuchUiMutationDialogData {
    periode: TSGesuchsperiode;
}

@Component({
    selector: 'lib-gesuch-ui-mutation-dialog',
    imports: [MatDialogActions, MatDialogTitle, TranslateModule],
    templateUrl: './mutation-dialog.component.html',
    styleUrl: './mutation-dialog.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MutationDialogComponent {
    private readonly $translate = inject(TranslateService);
    private readonly dialogRef =
        inject<MatDialogRef<MutationDialogComponent>>(MatDialogRef);
    private readonly data = inject<GesuchUiMutationDialogData>(MAT_DIALOG_DATA);

    public readonly text: string;

    public constructor() {
        const data = this.data;

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
