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
 *
 */

import {
    ChangeDetectionStrategy,
    Component,
    TemplateRef,
    viewChild,
    inject
} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {KibButtonDirective} from '../button/kib-button-directive';

/**
 * Displays additional contextual information that can be shown when needed.
 *
 * <p>The component is designed for additional information,
 * which is helpful for understanding a field or a function, but should not be
 * permanently displayed on the page.</p>
 *
 * <p>The information in the tooltip can, for example, contain explanations,
 * examples or excerpts from legal provisions. This information can be relatively
 * extensive depending on the use case. Therefore, the tooltip is displayed as
 * a dialog and not as a classic small tooltip directly next to the triggering
 * element.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * <kib-tooltip>
 *     <p>Here you will find additional information about the field.</p>
 *     <p>For example, an excerpt from the corresponding legal
 *     basis.</p>
 * </kib-tooltip>
 * }
 * </pre>
 * <pre>
 * {@code
 *  <kib-tooltip>
 *             <div [innerHTML]="'TEXT_ELEMENT' | translate"></div>
 *         </kib-tooltip>
 *         }
 * </pre>
 *
 * <p>The tooltip should not be used for:</p>
 * <ul>
 *     <li>Error messages or validation messages</li>
 *     <li>Information that must be visible for processing</li>
 *    <li>Interactions or actions that require a user decision</li>
 * </ul>
 */
@Component({
    selector: 'kib-tooltip',
    imports: [TranslatePipe, KibButtonDirective],
    templateUrl: './kib-tooltip.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KibTooltip {
    private readonly dialog = inject(MatDialog);

    private readonly dialogTemplate =
        viewChild.required<TemplateRef<unknown>>('dialogTemplate');

    private dialogRef?: MatDialogRef<unknown>;

    open(): void {
        this.dialogRef = this.dialog.open(this.dialogTemplate(), {
            width: '100vw',
            height: '100vh',
            maxWidth: '100vw',
            maxHeight: '100vh',
            autoFocus: 'first-tabbable',
            restoreFocus: true
        });
    }
    close(): void {
        this.dialogRef?.close();
    }
}
