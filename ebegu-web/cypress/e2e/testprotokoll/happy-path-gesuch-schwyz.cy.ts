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

import {FixtureFinSitFeutz} from '@dv-e2e/fixtures';
import {
    AntragBeschaeftigungspensumPO,
    AntragBetreuungPO,
    AntragCreationPO,
    AntragFamSitPO,
    AntragKindPO,
    ConfirmDialogPO,
    DokumentePO,
    EinkommensverschlechterungPO,
    EinkommensverschlechterungResultatePO,
    FinanzielleSituationPO,
    FinanzielleSituationResultatePO,
    FinanzielleSituationStartPO,
    FreigabePO,
    GesuchstellendeDashboardPO,
    NavigationPO,
    RegistrationAbschliessenPO,
    SidenavPO,
    TestFaellePO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';
import {GemeindeTestFall, getUser, User} from '@dv-e2e/types';
import {MANDANTS} from '@kibon/shared-model-mandant';
import {GesuchstellendePO} from '../../page-objects/antrag/gesuchstellende.po';

describe('Schwyz - Happy Path Gesuch', () => {
    const userSuperadmin: User = getUser('[1-Superadmin] Super User');
    const userGemeinde: User = getUser('[6-P-SB-Gemeinde] Stefan Wirth');
    const userTraegerschaft: User = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    const userGS: User = getUser('[5-GS] Emma Gerber');
    const gemeinde: GemeindeTestFall = 'Testgemeinde Schwyz';

    beforeEach(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    before(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        // DELETE GESUCHE FROM EMMA GERBER
        cy.changeMandant(MANDANTS.SCHWYZ);
        cy.login(userSuperadmin);
        cy.intercept('GET', '**/benutzer/gesuchsteller').as(
            'loadingGesuchsteller'
        );
        cy.visit('#/testdaten');
        cy.wait('@loadingGesuchsteller');
        TestFaellePO.getGesuchstellerFaelleLoeschen().click();
        TestFaellePO.getGesuchstellerInToRemoveFaelle(userGS).click();
        cy.waitForRequest('DELETE', '**/testfaelle/testfallgs/*', () => {
            TestFaellePO.getGesucheLoeschenButton().click();
        });
    });

    it('should register and login to schwyz as GS emma gerber', () => {
        register(userGS, gemeinde);
    });

    it('run Happy Path Schwyz Gesuch', () => {
        register(userGS, gemeinde);
        //GESUCH STELLEN
        openAntrag(userGS);
        clickSave();

        //Familiensituation
        fillFamilienSituation();

        // Get AntragsID
        SidenavPO.getGesuchsDaten()
            .then(el$ => el$.data('antrags-id'))
            .as('antragsId');
        const antragIdAlias = '@antragsId';

        // KIBON-3641 (test fails because date is in new periode and gutschein will only be applied the following month)
        cy.changeLogin(userSuperadmin);
        changeEingangsDatum(antragIdAlias);

        cy.changeLogin(userGS);
        openGesuchInKinder(antragIdAlias);

        //Kinder
        createKinder();

        //Betreuung
        fillBetreuungen(gemeinde);

        // BESCHAEFTIGUNGSPENSUM
        fillBeschaeftigungspensum();

        // FINANZIELLE VERHAELTNISSE
        fillFinSit();

        // Resultate
        checkFinSitResults();

        // EINKOMMENSVERSCHLECHTERUNG
        fillEinkommensverschlechterung();

        // EINKOMMENSVERSCHLECHTERUNG RESULTATE
        checkEinkommensverschlechterungResults();

        // DOKUMENTE
        uploadDummyDocuments();

        // PLATZBESTAETIGUNG mit Kita SB
        // !!!!!! - New User - !!!!!!
        cy.changeLogin(userTraegerschaft);
        openGesuchInBetreuung(antragIdAlias);

        AntragBetreuungPO.getBetreuung(0, 0).click();
        AntragBetreuungPO.fillKitaBetreuungspensumFormSchwyz(
            'withValid',
            'Testgemeinde Schwyz'
        );
        AntragBetreuungPO.platzBestaetigen();

        openGesuchInBetreuung(antragIdAlias);
        AntragBetreuungPO.getBetreuung(1, 0).click();
        AntragBetreuungPO.fillKitaBetreuungspensumFormSchwyz(
            'withValid',
            'Testgemeinde Schwyz'
        );
        AntragBetreuungPO.platzBestaetigen();

        // FREIGABE
        cy.changeLogin(userGS);
        openGesuchInFreigabe(antragIdAlias);
        FreigabePO.getApproveCorrectDataValuesCheckbox().click();
        cy.wait(1000);
        FreigabePO.getFreigebenButtonSchwyz().click();

        // VERFUEGUNG
        startVerfuegen(userGemeinde, antragIdAlias);
    });
});

function register(user: User, gemeinde: GemeindeTestFall) {
    cy.login(user);
    cy.visit('/#/registration-abschliessen');
    RegistrationAbschliessenPO.getBGBeantragen().click();
    RegistrationAbschliessenPO.getGemeindeSelection().select(gemeinde);
    RegistrationAbschliessenPO.getRegistrierenButton().click();
    RegistrationAbschliessenPO.getRegistrierungAbschliessenButton().click();
}

function clickSave() {
    cy.wait(1500);
    NavigationPO.getSaveAndNextButton().should(
        'not.have.a.property',
        'disabled'
    );
    cy.wait(1500);
    NavigationPO.saveAndGoNext();
}

function openAntrag(user: User) {
    cy.login(user);
    cy.waitForRequest('GET', '**/gesuchsperioden/aktive/gemeinde/*', () => {
        cy.visit('/#/dossier/gesuchstellerDashboard');
    });
    cy.wait(2000);
    AntragCreationPO.getAntragBearbeitenButton().should(
        'include.text',
        'Gesuch stellen'
    );
    GesuchstellendeDashboardPO.getCorrectPeriodeGesuchButton('2025/26').click();
}

function checkEinkommensverschlechterungResults() {
    cy.groupBy('Resultate', () => {
        cy.url().should('include', 'einkommensverschlechterung');
        EinkommensverschlechterungResultatePO.getSectionTitle().should(
            'include.text',
            'Resultate Einkommensänderung'
        );
        EinkommensverschlechterungResultatePO.checkResultsSchwyz();
    });
    cy.waitForRequest('GET', '**/dokumente/**', () => {
        clickSave();
    });
}

function createKinder(): void {
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFamilyFeutz('withValidGirl');
    clickSave();

    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFamilyFeutz('withValidBoy');
    clickSave();

    AntragKindPO.getPageTitle().should('include.text', 'Kinder');

    cy.waitForRequest('POST', '**/wizard-steps', () => {
        clickSave();
    });
}

function checkFinSitResults() {
    FixtureFinSitFeutz.withValid(({Resultate}) => {
        FinanzielleSituationResultatePO.getMassgebendesEinkommen('GS1')
            .find('input')
            .should('have.value', Resultate.massgebendesEinkommenGS1);
        FinanzielleSituationResultatePO.getMassgebendesEinkommen('GS2')
            .find('input')
            .should('have.value', Resultate.massgebendesEinkommenGS2);
        FinanzielleSituationResultatePO.getMassgebendesEinkommenTotal()
            .find('input')
            .should('have.value', Resultate.massgebendesEinkommenTotal);
    });

    cy.waitForRequest(
        'GET',
        '**/einkommensverschlechterung/minimalesMassgebendesEinkommen/**',
        () => {
            clickSave();
        }
    );
}

function uploadDummyDocuments() {
    // Test upload file
    cy.fixture('documents/small.png').as('smallPng');

    // Upload the file on every <input type=file>, Angular JS file upload makes specific upload difficult:
    // https://github.com/abramenal/cypress-file-upload/tree/main/recipes/angularjs-ng-file-upload
    // https://github.com/danialfarid/ng-file-upload/issues/1140
    // https://github.com/danialfarid/ng-file-upload/issues/1167
    DokumentePO.getAllFileUploads().each(($el, index) => {
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
    cy.waitForRequest('POST', '**/wizard-steps', () => {
        clickSave();
    });
}

function startVerfuegen(user: User, antradId: string) {
    cy.changeLogin(user);
    openGesuchInFreigabe(antradId);
    clickSave();
    SidenavPO.getGesuchStatus().should('have.text', 'In Bearbeitung');

    VerfuegenPO.finSitAkzeptieren();
    VerfuegenPO.pruefeGesuch();
    SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');

    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        VerfuegenPO.getVerfuegenStartenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');

    // KIND 1
    VerfuegenPO.getVerfuegung(0, 0).click();
    VerfuegungPO.getAnspruchberechtigtesBetreuungspensum(5).should(
        'include.text',
        '40%'
    );
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnElternHeading().should(
        'have.text',
        'An Eltern geschuldeter Beitrag [Fr.]'
    );
    VerfuegenPO.getAuszahlungAnEltern(0).should('have.text', '346.60');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'Verfügt');
    SidenavPO.getGesuchStatus().should('have.text', 'Verfügen');

    // KIND 2
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(1, 0).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnInstitutionHeading().should(
        'have.text',
        'An Institution geschuldeter Beitrag [Fr.]'
    );
    VerfuegenPO.getAuszahlungAnInstitution(0).should('have.text', '346.60');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'Verfügt');
    SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
}

function fillEinkommensverschlechterung() {
    EinkommensverschlechterungPO.fillEinkommensverschlechterungFormSchwyz(
        'withValid',
        'GS1'
    );
    clickSave();
    EinkommensverschlechterungPO.fillEinkommensverschlechterungFormSchwyz(
        'withValid',
        'GS2'
    );
}

function openGesuchInFamSit(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/familiensituation-sz/${antragsId}`)
    );
}

function openGesuchInBetreuung(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/betreuungen/${antragsId}`)
    );
}

function openGesuchInKinder(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/kinder/${antragsId}`)
    );
}

function openGesuchInFreigabe(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/freigabe-online/${antragsId}`)
    );
}

function fillBetreuungen(gemeinde: GemeindeTestFall) {
    //KITA KIND 1
    AntragBetreuungPO.createNewBetreuung(0);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withSchwyz', gemeinde);
    AntragBetreuungPO.platzBestaetigungAnfordern();
    //KITA KIND 2
    AntragBetreuungPO.createNewBetreuung(1);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withSchwyz', gemeinde);
    AntragBetreuungPO.fillAuszahlungBeitraege();
    AntragBetreuungPO.platzBestaetigungAnfordern();

    AntragBetreuungPO.getPageTitle().should('include.text', 'Betreuung');
    cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
        clickSave();
    });
}

function fillFamilienSituation() {
    cy.wait(5000);
    AntragFamSitPO.getFamSitTitle().should('have.text', 'Familiensituation');
    AntragFamSitPO.fillFamiliensituationFormSchwyz('withValid');
    clickSave();
    GesuchstellendePO.fillVerheiratetFamFeutz('withValid');
    clickSave();
}

function fillBeschaeftigungspensum() {
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensumFamFeutz(
        'GS1',
        'withValid'
    );
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensumFamFeutz(
        'GS2',
        'withValid'
    );
    clickSave();
}

function fillFinSit() {
    // Config
    FinanzielleSituationStartPO.fillFinanzielleSituationStartFormFeutz(
        'withValid'
    );
    NavigationPO.saveAndGoNext();
    // Finanzielle Situation - GS 1
    FinanzielleSituationPO.fillFinanzielleSituationFormFeutzSchwyz(
        'withValid',
        'GS1'
    );
    FinanzielleSituationPO.saveFormAndGoNext();

    // Finanzielle Situation - GS 2
    FinanzielleSituationPO.fillFinanzielleSituationFormFeutzSchwyz(
        'withValid',
        'GS2'
    );
    NavigationPO.saveAndGoNext();
}

function changeEingangsDatum(antragIdAlias: string) {
    openGesuchInFamSit(antragIdAlias);
    cy.wait(1000);
    SidenavPO.goTo('GESUCH_ERSTELLEN');
    AntragCreationPO.getEingangsdatum().find('input').type('01.07.2024');
    AntragCreationPO.getAlternativdatum().find('input').type('01.07.2024');

    AntragCreationPO.getSpeichernUndWeiterButton().click();
}
