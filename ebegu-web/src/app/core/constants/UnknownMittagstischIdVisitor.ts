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

import {
    KiBonMandant,
    AbstractMandantDefaultVisitor
} from '@kibon/shared-model-mandant';

export class UnknownMittagstischIdVisitor extends AbstractMandantDefaultVisitor<string> {
    public process(mandant: KiBonMandant): string {
        return mandant.accept(this);
    }

    protected visitDefault(): string {
        throw new Error('This Mandant has no Mittagstisch.');
    }

    public visitSchwyz(): string {
        return '00000000-0000-0000-0000-000000000015';
    }
}
