/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle
} from '@angular/material/dialog';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../shared/shared.module';
import {TableAuswahlInstitution} from '../edit-gemeinde-institution.component';

@Component({
    selector: 'dv-edit-infoma-dialog',
    imports: [
        MatDialogActions,
        MatDialogTitle,
        TranslateModule,
        FormsModule,
        MatDialogContent,
        ReactiveFormsModule,
        SharedModule
    ],
    templateUrl: './edit-infoma-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditInfomaDialogComponent {
    private readonly dialogRef =
        inject<MatDialogRef<EditInfomaDialogComponent>>(MatDialogRef);
    private readonly data = inject<TableAuswahlInstitution>(MAT_DIALOG_DATA);

    public infomaData: TableAuswahlInstitution;

    public constructor() {
        const data = this.data;

        if (data) {
            this.infomaData = data;
        }
    }

    public close(): void {
        this.dialogRef.close(this.infomaData);
    }

    public save() {
        this.close();
    }
}
