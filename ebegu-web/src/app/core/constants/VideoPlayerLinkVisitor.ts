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

import {AbstractMandantDefaultVisitor, KiBonMandant} from '@models/mandant';

export class VideoPlayerLinkVisitor extends AbstractMandantDefaultVisitor<
    string | null
> {
    private readonly _isGerman: boolean;

    public constructor(isGerman: boolean) {
        super();
        this._isGerman = isGerman;
    }

    public process(mandant: KiBonMandant): string | null {
        return mandant.accept(this);
    }

    protected visitDefault(): string {
        return null;
    }

    public visitBern(): string {
        if (this._isGerman) {
            return '/videos/kiBon_video_Eltern_BE_DE.mp4';
        }
        return '/videos/kiBon_video_Eltern_BE_FR.mp4';
    }

    public visitAppenzellAusserrhoden(): string {
        return this.visitDefault();
    }

    public visitLuzern(): string {
        return this.visitDefault();
    }

    public visitSolothurn(): string {
        return this.visitDefault();
    }
}
