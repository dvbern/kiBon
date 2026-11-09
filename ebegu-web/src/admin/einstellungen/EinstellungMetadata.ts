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

import {GroupableConfiguration} from '../../models/einstellung/GroupableConfiguration';

export class EinstellungMetadata implements GroupableConfiguration {
    private _keyGroup: string;
    private _keyGroupOrder: number;
    private _keyOrder: number;

    public get keyGroup(): string {
        return this._keyGroup;
    }

    public set keyGroup(value: string) {
        this._keyGroup = value;
    }

    public set keyGroupOrder(value: number) {
        this._keyGroupOrder = value;
    }

    public get keyOrder(): number {
        return this._keyOrder;
    }

    public set keyOrder(value: number) {
        this._keyOrder = value;
    }

    public get keyGroupOrder(): number {
        return this._keyGroupOrder;
    }
}
