/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {Component, inject, ChangeDetectionStrategy} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {CONSTANTS} from '@models/constants';
import {GesuchstellerRS} from '../../../../../app/core/service/gesuchstellerRS.rest';
import {TSSprache} from '../../../../../models/enums/TSSprache';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';

@Component({
    selector: 'dv-stek-identifier-verknuepfen-dialog',
    templateUrl: './dialog-init-stek-identifier-verknpuefen.template.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class DialogInitStekNummerVerknuepfenComponent {
    private readonly dialogRef =
        inject<MatDialogRef<DialogInitStekNummerVerknuepfenComponent>>(
            MatDialogRef
        );
    private readonly gesuchstellerRS = inject(GesuchstellerRS);
    readonly data = inject<InitStekIdentifierDialogData>(MAT_DIALOG_DATA);

    readonly gs = this.data.gs;
    readonly korrespondenzSprache = this.data.korrespondenzSprache;

    public email: string;
    public readonly CONSTANTS = CONSTANTS;

    public constructor() {
        const data = this.data;

        this.gs = data.gs;
        this.korrespondenzSprache = data.korrespondenzSprache;
    }

    public save(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        this.gesuchstellerRS
            .initGS2ZPVNr(this.email, this.gs, this.korrespondenzSprache)
            .then(() => this.dialogRef.close());
    }

    public close(): void {
        this.dialogRef.close();
    }
}

export type InitStekIdentifierDialogData = {
    gs: TSGesuchstellerContainer;
    korrespondenzSprache: TSSprache;
    gesuch: TSGesuch;
};
