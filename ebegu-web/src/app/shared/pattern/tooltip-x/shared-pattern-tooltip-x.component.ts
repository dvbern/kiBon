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

import {Component, inject, input} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslatePipe} from '@ngx-translate/core';
import {TooltipTemplateComponent} from './tooltip-template.component';

@Component({
    selector: 'lib-tooltip-x',
    templateUrl: 'shared-pattern-tooltip-x.component.html',
    styles: `
        .tooltip-spacing {
            margin-left: 0.5rem;
        }
    `,
    imports: [TranslatePipe],
    standalone: true
})
export class SharedPatternTooltipXComponent {
    text = input.required<string>();

    private readonly dialog = inject(MatDialog);

    open() {
        this.dialog.open(TooltipTemplateComponent, {
            height: '100vh',
            width: '100vw',
            maxWidth: '100vw',
            panelClass: 'dv-tooltip-dialog',
            data: {
                text: this.text()
            }
        });
    }

    isTextPresent(text: string) {
        return text?.length > 0;
    }
}
