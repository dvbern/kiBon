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

import {ChangeDetectionStrategy, Component, input} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';

/**
 * Display the given value and label to display changes when needed.
 * The id is required to ensure the accessibility!
 *
 * <input  [attr.aria-describedby]="
 *         showBisher() ? 'erwerbspensum-bisher' : null
 *     ">
 *     ....
 * </input>
 * @if (showBisher()) {
 *     <kib-bisher id="erwerbspensum-bisher">
 *         <span kibBisherValue>
 *             {{ kibBisherValue }} %
 *         </span>
 *     </kib-bisher>
 * }
 *
 * We can specify the kibBisherLabel part aswell when it differ from the default text:
 *
 * <kib-bisher id="erwerbspensum-bisher">
 *      <span kibBisherLabel>
 *          {{ 'LATS_BISHER_LABEL' | translate }}
 *      </span>
 *      <span kibBisherValue>
 *          {{ kibBisherValue }} %
 *      </span>
 * </kib-bisher>
 *
 */
@Component({
    selector: 'kib-bisher',
    imports: [TranslatePipe],
    templateUrl: './kib-bisher.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KibBisherComponent {
    /**
     * Unique ID used to reference the Bisher information from the
     * associated form control via aria-describedby.
     */
    public readonly id = input.required<string>();
}
