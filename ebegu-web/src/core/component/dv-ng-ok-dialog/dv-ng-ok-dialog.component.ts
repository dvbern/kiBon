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

import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

/**
 * This component shows a Dialog with a title and an OK-Button. Nothing is returned and nothing is executed
 */
@Component({
    selector: 'dv-ng-ok-dialog',
    template: require('./dv-ng-ok-dialog.template.html'),
})
export class DvNgOkDialogComponent {

    title: string = '';

    constructor(
        private readonly dialogRef: MatDialogRef<DvNgOkDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: any) {

        if (data) {
            this.title = data.title;
        }
    }

    close() {
        this.dialogRef.close();
    }
}
