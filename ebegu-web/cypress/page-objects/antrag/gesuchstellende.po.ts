/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
    FixtureFamSit,
    FixtureFamSitFeutz,
    FixtureFamSitFeutzLuzern,
    FixtureFamSitFeutzSolothurn,
    FixtureFamSitFeutzAppenzell
} from '@dv-e2e/fixtures';
import {DokumentePO, NavigationPO} from '@dv-e2e/page-objects';

const getGeschlechtOption = (selection: string) => {
    return cy.getByData(`geschlecht.radio-value.${selection}`);
};

const getVorname = () => {
    return cy.getByData('vorname');
};

const getNachname = () => {
    return cy.getByData('nachname');
};

const getMobilePhone = () => {
    return cy.getByData('mobile');
};

const getGeburtsdatum = () => {
    return cy.getByData('geburtsdatum');
};

const getMail = () => {
    return cy.getByData('email');
};

const getSozialVersicherungsNummer = () => {
    return cy.getByData('sozialversicherungsnummer');
};

const getKorrespondenzsprache = () => {
    return cy.getByData('korrespondenzSprache');
};

const getAdresseStrasse = () => {
    return cy.getByData('container.wohn', 'adresseStrasse');
};

const getAdresseHausnummer = () => {
    return cy.getByData('container.wohn', 'adresseHausnummer');
};

const getAdressePlz = () => {
    return cy.getByData('container.wohn', 'adressePlz');
};

const getAdresseOrt = () => {
    return cy.getByData('container.wohn', 'adresseOrt');
};

const getFormularTitle = () => {
    return cy.getByData('gesuchformular-title');
};

// !! -- PAGE ACTIONS -- !!
const fillVerheiratet = (dataset: keyof typeof FixtureFamSit) => {
    FixtureFamSit[dataset](({GS1}) => {
        fillGS1(GS1);
    });
    NavigationPO.saveAndGoNext();
    getFormularTitle().should('include.text', '2');
    FixtureFamSit[dataset](({GS2}) => {
        fillBaseGesuchsteller(GS2);
    });
};

const fillAntragsstellendeAppenzell = (
    dataset: keyof typeof FixtureFamSitFeutzAppenzell
) => {
    FixtureFamSitFeutzAppenzell[dataset](({GS1}) => {
        fillGS1Appenzell(GS1);
    });
    NavigationPO.saveAndGoNext();
    getFormularTitle().should('include.text', '2');
    FixtureFamSitFeutzAppenzell[dataset](({GS2}) => {
        fillBaseGesuchsteller(GS2);
    });
};

const fillVerheiratetFamFeutz = (dataset: keyof typeof FixtureFamSitFeutz) => {
    FixtureFamSitFeutz[dataset](({GS1}) => {
        fillGS1Schwyz(GS1);
    });
    NavigationPO.saveAndGoNext();
    getFormularTitle().should('include.text', '2');
    FixtureFamSitFeutz[dataset](({GS2}) => {
        fillGS2Schwyz(GS2);
    });
};
const fillVerheiratetFamFeutzLuzern = (
    dataset: keyof typeof FixtureFamSitFeutzLuzern
) => {
    FixtureFamSitFeutzLuzern[dataset](({GS1}) => {
        fillGS1Luzern(GS1);
    });
    NavigationPO.saveAndGoNext();
    getFormularTitle().should('include.text', '2');
    FixtureFamSitFeutzLuzern[dataset](({GS2}) => {
        fillGS2Luzern(GS2);
    });
};

// TODO: type this
function fillGS1(gesuchSteller: any): void {
    fillBaseGesuchsteller(gesuchSteller);
    getKorrespondenzsprache().select(gesuchSteller.korrespondenzSprache);
    fillBaseAdress(gesuchSteller);
}

function fillBaseAdress(gesuchSteller: any) {
    getAdresseStrasse().type(gesuchSteller.adresseStrasse);
    getAdresseHausnummer().type(gesuchSteller.adresseHausnummer);
    getAdressePlz().type(gesuchSteller.adressePlz);
    getAdresseOrt().type(gesuchSteller.adresseOrt);
}

function fillEmail(gesuchSteller: any) {
    getMail().type(gesuchSteller.email);
}

function fillGS1Luzern(gesuchSteller: any) {
    fillBaseGesuchsteller(gesuchSteller);
    uploadDummyAusweis();
    fillBaseAdress(gesuchSteller);
}
function fillGS2Luzern(gesuchSteller: any) {
    fillBaseGesuchsteller(gesuchSteller);
}

function fillGS1Schwyz(gesuchSteller: any): void {
    fillBaseGesuchsteller(gesuchSteller);
    getSozialVersicherungsNummer().type(
        gesuchSteller.sozialversicherungsnummer
    );
    fillBaseAdress(gesuchSteller);
    getMobilePhone().type(gesuchSteller.mobile);
}

function fillGS2Schwyz(gesuchSteller: any): void {
    fillBaseGesuchsteller(gesuchSteller);
    getSozialVersicherungsNummer().type(
        gesuchSteller.sozialversicherungsnummer
    );
    getMobilePhone().type(gesuchSteller.mobile);
}

function fillGS1Appenzell(gesuchSteller: any): void {
    fillBaseGesuchsteller(gesuchSteller);
    fillBaseAdress(gesuchSteller);
}

function fillGSSolothurn(dataset: keyof typeof FixtureFamSitFeutzSolothurn) {
    FixtureFamSitFeutzSolothurn[dataset](({GS1}) => {
        fillBaseGesuchsteller(GS1);
        fillBaseAdress(GS1);
    });
    NavigationPO.saveAndGoNext();

    getFormularTitle().should('include.text', '2');
    FixtureFamSitFeutzSolothurn[dataset](({GS2}) => {
        fillBaseGesuchsteller(GS2);
    });
}

const fillBaseGesuchsteller = (GS1: {
    geschlecht: string;
    vorname: string;
    nachname: string;
    geburtsdatum: string;
}) => {
    cy.wait(2000);
    getGeschlechtOption(GS1.geschlecht).click();
    getVorname().clear().type(GS1.vorname);
    getNachname().clear().type(GS1.nachname);
    getGeburtsdatum().find('input').type(GS1.geburtsdatum);
    cy.wait(1500);
};

function uploadDummyAusweis() {
    cy.fixture('documents/small.png').as('smallPng');

    getAusweisUpload().each(($el, index) => {
        const upload = `fileUpload#${index}`;
        cy.intercept('POST', '**/upload').as(upload);
        cy.wrap($el).selectFile(
            {
                contents: '@smallPng',
                fileName: `small-${index}.png`
            },
            {force: true}
        );
        return cy.wait(`@${upload}`);
    });
}

const getAusweisUpload = () => {
    return cy.getByData('mutipleFiles');
};

export const GesuchstellendePO = {
    // page objects
    getGeschlechtOption,
    getVorname,
    getNachname,
    getGeburtsdatum,
    getKorrespondenzsprache,
    getAdresseStrasse,
    getAdresseHausnummer,
    getAdressePlz,
    getAdresseOrt,
    getFormularTitle,
    // page actions
    fillVerheiratet,
    fillGS1Appenzell,
    fillAntragsstellendeAppenzell,
    fillVerheiratetFamFeutz,
    fillVerheiratetFamFeutzLuzern,
    fillGSSolothurn,
    fillBaseGesuchsteller
};
