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

import {
    GesuchsperiodeEditPO,
    GesuchsperiodeListPO,
    RemoveDialogPO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {KiBonMandant, MANDANTS} from '@models/mandant';

const isPeriodeEroeffnenAllowedOn = (baseUrl: string | null | undefined) => {
    const url = baseUrl ?? '';
    return url.includes('preview') || url.includes('local');
};

describe('kiBon - Gesuchsperiode eröffnen - baseUrl guard', () => {
    it('runs on local (default cypress.config baseUrl)', () => {
        expect(isPeriodeEroeffnenAllowedOn('https://local-be.kibon.ch:4200/'))
            .to.be.true;
    });

    it('runs on preview (mandant subdomain)', () => {
        expect(isPeriodeEroeffnenAllowedOn('https://preview-be.kibon.ch')).to.be
            .true;
    });

    /**
     * skip test on uat (if mistankenly trying to run on that eonvironment)
     */
    it('skips on uat', () => {
        expect(isPeriodeEroeffnenAllowedOn('https://uat-be.kibon.ch')).to.be
            .false;
    });

    /**
     * skip test on uat (if mistankenly trying to run on that eonvironment)
     */
    it('skips on prod', () => {
        expect(isPeriodeEroeffnenAllowedOn('https://kibon.ch')).to.be.false;
    });
});

describe('kiBon - Gesuchsperiode eröffnen', () => {
    const superAdmin = getUser('[1-Superadmin] Super User');

    // this test is only allowed to run on local and preview environments
    before(function () {
        if (!isPeriodeEroeffnenAllowedOn(Cypress.config('baseUrl'))) {
            this.skip();
        }
    });

    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    const runPeriodeEroeffnenperMandant = (mandant: KiBonMandant) => {
        cy.changeMandant(mandant);
        cy.login(superAdmin);
        cy.visit('/#/gesuchsperioden');

        GesuchsperiodeListPO.getCreateGesuchsperiodeButton().click();

        // open the new Gesuchsperiode with status ENTWURF
        cy.waitForRequest('PUT', '**/gesuchsperioden', () => {
            GesuchsperiodeEditPO.getSaveButton().click();
            RemoveDialogPO.getRemoveOkButton().click();
        });

        // switch status to AKTIV
        GesuchsperiodeEditPO.getStatusSelect().select('Aktiv');
        cy.waitForRequest('PUT', '**/gesuchsperioden', () => {
            GesuchsperiodeEditPO.getSaveButton().click();
            RemoveDialogPO.getRemoveOkButton().click();
        });

        GesuchsperiodeEditPO.getCancelLink().click();

        GesuchsperiodeListPO.getFirstRowStatusCell().should(
            'contain.text',
            'Aktiv'
        );
    };

    it('Bern - should open a new Gesuchsperiode and activate it', () => {
        runPeriodeEroeffnenperMandant(MANDANTS.BERN);
    });

    it('Appenzell - should open a new Gesuchsperiode and activate it', () => {
        runPeriodeEroeffnenperMandant(MANDANTS.APPENZELL_AUSSERRHODEN);
    });

    it('Solothurn - should open a new Gesuchsperiode and activate it', () => {
        runPeriodeEroeffnenperMandant(MANDANTS.SOLOTHURN);
    });

    it('Schwyz - should open a new Gesuchsperiode and activate it', () => {
        runPeriodeEroeffnenperMandant(MANDANTS.SCHWYZ);
    });

    it('Standard - should open a new Gesuchsperiode and activate it', () => {
        runPeriodeEroeffnenperMandant(MANDANTS.DVB);
    });
});
