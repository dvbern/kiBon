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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ConfirmDialogPO,
    SidenavPO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';

const betreuungVerfuegen = (kindIndex: number, betreuungIndex: number) => {
    SidenavPO.goTo('VERFUEGEN');
    cy.waitForRequest(
        'GET',
        '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**',
        () => {
            VerfuegenPO.getVerfuegung(kindIndex, betreuungIndex).click();
        }
    );
    cy.wait(1000);
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        cy.wait(1000);
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
        cy.wait(1000);
    });
};

export const GesuchPO = {
    betreuungVerfuegen
};
