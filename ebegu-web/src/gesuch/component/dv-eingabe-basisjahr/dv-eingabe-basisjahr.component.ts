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

import {
    ChangeDetectionStrategy,
    Component,
    Input,
} from '@angular/core';

/**
 * Zeigt einen Hinweis, wie hoch die Eingabe im Basisjahr war.
 */
@Component({
    selector: 'dv-eingaben-basisjahr',
    templateUrl: './dv-eingabe-basisjahr.component.html',
    styleUrls: ['./dv-eingabe-basisjahr.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DvEingabeBasisjahrComponent {
    @Input() public neuerWert: number;
    @Input() public wertBasisjahr: number;
    @Input() public basisjahr: number;

    public isVisible(): boolean {
        return this.wertBasisjahr > 0 ||
            (this.wertBasisjahr !== this.neuerWert && this.neuerWert > 0);
    }
}
