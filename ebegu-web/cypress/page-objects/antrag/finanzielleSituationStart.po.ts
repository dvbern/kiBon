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

// !! -- PAGE OBJECTS -- !!
import {
    FixtureFinSit,
    FixtureFinSitFeutz,
    FixtureFinSitFeutzLuzern,
    FixtureFinSitFeutzSolothurn
} from '@dv-e2e/fixtures';
import {NavigationPO} from './navigation.po';

const getSozialhilfebezueger = (answer: string) => {
    return cy.getByData('sozialhilfeBezueger.radio-value.' + answer);
};

const getSozialhilfebezuegerx = (answer: string) => {
    return cy.getByData('sozialhilfeBezueger', 'radio-value.' + answer);
};

const getIban = () => {
    return cy.getByData('iban');
};

const getKontoinhaberIn = () => {
    return cy.getByData('kontoinhaber');
};

const getSteuerveranlagungGemeinsam = (answer: string) => {
    return cy.getByData(
        'steuerveranlagungGemeinsam.radio-group',
        'radio-value.' + answer
    );
};

const fillFinanzielleSituationStartForm = (
    dataset: keyof typeof FixtureFinSit
) => {
    FixtureFinSit[dataset](({Start}) => {
        cy.wait(2000);
        getSozialhilfebezueger(Start.sozialhilfebeziehende).click();
        getIban().type(Start.iban);
        getKontoinhaberIn().type(Start.kontoinhaber);
    });
};

const fillFinanzielleSituationStartFormFeutz = (
    dataset: keyof typeof FixtureFinSitFeutz
) => {
    FixtureFinSitFeutz[dataset](({Start}) => {
        cy.wait(2000);
        getIban().type(Start.iban);
        getKontoinhaberIn().type(Start.kontoinhaber);
        getSteuerveranlagungGemeinsam(Start.steuerveranlagungGemeinsam)
            .find('input')
            .click();
    });
};

const fillFinanzielleSituationStartFormFeutzLuzern = (
    dataset: keyof typeof FixtureFinSitFeutzLuzern
) => {
    FixtureFinSitFeutzLuzern[dataset](({Start}) => {
        cy.wait(2000);
        getIban().type(Start.iban);
        getKontoinhaberIn().type(Start.kontoinhaber);
        getSozialhilfebezuegerx(Start.sozialhilfeBezueger)
            .find('input')
            .click();
    });
};

const fillFinanzielleSituationStartFormFeutzSolothurn = (
    dataset: keyof typeof FixtureFinSitFeutzSolothurn
) => {
    FixtureFinSitFeutzSolothurn[dataset](({Start}) => {
        cy.wait(1000);
        getSozialhilfebezuegerx(Start.sozialhilfebeziehende)
            .find('label')
            .click();
        cy.wait(1000);
        getSteuerveranlagungGemeinsam(Start.steuererklaerungGemeinsam)
            .find('label')
            .click();
    });
};

const getInfomaKred = () => {
    return cy.getByData('infoma_kred');
};

const getInfomaBank = () => {
    return cy.getByData('infoma_bankcode');
};

const saveForm = () => {
    cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
        NavigationPO.saveAndGoNext();
    });
};

export const FinanzielleSituationStartPO = {
    // page objects
    getSozialhilfebezueger,
    getIban,
    getKontoinhaberIn,
    getInfomaKred,
    getInfomaBank,
    // page actions
    fillFinanzielleSituationStartForm,
    fillFinanzielleSituationStartFormFeutz,
    fillFinanzielleSituationStartFormFeutzLuzern,
    fillFinanzielleSituationStartFormFeutzSolothurn,
    saveForm
};
