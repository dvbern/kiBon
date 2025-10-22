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
    FixtureEinkommensverschlechterungAppenzell,
    FixtureEinkommensverschlechterung,
    FixtureEinkommensverschlechterungLuzern,
    FixtureEinkommensverschlechterungSchwyz,
    FixtureEinkommensverschlechterungSolothurn
} from '@dv-e2e/fixtures';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getSectionTitle = () => {
    return cy.getByData('einkommensverschlechterungResultat');
};

const getBruttovermoegenGS1 = () => {
    return cy.getByData('bruttovermoegen1');
};

const getBruttovermoegenGS2 = () => {
    return cy.getByData('bruttovermoegen2');
};

const getSchuldenGS1 = () => {
    return cy.getByData('schulden1');
};

const getSchuldenGS2 = () => {
    return cy.getByData('schulden2');
};

const getEinkommenBeiderGesuchsteller = () => {
    return cy.getByData('einkommen-beider-gesuchsteller');
};

const getEinkommenVorjahrBasis = () => {
    return cy.getByData('einkommen-vorjahr-basis');
};

const getEinkommenVorjahr = () => {
    return cy.getByData('einkommen-vorjahr');
};

const getMassgebendesEinkommenVorher = () => {
    return cy.getByData('massgebendesEinkommenVorher');
};

const getEinkommenVorjahrLuzern = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrOld');
};

const getMassgebendesEinkommenAufgrundSteuerveranlagung = () => {
    return cy.getByData(
        'massgebendesEinkommenAufgrundLetzterSteuerveranlagung'
    );
};

const getMassgebendesEinkommenGS = (gs: string) => {
    return cy.getByData('massgebendesEinkommen' + gs);
};

const getMassgebendesEinkommenNachher = () => {
    return cy.getByData('massgebendesEinkommenNachher');
};

const getEinkommenCurrentJahrLuzern = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrNew');
};

const getAenderungPercent = () => {
    return cy.getByData('aenderungPercent');
};

const getMassgebendesEinkVorAbzFamGr = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGr');
};

const getMassgebendesEinkVorAbzFamGrGS1 = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrGS1');
};

const getMassgebendesEinkVorAbzFamGrGS2 = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrGS2');
};

const getMassgebendesEinkVorAbzFamGrYear = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrYear');
};

const getResultatProzent = () => {
    return cy.getByData('resultatProzent');
};

// !! -- PAGE ACTIONS -- !!
const fillResultateForm = (
    dataset: keyof typeof FixtureEinkommensverschlechterung,
    jahr: 'jahr1' | 'jahr2'
) => {
    FixtureEinkommensverschlechterung[dataset](({[jahr]: {Resultate}}) => {
        getBruttovermoegenGS1().find('input').type(Resultate.bruttovermoegen1);
        getBruttovermoegenGS2().find('input').type(Resultate.bruttovermoegen2);
        getSchuldenGS1().find('input').type(Resultate.schulden1);
        getSchuldenGS2().find('input').type(Resultate.schulden2);
    });
};

function checkResultsSchwyz() {
    cy.wait(1500);
    FixtureEinkommensverschlechterungSchwyz.withValid(data => {
        cy.wait(1500);
        getMassgebendesEinkommenVorher().find('input').should('be.disabled');
        getMassgebendesEinkommenVorher()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenVorher);
        getMassgebendesEinkommenGS('GS1')
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenGS1);
        getMassgebendesEinkommenGS('GS2')
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenGS2);
        getMassgebendesEinkommenNachher()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenNachher);
        cy.wait(1500);
        getAenderungPercent()
            .find('input')
            .should('have.value', data.Resultate.aenderungPercent);
    });
}

function checkResultsLuzern() {
    FixtureEinkommensverschlechterungLuzern.withValid(data => {
        cy.wait(1500);
        getEinkommenVorjahrLuzern().find('input').should('be.disabled');
        getEinkommenVorjahrLuzern()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenVorher);
        getEinkommenCurrentJahrLuzern()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenNachher);
        cy.wait(1500);
        getAenderungPercent()
            .find('input')
            .should('have.value', data.Resultate.aenderungPercent);
    });
}

function checkResultsAppenzell() {
    FixtureEinkommensverschlechterungAppenzell.withValid(data => {
        cy.wait(1500);

        getMassgebendesEinkommenAufgrundSteuerveranlagung()
            .find('input')
            .should('be.disabled');
        getMassgebendesEinkommenAufgrundSteuerveranlagung()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenTotal);

        getMassgebendesEinkommenGS('GS1').find('input').should('be.disabled');
        getMassgebendesEinkommenGS('GS1')
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenGS1);

        getMassgebendesEinkommenGS('GS2').find('input').should('be.disabled');
        getMassgebendesEinkommenGS('GS2')
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommenGS2);

        getMassgebendesEinkVorAbzFamGr().find('input').should('be.disabled');
        getMassgebendesEinkVorAbzFamGr()
            .find('input')
            .should('have.value', data.Resultate.massgebendesEinkommen);

        getAenderungPercent().find('input').should('be.disabled');
        getAenderungPercent()
            .find('input')
            .should('have.value', data.Resultate.einkommensaenderungInProzent);
    });
}

function checkResultsSolothurn(
    dataset: keyof typeof FixtureEinkommensverschlechterungSolothurn,
    year: string
) {
    FixtureEinkommensverschlechterungSolothurn[dataset](data => {
        cy.wait(1500);
        if (year === '1') {
            getMassgebendesEinkVorAbzFamGr()
                .find('input')
                .should('be.disabled');
            getMassgebendesEinkVorAbzFamGr()
                .find('input')
                .should(
                    'have.value',
                    data.jahr1.Resultate.massgebendesEinkVorAbzFamGr
                );
            getMassgebendesEinkVorAbzFamGrGS1()
                .find('input')
                .should(
                    'have.value',
                    data.jahr1.Resultate.massgebendesEinkVorAbzFamGrGS1
                );
            getMassgebendesEinkVorAbzFamGrGS2()
                .find('input')
                .should(
                    'have.value',
                    data.jahr1.Resultate.massgebendesEinkVorAbzFamGrGS2
                );
            getMassgebendesEinkVorAbzFamGrYear()
                .find('input')
                .should(
                    'have.value',
                    data.jahr1.Resultate.massgebendesEinkVorAbzFamGrYear
                );
            getResultatProzent()
                .find('input')
                .should('have.value', data.jahr1.Resultate.resultatProzent);
        } else {
            getMassgebendesEinkVorAbzFamGr()
                .find('input')
                .should('be.disabled');
            getMassgebendesEinkVorAbzFamGr()
                .find('input')
                .should(
                    'have.value',
                    data.jahr2.Resultate.massgebendesEinkVorAbzFamGr
                );
            getMassgebendesEinkVorAbzFamGrGS1()
                .find('input')
                .should(
                    'have.value',
                    data.jahr2.Resultate.massgebendesEinkVorAbzFamGrGS1
                );
            getMassgebendesEinkVorAbzFamGrGS2()
                .find('input')
                .should(
                    'have.value',
                    data.jahr2.Resultate.massgebendesEinkVorAbzFamGrGS2
                );
            getMassgebendesEinkVorAbzFamGrYear()
                .find('input')
                .should(
                    'have.value',
                    data.jahr2.Resultate.massgebendesEinkVorAbzFamGrYear
                );
            getResultatProzent()
                .find('input')
                .should('have.value', data.jahr2.Resultate.resultatProzent);
        }
    });
}

export const EinkommensverschlechterungResultatePO = {
    getPageTitle,
    getBruttovermoegenGS1,
    getBruttovermoegenGS2,
    getSchuldenGS1,
    getSchuldenGS2,
    getEinkommenBeiderGesuchsteller,
    getEinkommenVorjahrBasis,
    getEinkommenVorjahr,
    getSectionTitle,
    getMassgebendesEinkommenAufgrundSteuerveranlagung,
    getMassgebendesEinkVorAbzFamGr,
    // page actions
    fillResultateForm,
    checkResultsSchwyz,
    checkResultsSolothurn,
    checkResultsLuzern,
    checkResultsAppenzell
};
