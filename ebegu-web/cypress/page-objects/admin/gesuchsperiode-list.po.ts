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

const getCreateGesuchsperiodeButton = () => {
    return cy.getByData(
        'container.gesuchsperiode-hinzufuegen',
        'navigation-button'
    );
};

// Rows are sorted by gueltigAb desc, so the newest Gesuchsperiode is at the top.
const getFirstRowStatusCell = () => {
    return cy.get('mat-row').first().find('mat-cell').eq(2);
};

export const GesuchsperiodeListPO = {
    getCreateGesuchsperiodeButton,
    getFirstRowStatusCell
};
