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

import {FixtureFinSitFeutzLuzern} from '@dv-e2e/fixtures';
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

xdescribe('Luzern - Happy Path Gesuch', () => {
    const userSuperadmin: User = getUser('[1-Superadmin] Super User');
    const userGemeinde: User = getUser('[6-P-SB-Gemeinde] Stefan Wirth');
    const userTraegerschaft: User = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    const userGS: User = getUser('[5-GS] Emma Gerber');
    const gemeinde: GemeindeTestFall = 'Testgemeinde Luzern';

    beforeEach(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    before(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        // DELETE GESUCHE FROM EMMA GERBER
        cy.changeMandant(MANDANTS.LUZERN);
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

    it('should register and login to Luzern as GS emma gerber', () => {
        register(userGS, gemeinde);
    });

    it('run Happy Path Luzern Gesuch', () => {
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
        bestaetigePlatzAsKita(antragIdAlias, userTraegerschaft, gemeinde);

        // FREIGABE
        startFreigabe(userGS, antragIdAlias);

        // VERFUEGUNG
        startVerfuegenLuzern(userGemeinde, antragIdAlias, userSuperadmin);
    });
});

describe('Horw - Happy Path Gesuch', () => {
    const userSuperadmin: User = getUser('[1-Superadmin] Super User');
    const userTraegerschaft: User = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    const userGS: User = getUser('[5-GS] Emma Gerber');
    const gemeinde: GemeindeTestFall = 'Testgemeinde Horw';

    before(() => {
        // DELETE GESUCHE FROM EMMA GERBER
        cy.changeMandant(MANDANTS.LUZERN);
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.ignoreUncaughtException();
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

    it('should register and login to Horw as GS emma gerber', () => {
        cy.ignoreUncaughtException();
        register(userGS, gemeinde);
    });

    it('run Happy Path Horw Gesuch', () => {
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
        bestaetigePlatzAsKita(antragIdAlias, userTraegerschaft, gemeinde);

        // FREIGABE
        startFreigabe(userGS, antragIdAlias);

        // VERFUEGUNG
        startVerfuegenHorw(antragIdAlias, userSuperadmin);
    });
});

function register(user: User, gemeinde: GemeindeTestFall) {
    cy.login(user);
    cy.visit('/#/registration-abschliessen');
    cy.wait(1500);
    RegistrationAbschliessenPO.getBGBeantragen().click();
    RegistrationAbschliessenPO.getGemeindeSelection().select(gemeinde);
    RegistrationAbschliessenPO.getRegistrierenButton().click();
    cy.wait(1500);
    RegistrationAbschliessenPO.getRegistrierungAbschliessenButton().click();
    cy.wait(1500);
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
        'Antrag stellen'
    );
    GesuchstellendeDashboardPO.getCorrectPeriodeGesuchButton('2023/24').click();
}

function checkEinkommensverschlechterungResults() {
    cy.groupBy('Resultate', () => {
        cy.url().should('include', 'einkommensverschlechterung');
        EinkommensverschlechterungResultatePO.getSectionTitle().should(
            'include.text',
            'Resultate Einkommensänderung'
        );
        EinkommensverschlechterungResultatePO.checkResultsLuzern();
    });
    cy.waitForRequest('GET', '**/dokumente/**', () => {
        clickSave();
    });
}

function createKinder(): void {
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFamilyFeutzLuzern('withValidGirl');
    clickSave();

    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFamilyFeutzLuzern('withValidBoy');
    clickSave();

    AntragKindPO.getPageTitle().should('include.text', 'Kinder');

    cy.waitForRequest('POST', '**/wizard-steps', () => {
        clickSave();
    });
}

function checkFinSitResults() {
    FixtureFinSitFeutzLuzern.withValid(({Resultate}) => {
        FinanzielleSituationResultatePO.getMassgebendesEinkommenLuzern()
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

function bestaetigePlatzAsKita(
    antragIdAlias: string,
    userTraegerschaft: User,
    gemeinde: GemeindeTestFall
) {
    cy.changeLogin(userTraegerschaft);

    openGesuchInBetreuung(antragIdAlias);
    AntragBetreuungPO.getBetreuung(0, 0).click();
    AntragBetreuungPO.fillKitaBetreuungspensumFormLuzern('withValid', gemeinde);
    AntragBetreuungPO.platzBestaetigen();

    openGesuchInBetreuung(antragIdAlias);
    AntragBetreuungPO.getBetreuung(0, 1).click();
    AntragBetreuungPO.fillKitaBetreuungspensumFormLuzern('withValid', gemeinde);
    AntragBetreuungPO.platzBestaetigen();

    openGesuchInBetreuung(antragIdAlias);
    AntragBetreuungPO.getBetreuung(1, 0).click();
    AntragBetreuungPO.fillKitaBetreuungspensumFormLuzern('withValid', gemeinde);
    AntragBetreuungPO.platzBestaetigen();

    openGesuchInBetreuung(antragIdAlias);
    AntragBetreuungPO.getBetreuung(1, 1).click();
    AntragBetreuungPO.fillKitaBetreuungspensumFormLuzern('withValid', gemeinde);
    AntragBetreuungPO.platzBestaetigen();
}

function startFreigabe(userGS: User, antragIdAlias: string) {
    cy.changeLogin(userGS);
    openGesuchInFreigabe(antragIdAlias);
    cy.wait(1000);
    FreigabePO.getFreigebenButton().click();
    cy.wait(1000);

    // SUPPRESS NEW TAB (Freigabequittung) BECAUSE CYPRESS DOESNT SUPPORT MULTITABS
    cy.window().then(win => {
        cy.stub(win, 'open').as('windowOpen'); // stub window.open
    });
    FreigabePO.getConfirmButton().click();
    cy.wait(1500);
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

function simulateFreigabeQuittung(superAdmin: User, antragId: string) {
    // SIMULATE FREIGABE QUITTUNG
    cy.changeLogin(superAdmin);
    openGesuchInFreigabe(antragId);
    cy.waitForRequest('GET', '**/dossier/fall/**', () => {
        FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
    });
}

function startVerfuegenHorw(antragId: string, superAdmin: User) {
    simulateFreigabeQuittung(superAdmin, antragId);
    clickSave();

    // USER GEMEINDE VERFUEGUNG
    SidenavPO.getGesuchStatus().should('have.text', 'Freigegeben');
    VerfuegenPO.finSitAkzeptieren();
    VerfuegenPO.pruefeGesuch();
    SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        VerfuegenPO.getVerfuegenStartenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    // KIND 1 BETREUUNG 1
    VerfuegenPO.getVerfuegung(0, 0).click();
    VerfuegungPO.getAnspruchberechtigtesBetreuungspensum(3).should(
        'include.text',
        '70%'
    );
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnInstitutionHeading().should(
        'have.text',
        'An Institution geschuldeter Betrag'
    );
    VerfuegenPO.getAuszahlungAnInstitution(0).should('have.text', '964.45');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'BG Bestätigt');
    SidenavPO.getGesuchStatus().should('have.text', 'BG Bestätigen');

    // KIND 1 BETREUUNG 2
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(0, 1).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    // KIND 2 BETREUUNG 1
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(1, 1).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    // KIND 2 BETREUUNG 2
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(1, 0).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnElternHeading().should(
        'have.text',
        'An Eltern geschuldeter Betrag'
    );
    VerfuegenPO.getAuszahlungAnEltern(0).should('have.text', '1’281.70');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'BG Bestätigt');
    SidenavPO.getGesuchStatus().should('have.text', 'BG Bestätigt');
}

function startVerfuegenLuzern(
    userGemeinde: User,
    antragId: string,
    superAdmin: User
) {
    // SIMULATE FREIGABE QUITTUNG
    simulateFreigabeQuittung(superAdmin, antragId);
    clickSave();

    // FILL INFOMA LUZERN
    openGesuchInFinSitLu(antragId);
    cy.wait(1500);
    FinanzielleSituationStartPO.getInfomaKred().type('123');
    FinanzielleSituationStartPO.getInfomaBank().type('123');
    clickSave();
    cy.wait(1500);

    cy.changeLogin(userGemeinde);
    openGesuchInResults(antragId);
    cy.wait(1500);

    // USER GEMEINDE VERFUEGUNG
    SidenavPO.getGesuchStatus().should('have.text', 'In Bearbeitung');
    VerfuegenPO.finSitAkzeptieren();
    VerfuegenPO.pruefeGesuch();
    SidenavPO.getGesuchStatus().should('have.text', 'Geprüft');
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        VerfuegenPO.getVerfuegenStartenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    SidenavPO.getGesuchStatus().should('have.text', 'BG Bestätigen');

    // KIND 1 BETREUUNG 1
    VerfuegenPO.getVerfuegung(0, 0).click();
    VerfuegungPO.getAnspruchberechtigtesBetreuungspensum(3).should(
        'include.text',
        '70%'
    );
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnInstitutionHeading().should(
        'have.text',
        'An Institution geschuldeter Betrag'
    );
    VerfuegenPO.getAuszahlungAnInstitution(0).should('have.text', '964.45');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'BG Bestätigt');
    SidenavPO.getGesuchStatus().should('have.text', 'BG Bestätigen');

    // KIND 1 BETREUUNG 2
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(0, 1).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    // KIND 2 BETREUUNG 1
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(1, 1).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    // KIND 2 BETREUUNG 2
    cy.waitForRequest('GET', '**/verfuegung/calculate/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });
    VerfuegenPO.getVerfuegung(1, 0).click();
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        VerfuegungPO.getVerfuegenButton().click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });
    VerfuegenPO.getAuszahlungAnElternHeading().should(
        'have.text',
        'An Eltern geschuldeter Betrag'
    );
    VerfuegenPO.getAuszahlungAnEltern(0).should('have.text', '1’281.70');
    VerfuegungPO.getVerfuegenStatusTitle().should('have.text', 'BG Bestätigt');
    SidenavPO.getGesuchStatus().should('have.text', 'BG Bestätigt');
}

function fillEinkommensverschlechterung() {
    EinkommensverschlechterungPO.fillEinkommensverschlechterungFormLuzern(
        'withValid'
    );
    clickSave();
}

function openGesuchInFamSit(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/familiensituation/${antragsId}`)
    );
}

function openGesuchInFinSitLu(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/lu/finanzielleSituationStart/${antragsId}`)
    );
}

function openGesuchInResults(antragIdAlias: string) {
    cy.get(antragIdAlias).then(antragsId =>
        cy.visit(`/#/gesuch/verfuegen/${antragsId}`)
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
        cy.visit(`/#/gesuch/freigabe-mit-quittung/${antragsId}`)
    );
}

function fillBetreuungen(gemeinde: GemeindeTestFall) {
    //KITA 1 KIND 1
    AntragBetreuungPO.createNewBetreuung(0);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withValid', gemeinde);
    AntragBetreuungPO.selectAusserordentlicherBedarf('nein');
    AntragBetreuungPO.fillAuszahlungBeitraege();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 2 KIND 1
    AntragBetreuungPO.createNewBetreuung(0);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withValid', gemeinde, {
        kita2: true
    });
    AntragBetreuungPO.selectAusserordentlicherBedarf('nein');
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 1 KIND 2
    AntragBetreuungPO.createNewBetreuung(1);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withValid', gemeinde);
    AntragBetreuungPO.selectAusserordentlicherBedarf('nein');
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 2 KIND 2
    AntragBetreuungPO.createNewBetreuung(1);
    AntragBetreuungPO.fillOnlineKitaBetreuungsForm('withValid', gemeinde, {
        kita2: true
    });
    AntragBetreuungPO.selectAusserordentlicherBedarf('nein');
    AntragBetreuungPO.fillAuszahlungBeitraege();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    AntragBetreuungPO.getPageTitle().should('include.text', 'Betreuung');
    cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
        clickSave();
    });
}

function fillFamilienSituation() {
    cy.wait(5000);
    AntragFamSitPO.getPageTitle().should('have.text', 'Familiensituation');
    AntragFamSitPO.fillFamilienSituationFormLuzern('withValid');
    clickSave();
    GesuchstellendePO.fillVerheiratetFamFeutzLuzern('withValid');
    clickSave();
}

function fillBeschaeftigungspensum() {
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensumLuzern(
        'GS1',
        'withValid'
    );
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensumLuzern(
        'GS2',
        'withValid'
    );
    clickSave();
}

function fillFinSit() {
    // Config
    FinanzielleSituationStartPO.fillFinanzielleSituationStartFormFeutzLuzern(
        'withValid'
    );
    // Finanzielle Situation - GS 1
    FinanzielleSituationPO.fillFinanzielleSituationFormFeutzLuzern('withValid');
}

function changeEingangsDatum(antragIdAlias: string) {
    openGesuchInFamSit(antragIdAlias);
    cy.wait(1000);
    SidenavPO.goTo('GESUCH_ERSTELLEN');
    AntragCreationPO.getEingangsdatum().find('input').type('01.07.2023');
    AntragCreationPO.getAlternativdatum().find('input').type('01.07.2023');

    AntragCreationPO.getSpeichernUndWeiterButton().click();
}
