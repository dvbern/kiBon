/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    Component,
    ViewChild,
    inject,
    ChangeDetectionStrategy
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TSInternePendenz} from '../../../../models/TSInternePendenz';
import moment from 'moment';

@Component({
    selector: 'interne-pendenz-dialog',
    templateUrl: './interne-pendenz-dialog.template.html',
    styleUrls: ['./interne-pendenz-dialog.component.less'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class InternePendenzDialogComponent {
    private readonly dialogRef =
        inject<MatDialogRef<InternePendenzDialogComponent>>(MatDialogRef);
    private readonly data = inject(MAT_DIALOG_DATA);

    @ViewChild(NgForm, {static: true}) public form: NgForm;

    public internePendenz: TSInternePendenz;
    public readonlyMode = false;

    public constructor() {
        const data = this.data;

        this.internePendenz = data.internePendenz;
        this.readonlyMode = !this.isNew();
    }

    public close(): void {
        this.dialogRef.close();
    }

    public save(): void {
        if (this.form.valid) {
            this.dialogRef.close(this.internePendenz);
        }
    }

    public getMinDateTermin(): moment.Moment {
        return moment();
    }

    public isNew(): boolean {
        return this.internePendenz.isNew();
    }
}
