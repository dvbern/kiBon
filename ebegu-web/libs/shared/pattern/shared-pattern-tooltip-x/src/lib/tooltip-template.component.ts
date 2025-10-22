/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';

@Component({
    imports: [CommonModule, SharedModule],
    templateUrl: 'tooltip-template.component.html',
    styles: `
        .ugly-button-row {
            align-items: end;
            display: flex;
            flex-direction: column;
            & > * {
                max-width: 1rem;
                padding-right: 2rem;
            }
        }
        .tooltip-panel {
            align-items: center;
            display: flex;
            flex-direction: column;
            height: 100%;
            padding: 5rem;

            & > * {
                flex-grow: 0;
            }

            & > .text-content {
                flex-grow: 1;
                width: 100%;
            }
        }
        .tooltip-wrapper {
            background-color: #e5e5e5;
            height: 100%;
            display: flex;
            flex-direction: column;
            & > * {
                width: 100%;
            }
        }
    `
})
export class TooltipTemplateComponent {
    readonly dialogData = inject<{text: string}>(MAT_DIALOG_DATA);
    private readonly dialogRef = inject(MatDialogRef);

    close() {
        this.dialogRef.close();
    }
}
