/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {Component, inject, viewChild} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {TSSupportAnfrage} from '../../../models/TSSupportAnfrage';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {SupportRS} from '../../service/supportRS.rest';
import {NgForm} from '@angular/forms';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {from} from 'rxjs';

/**
 * This component shows a dialog to send a request of support
 */
@Component({
    selector: 'dv-ng-support-dialog',
    templateUrl: './dv-ng-support-dialog.template.html',
    styleUrls: ['./dv-ng-support-dialog.component.less'],
    standalone: false
})
export class DvNgSupportDialogComponent {
    private readonly dialogRef =
        inject<MatDialogRef<DvNgSupportDialogComponent>>(MatDialogRef);
    private readonly supportRS = inject(SupportRS);

    public beschreibung: string = '';
    public betroffeneFaelle: string;
    public betroffenePeriode: string;
    public readonly idLength = 20;
    public anfrageForm = viewChild<NgForm>('anfrageForm');
    public gesuchsPeriodenService = inject(GesuchsperiodeRS);
    public allActiveGesuchsperioden$ = from(
        this.gesuchsPeriodenService.getAllActiveGesuchsperioden()
    );

    public send(): void {
        if (this.anfrageForm().invalid) {
            return;
        }
        const anfrage = new TSSupportAnfrage();
        anfrage.id = EbeguUtil.generateRandomName(this.idLength);
        anfrage.beschreibung = this.beschreibung;
        anfrage.betroffeneFaelle = this.betroffeneFaelle;
        anfrage.betroffenePeriode = this.betroffenePeriode;
        this.dialogRef.close();
        this.supportRS.sendSupportAnfrage(anfrage);
    }

    public cancel(): void {
        this.dialogRef.close();
    }
}
