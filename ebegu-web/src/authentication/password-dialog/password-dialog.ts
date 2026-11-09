/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
    MatDialogActions,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle
} from '@angular/material/dialog';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'kib-password-dialog',
    imports: [
        MatLabel,
        MatFormField,
        MatDialogContent,
        MatDialogActions,
        MatInput,
        FormsModule,
        MatDialogTitle,
        TranslatePipe
    ],
    templateUrl: './password-dialog.html',
    styleUrls: ['./password-dialog.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PasswordDialog {
    password = '';
    constructor(private dialogRef: MatDialogRef<PasswordDialog>) {}

    submit() {
        this.dialogRef.close(this.password);
    }
    cancel() {
        this.dialogRef.close(null);
    }
}
