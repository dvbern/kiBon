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
    FixtureFamSitFeutzAppenzell,
    FixtureFinSitFeutzAppenzell
} from '@dv-e2e/fixtures';
import {
    AntragBeschaeftigungspensumPO,
    AntragBetreuungPO,
    AntragCreationPO,
    AntragFamSitPO,
    AntragKindPO,
    ConfirmDialogPO,
    DokumentePO,
    EinkommensverschlechterungInfoPO,
    EinkommensverschlechterungPO,
    EinkommensverschlechterungResultatePO,
    FinanzielleSituationPO,
    FinanzielleSituationResultatePO,
    FreigabePO,
    GesuchstellendeDashboardPO,
    NavigationPO,
    RegistrationAbschliessenPO,
    TestFaellePO,
    VerfuegenPO,
    VerfuegungPO
} from '@dv-e2e/page-objects';
import {getUser, TestPeriode, User} from '@dv-e2e/types';
import {GesuchstellendePO} from '../../page-objects/antrag/gesuchstellende.po';
import {SidenavPO} from '@dv-e2e/page-objects';
import {MANDANTS} from '@kibon/shared-model-mandant';

describe('Appenzell - Happy Path Gesuch', () => {
    const userSuperadmin = getUser('[1-Superadmin] Super User');
    const userGemeinde = getUser('[6-P-SB-Gemeinde] Stefan Wirth');
    const userTraegerschaft = getUser(
        '[3-SB-Trägerschaft-Kitas-StadtBern] Agnes Krause'
    );
    const userGS = getUser('[5-GS] Emma Gerber');
    const gesuchsPeriode: {ganze: TestPeriode; anfang: string; ende: string} = {
        ganze: '2023/24',
        anfang: '2023',
        ende: '2024'
    };

    beforeEach(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    before(() => {
        cy.ignoreUncaughtException();
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
        cy.changeMandant(MANDANTS.APPENZELL_AUSSERRHODEN);
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

    it('should register new user for bg', () => {
        register(userGS);
    });

    it('run Happy Path Appenzell Gesuch', () => {
        // GESUCH STELLEN
        register(userGS);
        cy.wait(1000);
        openAntrag(userGS, gesuchsPeriode);
        cy.wait(1000);
        clickSave();

        // Familiensituation
        fillFamilienSituation();

        // Kinder
        createKinder();

        // Betreuung
        createBetreuungen('withValid');

        // BESCHAEFTIGUNGSPENSUM
        fillBeschaeftigungspensum();

        // FINANZIELLE VERHAELTNISSE
        fillFinSit();

        // Resultate
        checkFinSitResults();

        // EINKOMMENSVERSCHLECHTERUNG
        fillEinkommensverschlechterung(gesuchsPeriode, 'withValid');

        // Resultate
        checkEinkommensverschlechterungResults(gesuchsPeriode);

        // DOKUMENTE
        uploadDummyDocuments();

        // PLATZBESTAETIGUNG mit Kita SB
        bestaetigePlatzAsKita(userGS, userTraegerschaft, gesuchsPeriode);

        // FREIGABE
        startFreigabe();

        // VERFUEGUNG
        startVerfuegenAppenzell(userSuperadmin, userGemeinde);
    });
});

function register(user: User) {
    cy.login(user);
    cy.visit('/#/registration-abschliessen');
    cy.wait(1500);
    RegistrationAbschliessenPO.getBGBeantragen().click();
    RegistrationAbschliessenPO.getGemeindeSelection().select(
        'Testgemeinde Appenzell Ausserrhoden'
    );

    RegistrationAbschliessenPO.getRegistrierenButton().click();
    cy.waitForRequest('GET', '**/gemeinde/gemeindeRegistrierung/**', () => {
        RegistrationAbschliessenPO.getConfirmPopUpRegistrationButton().click();
    });
    cy.wait(1500);
    RegistrationAbschliessenPO.getRegistrierungAbschliessenButton().click();
    cy.wait(1500);
}

function openAntrag(
    userGS: User,
    gesuchsPeriode: {ganze: TestPeriode; anfang: string; ende: string}
) {
    cy.login(userGS);
    cy.waitForRequest('GET', '**/gesuchsperioden/aktive/gemeinde/*', () => {
        cy.visit('/#/dossier/gesuchstellerDashboard');
    });
    cy.wait(2000);
    AntragCreationPO.getAntragBearbeitenButton().should(
        'include.text',
        'Antrag'
    );
    GesuchstellendeDashboardPO.getAntragBearbeitenButton(
        gesuchsPeriode.ganze
    ).click();
}

function fillFamilienSituation() {
    AntragFamSitPO.getPageTitle().should('have.text', 'Familiensituation');
    SidenavPO.getGesuchStatus().should(
        'have.text',
        'In Bearbeitung Antragsteller/in'
    );
    cy.url()
        .then(url => /familiensituation-ar\/(.*)$/.exec(url)[1])
        .as('antragsId');
    AntragFamSitPO.fillFamiliensituationFormAppenzell('withValid');
    clickSave();
    GesuchstellendePO.fillAntragsstellendeAppenzell('withValid');
    clickSave();
}

function createKinder() {
    //Kinder
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFeutzAppenzell('withValidBoy');
    clickSave();
    AntragKindPO.createNewKind();
    AntragKindPO.fillKindFormFeutzAppenzell('withValidGirl');
    clickSave();
    AntragKindPO.getPageTitle().should('include.text', 'Kinder');
    SidenavPO.getGesuchStatus().should(
        'have.text',
        'In Bearbeitung Antragsteller/in'
    );
    cy.waitForRequest('POST', '**/wizard-steps', () => {
        clickSave();
    });
}

function createBetreuungen(famsitDataset: 'withValid') {
    //KITA 1 KIND 1
    AntragBetreuungPO.createNewBetreuung(0);
    AntragBetreuungPO.fillOnlineKitaBetreuungsFormAppenzell(
        famsitDataset,
        'Testgemeinde Appenzell Ausserrhoden'
    );
    AntragBetreuungPO.getHasErweiterteBeduerfnisse('nein').click();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 2 KIND 1
    AntragBetreuungPO.createNewBetreuung(0);
    AntragBetreuungPO.fillOnlineKitaBetreuungsFormAppenzell(
        famsitDataset,
        'Testgemeinde Appenzell Ausserrhoden',
        {kita2: true}
    );
    AntragBetreuungPO.getHasErweiterteBeduerfnisse('nein').click();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 1 KIND 2
    AntragBetreuungPO.createNewBetreuung(1);
    AntragBetreuungPO.fillOnlineKitaBetreuungsFormAppenzell(
        famsitDataset,
        'Testgemeinde Appenzell Ausserrhoden'
    );
    AntragBetreuungPO.getHasErweiterteBeduerfnisse('nein').click();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    //KITA 2 KIND 2
    AntragBetreuungPO.createNewBetreuung(1);
    AntragBetreuungPO.fillOnlineKitaBetreuungsFormAppenzell(
        famsitDataset,
        'Testgemeinde Appenzell Ausserrhoden',
        {kita2: true}
    );
    AntragBetreuungPO.getHasErweiterteBeduerfnisse('nein').click();
    AntragBetreuungPO.platzBestaetigungAnfordern();
    cy.wait(1500);

    AntragBetreuungPO.getPageTitle().should('include.text', 'Betreuung');
    SidenavPO.getGesuchStatus().should(
        'have.text',
        'Warten auf Platzbestätigung'
    );
    cy.waitForRequest('GET', '**/erwerbspensen/required/**', () => {
        clickSave();
    });
}

function fillBeschaeftigungspensum() {
    AntragBeschaeftigungspensumPO.getPageTitle().should(
        'include.text',
        'Beschäftigungspensum'
    );
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensum(
        'GS1',
        'withValid'
    );
    AntragBeschaeftigungspensumPO.createBeschaeftigungspensum(
        'GS2',
        'withValid'
    );
    clickSave();
}

function fillFinSit() {
    FinanzielleSituationPO.getPageTitle().should(
        'have.text',
        'Berechnung der finanziellen Verhältnisse'
    );
    // Finanzielle Situation - GS 1
    FinanzielleSituationPO.getSteuerveranlagungGemeinsam('false').click();
    FinanzielleSituationPO.fillFinanzielleSituationFormAppenzell(
        'withValid',
        'GS1'
    );
    FinanzielleSituationPO.saveFormAndGoNext();

    // Finanzielle Situation - GS 2
    FinanzielleSituationPO.fillFinanzielleSituationFormAppenzell(
        'withValid',
        'GS2'
    );
}

function checkFinSitResults() {
    FixtureFinSitFeutzAppenzell.withValid(({Resultate}) => {
        FinanzielleSituationResultatePO.getMassgebendesEinkVorAbzFamGrGS1()
            .find('input')
            .should('have.value', Resultate.massgebendesEinkommenGS1);
        FinanzielleSituationResultatePO.getMassgebendesEinkVorAbzFamGrGS2()
            .find('input')
            .should('have.value', Resultate.massgebendesEinkommenGS2);
        FinanzielleSituationResultatePO.getMassgebendesEinkVorAbzFamGr()
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

function fillEinkommensverschlechterung(
    gesuchsPeriode: {ganze: TestPeriode; anfang: string; ende: string},
    famsitDataset: 'withValid'
) {
    EinkommensverschlechterungInfoPO.fillEinkommensverschlechterungInfoForm(
        'withValid'
    );
    cy.waitForRequest(
        'POST',
        '**/einkommensverschlechterung/calculateTemp/1',
        () => {
            clickSave();
        }
    );
    cy.groupBy('Einkommensverschlechterung - Jahr 1', () => {
        EinkommensverschlechterungPO.getPageTitle().should(
            'include.text',
            gesuchsPeriode.anfang
        );
        EinkommensverschlechterungPO.fillEinkommensverschlechterungFormAppenzell(
            'withValid',
            'jahr1',
            'GS1'
        );
        clickSave();
        FixtureFamSitFeutzAppenzell[famsitDataset](({GS2}) => {
            EinkommensverschlechterungPO.getPageTitle().should(
                'include.text',
                `${GS2.vorname} ${GS2.nachname}`
            );
        });
        EinkommensverschlechterungPO.fillEinkommensverschlechterungFormAppenzell(
            'withValid',
            'jahr1',
            'GS2'
        );
        clickSave();
    });
    cy.groupBy('Einkommensverschlechterung - Jahr 2', () => {
        EinkommensverschlechterungPO.getPageTitle().should(
            'include.text',
            gesuchsPeriode.ende
        );
        EinkommensverschlechterungPO.fillEinkommensverschlechterungFormAppenzell(
            'withValid',
            'jahr2',
            'GS1'
        );
        clickSave();
        FixtureFamSitFeutzAppenzell[famsitDataset](({GS2}) => {
            EinkommensverschlechterungPO.getPageTitle().should(
                'include.text',
                `${GS2.vorname} ${GS2.nachname}`
            );
        });
        EinkommensverschlechterungPO.fillEinkommensverschlechterungFormAppenzell(
            'withValid',
            'jahr2',
            'GS2'
        );
        clickSave();
    });
}

function checkEinkommensverschlechterungResults(gesuchsPeriode: {
    ganze: TestPeriode;
    anfang: string;
    ende: string;
}) {
    cy.groupBy('Resultate', () => {
        EinkommensverschlechterungResultatePO.getPageTitle().should(
            'include.text',
            gesuchsPeriode.anfang
        );
        checkResultateForm();
        clickSave();

        EinkommensverschlechterungPO.getPageTitle().should(
            'include.text',
            gesuchsPeriode.ende
        );
        checkResultateForm();
    });
    cy.waitForRequest('GET', '**/dokumente/**', () => {
        clickSave();
    });
}

function uploadDummyDocuments() {
    DokumentePO.getPageTitle().should('have.text', 'Dokumente');
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

function bestaetigePlatzAsKita(
    userGS: User,
    userTraegerschaft: User,
    gesuchsPeriode: {
        ganze: TestPeriode;
        anfang: string;
        ende: string;
    }
) {
    cy.changeLogin(userTraegerschaft);

    const goToBetreuungen = () => {
        cy.get('@antragsId').then(antragsId =>
            cy.visit(`/#/gesuch/familiensituation/${antragsId}`)
        );

        cy.waitForRequest(
            'GET',
            '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**',
            () => {
                SidenavPO.goTo('BETREUUNG');
            }
        );
    };

    goToBetreuungen();
    AntragBetreuungPO.getBetreuung(0, 0).click();
    AntragBetreuungPO.fillKitaBetreuungspensumForm(
        'withValid',
        'Testgemeinde Appenzell Ausserrhoden'
    );
    AntragBetreuungPO.platzBestaetigen();
    goToBetreuungen();

    AntragBetreuungPO.getBetreuung(0, 1).click();
    AntragBetreuungPO.fillKitaBetreuungspensumForm(
        'withValid',
        'Testgemeinde Appenzell Ausserrhoden'
    );
    AntragBetreuungPO.platzBestaetigen();
    goToBetreuungen();

    cy.waitForRequest('GET', '**/fachstellen/erweiterteBetreuung', () => {
        AntragBetreuungPO.getBetreuung(1, 0).click();
    });

    AntragBetreuungPO.platzAbweisen(
        'Ein sehr legitimer Grund der hier nicht weiter aufgeführt wird.'
    );
    goToBetreuungen();

    AntragBetreuungPO.getBetreuung(1, 1).click();
    AntragBetreuungPO.fillKitaBetreuungspensumForm(
        'withValid',
        'Testgemeinde Appenzell Ausserrhoden'
    );
    AntragBetreuungPO.platzBestaetigen();

    cy.changeLogin(userGS);
    openAntrag(userGS, gesuchsPeriode);
    // !!!!!! - changed back to previous user - !!!!!!
}

function startFreigabe() {
    cy.waitForRequest(
        'GET',
        '**/einstellung/key/FINANZIELLE_SITUATION_TYP/gemeinde/**',
        () => {
            SidenavPO.goTo('BETREUUNG');
        }
    );
    AntragBetreuungPO.getBetreuungsstatus(1, 0).should(
        'include.text',
        'Abgewiesen'
    );
    AntragBetreuungPO.getBetreuungLoeschenButton(1, 0).click();
    cy.waitForRequest('DELETE', '**/betreuungen/**', () => {
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    SidenavPO.goTo('FREIGABE');
    FreigabePO.getFreigebenButton().click();
    cy.getDownloadUrl(() => {
        cy.waitForRequest('GET', '**/dossier/fall/**', () => {
            ConfirmDialogPO.getDvLoadingConfirmButton().click();
        });
    }).then(downloadUrl => {
        return cy
            .request(downloadUrl)
            .then(response =>
                expect(response.headers['content-disposition']).to.match(
                    /Freigabequittung_.*\.pdf/
                )
            );
    });
}

function startVerfuegenAppenzell(userSuperadmin: User, userGemeinde: User) {
    cy.changeLogin(userSuperadmin);
    cy.get('@antragsId').then(antragsId =>
        cy.visit(`/#/gesuch/freigabe/${antragsId}`)
    );
    cy.waitForRequest('GET', '**/dossier/fall/**', () => {
        FreigabePO.getFreigabequittungEinscannenSimulierenButton().click();
    });
    // attempt to reduce flakyness
    cy.wait(2000);
    SidenavPO.goTo('GESUCH_ERSTELLEN');
    AntragCreationPO.getEingangsdatum()
        .find('input')
        .clear()
        .type('01.07.2023');
    cy.waitForRequest('PUT', '**/gesuche', () => {
        clickSave();
    });

    cy.changeLogin(userGemeinde);
    cy.get('@antragsId').then(antragsId =>
        cy.visit(`/#/gesuch/freigabe/${antragsId}`)
    );
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

    // KIND 1 BETREUUNG 1
    VerfuegenPO.getVerfuegung(0, 0).click();
    VerfuegenPO.getAuszahlungAnInstitution(0).should('include.text', '40.25');
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    VerfuegungPO.getVerfuegenButton().click();
    ConfirmDialogPO.getDvLoadingConfirmButton().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });

    // KIND 1 BETREUUNG 2
    VerfuegenPO.getVerfuegung(0, 1).click();
    VerfuegenPO.getAuszahlungAnInstitution(0).should('include.text', '40.25');
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    VerfuegungPO.getVerfuegenButton().click();
    ConfirmDialogPO.getDvLoadingConfirmButton().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });

    // KIND 2 BETREUUNG 1
    VerfuegenPO.getVerfuegung(1, 0).click();
    VerfuegenPO.getAuszahlungAnInstitution(0).should('include.text', '40.25');
    VerfuegungPO.getVerfuegungsBemerkungenKontrolliert().click();
    VerfuegungPO.getVerfuegenButton().click();
    ConfirmDialogPO.getDvLoadingConfirmButton().click();
    cy.waitForRequest('PUT', '**/verfuegung/verfuegen/**', () => {
        SidenavPO.goTo('VERFUEGEN');
    });

    SidenavPO.getGesuchStatus().should('have.text', 'Verfügt');
}

function clickSave() {
    NavigationPO.getSaveAndNextButton().should(
        'not.have.a.property',
        'disabled'
    );
    cy.wait(1500);
    NavigationPO.saveAndGoNext();
}

function checkResultateForm() {
    EinkommensverschlechterungResultatePO.checkResultsAppenzell();
}
