/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {Component, inject} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {MANDANTS} from '@kibon/shared-model-mandant';
import {KiBonGuidedTourService} from '../../../app/kibonTour/service/KiBonGuidedTourService';
import {SupportDialogService} from '../../../app/shared/services/support-dialog.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {MandantService} from '@kibon/shared-util-mandant-service';

/**
 * This component shows a Help Dialog with all contact details and a Link to the user manual
 */
@Component({
    selector: 'dv-ng-help-dialog',
    styleUrls: ['./dv-ng-help-dialog.component.less'],
    templateUrl: './dv-ng-help-dialog.component.html',
    standalone: false
})
export class DvNgHelpDialogComponent {
    private readonly dialogRef =
        inject<MatDialogRef<DvNgHelpDialogComponent>>(MatDialogRef);
    private readonly dialogSupport = inject(MatDialog);
    private readonly kibonGuidedTourService = inject(KiBonGuidedTourService);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly supportDialogService = inject(SupportDialogService);
    private readonly mandantService = inject(MandantService);

    public hasRoleGemeinde: boolean = false;
    public hasRoleInstitution: boolean = false;
    public mandant$: Observable<MANDANTS>;
    public mandantTypes = MANDANTS;

    public constructor() {
        this.hasRoleGemeinde = this.isGemeinde();
        this.hasRoleInstitution = this.isInstitution();
        this.mandant$ = this.mandantService.mandant$;
    }

    public close(): void {
        this.dialogRef.close();
    }

    public openSupportanfrage(): void {
        this.close();
        this.dialogRef.afterClosed().subscribe(
            () => this.supportDialogService.openDialog(),
            error => console.error(error)
        );
    }

    public startTour(): void {
        this.close();
        this.kibonGuidedTourService.emit();
    }

    private isGemeinde(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeRoles().concat(TSRoleUtil.getMandantRoles())
        );
    }

    private isInstitution(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getInstitutionRoles().concat(
                TSRoleUtil.getMandantRoles()
            )
        );
    }
}
