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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject,
    OnInit,
    QueryList,
    signal,
    ViewChildren
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {TSSozialdienstStammdaten} from '../../../models/sozialdienst/TSSozialdienstStammdaten';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '@models/constants';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';
import {UnterstuetzungdienstPermissionService} from '../../authorisation/permissions/unterstuetzungdienst/Unterstuetzungdienst.permission.service';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';

@Component({
    selector: 'dv-edit-sozialdienst',
    templateUrl: './edit-sozialdienst.component.html',
    styleUrls: ['./edit-sozialdienst.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class EditSozialdienstComponent implements OnInit {
    private readonly $transition$ = inject(Transition);
    private readonly $state = inject(StateService);
    private readonly sozialdienstRS = inject(SozialdienstRS);
    private readonly translate = inject(TranslateService);
    private readonly changeDetectorRef = inject(ChangeDetectorRef);
    private readonly unterstuetzungdienstPermissionService = inject(
        UnterstuetzungdienstPermissionService
    );

    public readonly CONSTANTS: any = CONSTANTS;

    @ViewChildren(NgForm) public forms: QueryList<NgForm>;

    public stammdaten = signal<TSSozialdienstStammdaten | null>(null);
    public sozialdienstId: string;
    public editMode: boolean;
    public ebeguUtil = EbeguUtil;

    public ngOnInit(): void {
        this.sozialdienstId = this.$transition$.params().sozialdienstId;
        if (!this.sozialdienstId) {
            return;
        }
        this.loadStammdaten();
        this.editMode = false;
    }

    public onSubmit(stammdaten: TSSozialdienstStammdaten): void {
        if (this.editMode) {
            if (this.isStammdatenEditable()) {
                this.persistStammdaten(stammdaten);
            } else {
                this.persistSozialdienstName(stammdaten.sozialdienst);
            }

            return;
        }
        this.editMode = true;
    }

    public navigateBack(): void {
        this.$state.go('sozialdienst.list');
    }

    public isStammdatenEditable(): boolean {
        return this.unterstuetzungdienstPermissionService.canEditStammdaten();
    }

    public isUnterstuetzungNameEditable(): boolean {
        return this.unterstuetzungdienstPermissionService.canEditName();
    }

    private loadStammdaten(): void {
        this.sozialdienstRS
            .getSozialdienstStammdaten(this.sozialdienstId)
            .subscribe(stammdaten => {
                this.stammdaten.set(stammdaten);
            });
    }

    private persistStammdaten(stammdaten: TSSozialdienstStammdaten): void {
        let valid = true;
        this.forms.forEach(form => {
            if (!form.valid) {
                valid = false;
            }
        });

        if (!valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.sozialdienstRS
            .saveSozialdienstStammdaten(stammdaten)
            .subscribe(savedStammdaten => {
                this.stammdaten.set(savedStammdaten);
                this.editMode = false;
                this.changeDetectorRef.markForCheck();
            });
    }

    private persistSozialdienstName(sozialdienst: TSSozialdienst) {
        let valid = true;
        this.forms.forEach(form => {
            if (!form.valid) {
                valid = false;
            }
        });

        if (!valid) {
            EbeguUtil.selectFirstInvalid();
            return;
        }
        this.sozialdienstRS
            .saveSozialdienstName(sozialdienst)
            .subscribe(updatedSozialdienst => {
                this.stammdaten.update(stammdaten => {
                    stammdaten.sozialdienst = updatedSozialdienst;
                    return stammdaten;
                });
                this.editMode = false;
                this.changeDetectorRef.markForCheck();
            });
    }

    public submitButtonLabel(): string {
        if (this.editMode) {
            return this.translate.instant('SOZIALDIENST_SPEICHERN');
        }
        return this.translate.instant('SOZIALDIENST_EDIT');
    }
}
