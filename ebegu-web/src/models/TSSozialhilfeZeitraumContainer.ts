/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {TSAbstractMutableEntity} from './entity/TSAbstractMutableEntity';
import {TSSozialhilfeZeitraum} from './TSSozialhilfeZeitraum';

export class TSSozialhilfeZeitraumContainer extends TSAbstractMutableEntity {
    private _sozialhilfeZeitraumGS: TSSozialhilfeZeitraum;
    private _sozialhilfeZeitraumJA: TSSozialhilfeZeitraum;

    public constructor(
        sozialhilfeZeitraumGS?: TSSozialhilfeZeitraum,
        sozialhilfeZeitraumJA?: TSSozialhilfeZeitraum
    ) {
        super();
        this._sozialhilfeZeitraumGS = sozialhilfeZeitraumGS;
        this._sozialhilfeZeitraumJA = sozialhilfeZeitraumJA;
    }

    public get sozialhilfeZeitraumGS(): TSSozialhilfeZeitraum {
        return this._sozialhilfeZeitraumGS;
    }

    public set sozialhilfeZeitraumGS(value: TSSozialhilfeZeitraum) {
        this._sozialhilfeZeitraumGS = value;
    }

    public get sozialhilfeZeitraumJA(): TSSozialhilfeZeitraum {
        return this._sozialhilfeZeitraumJA;
    }

    public set sozialhilfeZeitraumJA(value: TSSozialhilfeZeitraum) {
        this._sozialhilfeZeitraumJA = value;
    }

    public isGSContainerEmpty(): boolean {
        return (
            this.sozialhilfeZeitraumGS === null ||
            this.sozialhilfeZeitraumGS === undefined
        );
    }

    public deepCopyTo(
        target: TSSozialhilfeZeitraumContainer
    ): TSSozialhilfeZeitraumContainer {
        super.deepCopyTo(target);
        if (
            this._sozialhilfeZeitraumGS !== null &&
            this._sozialhilfeZeitraumGS !== undefined
        ) {
            target._sozialhilfeZeitraumGS =
                this._sozialhilfeZeitraumGS.deepCopyTo(
                    new TSSozialhilfeZeitraum()
                );
        }
        if (
            this._sozialhilfeZeitraumJA !== null &&
            this._sozialhilfeZeitraumJA !== undefined
        ) {
            target._sozialhilfeZeitraumJA =
                this._sozialhilfeZeitraumJA.deepCopyTo(
                    new TSSozialhilfeZeitraum()
                );
        }
        return target;
    }
}
