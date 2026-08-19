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

import {getUser} from '@dv-e2e/types';
import {MANDANTS} from '@models/mandant';
import {QuicksearchPO} from '../../page-objects/admin/quicksearch.po';

describe('Erstellen einen Testfaelle und suchen der Antragstellende Name in der globale Suche', () => {
    const userSuperadmin = getUser('[1-Superadmin] Super User');

    before(() => {
        cy.changeMandant(MANDANTS.BERN);
        cy.login(userSuperadmin);
        cy.visit('/#/');
    });

    it('should be possible to get an answer after starting a quick search', () => {
        cy.intercept('GET', '**/api/v1/search/quicksearch/Feutz').as(
            'quicksearchCall'
        );

        QuicksearchPO.getQuicksearch().type('Feutz');

        cy.wait('@quicksearchCall')
            .its('response.statusCode')
            .should('eq', 200);
    });
});
