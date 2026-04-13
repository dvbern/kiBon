/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {AbstractMandantDefaultVisitor, KiBonMandant} from '@models/mandant';
import {
    getTSEinschulungTypGemeindeValues,
    getTSEinschulungTypValuesSchwyz,
    getTSEinschulungTypValuesZug,
    TSEinschulungTyp
} from '../../../models/enums/TSEinschulungTyp';

export class EinschulungTypesGemeindeVisitor extends AbstractMandantDefaultVisitor<
    ReadonlyArray<TSEinschulungTyp>
> {
    public process(mandant: KiBonMandant): ReadonlyArray<TSEinschulungTyp> {
        return mandant.accept(this);
    }

    protected visitDefault(): ReadonlyArray<TSEinschulungTyp> {
        return getTSEinschulungTypGemeindeValues();
    }

    public visitLuzern(): ReadonlyArray<TSEinschulungTyp> {
        return [
            TSEinschulungTyp.VORSCHULALTER,
            TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN,
            TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN
        ];
    }

    public visitAppenzellAusserrhoden(): ReadonlyArray<TSEinschulungTyp> {
        return [TSEinschulungTyp.VORSCHULALTER, TSEinschulungTyp.KINDERGARTEN1];
    }

    public visitSolothurn(): ReadonlyArray<TSEinschulungTyp> {
        return getTSEinschulungTypGemeindeValues();
    }

    public visitSchwyz(): ReadonlyArray<TSEinschulungTyp> {
        return getTSEinschulungTypValuesSchwyz();
    }

    public visitZug(): ReadonlyArray<TSEinschulungTyp> {
        return getTSEinschulungTypValuesZug();
    }
}
