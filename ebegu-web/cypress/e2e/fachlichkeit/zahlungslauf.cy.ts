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

import {TestFaellePO} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {MANDANTS} from '@models/mandant';
import {ZahlungslaufPO} from '../../page-objects/antragverwaltung/zahlungslauf.po';

describe('Kibon - generate Testfälle [Superadmin]', () => {
    const adminUser = getUser('[1-Superadmin] Super User');
    const admintraegerschaftUser = getUser(
        '[3-Admin-Trägerschaft-Kitas-StadtBern] Bernhard Bern'
    );
    const adminGemeindeUser = getUser(
        '[6-P-Admin-Gemeinde] Gerlinde Hofstetter'
    );

    const testgemeindeSchwyz = 'Testgemeinde Schwyz';
    const testgemeindeLuzern = 'Testgemeinde Luzern';
    const testgemeindeSolothurn = 'Testgemeinde Solothurn';
    const testgemeindeAppenzellAusserrhoden =
        'Testgemeinde Appenzell Ausserrhoden';

    const zahlungBeschrieb = 'zahlungslauf';
    const institution = 'Weissenstein';
    const bestaetigt = 'Bestätigt';
    const bestaetigtLuzern = 'Platz bestätigt';

    // We expect that there is no open Zahlung
    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    it('should create a Zahlung Instiution and check its Values Schwyz', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.SCHWYZ, adminUser);
        ZahlungslaufPO.clearAndEditZahlungslaufAction(testgemeindeSchwyz);

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: testgemeindeSchwyz,
            periode: '2024/25',
            betreuungsstatus: 'verfuegt'
        });

        ZahlungslaufPO.userChangeAction(adminGemeindeUser);
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeSchwyz
        );
        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();

        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowInstitution(data);
            checkValuesOfZahlungInstitutionSchwyz(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', institution);
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.userChangeAction(admintraegerschaftUser);
        ZahlungslaufPO.zahlungAlsInstitutionBestaetigenAction(
            zahlungBeschrieb,
            institution,
            bestaetigt
        );

        ZahlungslaufPO.getDownloadExcel(institution);
        ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigt);

        ZahlungslaufPO.isZahlungBestaetigtAction(
            adminGemeindeUser,
            zahlungBeschrieb,
            bestaetigt
        );
    });

    it('should create a Zahlung Instiution and check its Values Luzern', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.LUZERN, adminUser);
        ZahlungslaufPO.clearAndEditZahlungslaufAction(testgemeindeLuzern);

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: testgemeindeLuzern,
            periode: '2024/25',
            betreuungsstatus: 'verfuegt'
        });

        ZahlungslaufPO.userChangeAction(adminGemeindeUser);
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeLuzern
        );
        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();

        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowInstitution(data);
            checkValuesOfZahlungInstitutionLuzern(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', institution);
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.userChangeAction(admintraegerschaftUser);
        ZahlungslaufPO.zahlungAlsInstitutionBestaetigenAction(
            zahlungBeschrieb,
            institution,
            bestaetigtLuzern
        );

        ZahlungslaufPO.getDownloadExcel(institution);
        ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigtLuzern);

        ZahlungslaufPO.isZahlungBestaetigtAction(
            adminGemeindeUser,
            zahlungBeschrieb,
            bestaetigtLuzern
        );
    });

    it('should create a Zahlung Instiution and check its Values Solothurn', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(
            MANDANTS.SOLOTHURN,
            adminUser
        );
        ZahlungslaufPO.clearAndEditZahlungslaufAction(testgemeindeSolothurn);

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: testgemeindeSolothurn,
            periode: '2024/25',
            betreuungsstatus: 'verfuegt'
        });

        ZahlungslaufPO.userChangeAction(adminGemeindeUser);
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeSolothurn
        );
        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();

        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowInstitution(data);
            checkValuesOfZahlungInstitutionSolothurn(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', institution);
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.userChangeAction(admintraegerschaftUser);
        ZahlungslaufPO.zahlungAlsInstitutionBestaetigenAction(
            zahlungBeschrieb,
            institution,
            bestaetigt
        );

        ZahlungslaufPO.getDownloadExcel(institution);
        ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigt);

        ZahlungslaufPO.isZahlungBestaetigtAction(
            adminGemeindeUser,
            zahlungBeschrieb,
            bestaetigt
        );
    });

    it('should create a Zahlung Instiution and check its Values Appenzell Ausserrhoden', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(
            MANDANTS.APPENZELL_AUSSERRHODEN,
            adminUser
        );
        ZahlungslaufPO.clearAndEditZahlungslaufAction(
            testgemeindeAppenzellAusserrhoden
        );

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: testgemeindeAppenzellAusserrhoden,
            periode: '2024/25',
            betreuungsstatus: 'verfuegt'
        });

        ZahlungslaufPO.userChangeAction(adminGemeindeUser);
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeAppenzellAusserrhoden
        );
        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();

        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowInstitution(data);
            checkValuesOfZahlungInstitutionAppenzellAusserrhoden(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', institution);
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.userChangeAction(admintraegerschaftUser);
        ZahlungslaufPO.zahlungAlsInstitutionBestaetigenAction(
            zahlungBeschrieb,
            institution,
            bestaetigt
        );

        ZahlungslaufPO.getDownloadExcel(institution);
        ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigt);

        ZahlungslaufPO.isZahlungBestaetigtAction(
            adminGemeindeUser,
            zahlungBeschrieb,
            bestaetigt
        );
    });

    it('should create a Zahlung Eltern and check its Values Luzern', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.LUZERN, adminUser);
        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.createZahlungTodayAction('clear', testgemeindeLuzern);
        ZahlungslaufPO.zahlungAusloesenAction();

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: testgemeindeLuzern,
            periode: '2024/25',
            betreuungsstatus: 'warten'
        });

        ZahlungslaufPO.zahlungAnElternAnfordernAction();

        ZahlungslaufPO.mandantAndUserChangeAction(
            MANDANTS.LUZERN,
            adminGemeindeUser
        );

        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeLuzern
        );

        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();
        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowElternLuzern(data);
            checkValuesOfZahlungElternLuzern(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', 'Dagmar Wälti');
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Ausgelöst');
    });
});

function checkFirstHeaderRow(data: any): void {
    // Check top header row
    expect(data[0][4]).to.eq('Auszahlung');
}

function checkSecondHeaderRowInstitution(data: any): void {
    // Check second header row
    expect(data[1][0]).to.eq('Name Institution');
    expect(data[1][1]).to.eq('ID Institution');
    expect(data[1][2]).to.eq('Angebot');
    expect(data[1][3]).to.eq('Trägerschaft');
    expect(data[1][4]).to.eq('Betrag');
    expect(data[1][5]).to.eq('IBAN-Nummer');
    expect(data[1][6]).to.eq('Kontoinhaber/in');
    expect(data[1][7]).to.eq('Anschrift');
    expect(data[1][8]).to.eq('Strasse');
    expect(data[1][9]).to.eq('Nr');
    expect(data[1][10]).to.eq('PLZ');
    expect(data[1][11]).to.eq('Ort');
}

function checkSecondHeaderRowElternLuzern(data: any): void {
    // Check second header row
    expect(data[1][0]).to.eq('Antragsteller/In');
    expect(data[1][1]).to.eq('Antragsteller/In 2');
    expect(data[1][4]).to.eq('Betrag');
    expect(data[1][5]).to.eq('IBAN-Nummer');
    expect(data[1][6]).to.eq('Kontoinhaber/in');
    expect(data[1][7]).to.eq('Anschrift');
    expect(data[1][8]).to.eq('Strasse');
    expect(data[1][9]).to.eq('Nr');
    expect(data[1][10]).to.eq('PLZ');
    expect(data[1][11]).to.eq('Ort');
}

function checkValuesOfZahlungInstitutionSchwyz(data: any): void {
    // Check Insitution Name
    expect(data[2][0]).to.eq('Weissenstein SZ');

    // Check Angebot Typ
    expect(data[2][2]).to.eq('Kita / schulische Tagesstruktur');

    // Check Traegerschaft Typ
    expect(data[2][3]).to.eq('Kitas & Tagis Kanton Schwyz');

    // Check Betrag
    expect(data[2][4]).to.eq(11007.6);

    // Check IBAN-Nummer
    expect(data[2][5]).to.eq('CH8209000000100150006');

    // Check Kontoinhaber/in
    expect(data[2][6]).to.eq('Kontoinhaber Weissenstein SZ');

    // Check Anschrift
    expect(data[2][7]).to.eq('Weissenstein Schwyz');

    // Check Strasse
    expect(data[2][8]).to.eq('Weberstrasse');

    // Check Strasse NR
    expect(data[2][9]).to.eq('5');

    // Check PLZ
    expect(data[2][10]).to.eq('4500');

    // Check Ort
    expect(data[2][11]).to.eq('Schwyz');
}

function checkValuesOfZahlungInstitutionLuzern(data: any): void {
    // Check Insitution Name
    expect(data[2][0]).to.eq('Weissenstein LU');

    // Check Angebot Typ
    expect(data[2][2]).to.eq('Tagesstätte für Kleinkinder');

    // Check Traegerschaft Typ
    expect(data[2][3]).to.eq('Kitas & Tagis Stadt Luzern');

    // Check Betrag
    expect(data[2][4]).to.eq(8143.2);

    // Check IBAN-Nummer
    expect(data[2][5]).to.eq('CH8209000000100150006');

    // Check Kontoinhaber/in
    expect(data[2][6]).to.eq('Kontoinhaber Weissenstein LU');

    // Check Anschrift
    expect(data[2][7]).to.eq('Weissenstein');

    // Check Strasse
    expect(data[2][8]).to.eq('Weberstrasse');

    // Check Strasse NR
    expect(data[2][9]).to.eq('5');

    // Check PLZ
    expect(data[2][10]).to.eq('6000');

    // Check Ort
    expect(data[2][11]).to.eq('Luzern');
}

function checkValuesOfZahlungInstitutionSolothurn(data: any): void {
    // Check Insitution Name
    expect(data[2][0]).to.eq('Weissenstein SO');

    // Check Angebot Typ
    expect(data[2][2]).to.eq('Tagesstätte für Kleinkinder');

    // Check Traegerschaft Typ
    expect(data[2][3]).to.eq('Kitas & Tagis Stadt Solothurn');

    // Check Betrag
    expect(data[2][4]).to.eq(8770.2);

    // Check IBAN-Nummer
    expect(data[2][5]).to.eq('CH8209000000100150006');

    // Check Kontoinhaber/in
    expect(data[2][6]).to.eq('Kontoinhaber Weissenstein SO');

    // Check Anschrift
    expect(data[2][7]).to.eq('Weissenstein Solothurn');

    // Check Strasse
    expect(data[2][8]).to.eq('Weberstrasse');

    // Check Strasse NR
    expect(data[2][9]).to.eq('5');

    // Check PLZ
    expect(data[2][10]).to.eq('4500');

    // Check Ort
    expect(data[2][11]).to.eq('Solothurn');
}

function checkValuesOfZahlungInstitutionAppenzellAusserrhoden(data: any): void {
    // Check Insitution Name
    expect(data[3][0]).to.eq('Weissenstein AR');

    // Check Angebot Typ
    expect(data[3][2]).to.eq('Tagesstätte für Kleinkinder');

    // Check Traegerschaft Typ
    expect(data[3][3]).to.eq('Kitas & Tagis Appenzell Ausserrhoden');

    // Check Betrag
    expect(data[3][4]).to.eq(7286.4);

    // Check IBAN-Nummer
    expect(data[3][5]).to.eq('CH8209000000100150006');

    // Check Kontoinhaber/in
    expect(data[3][6]).to.eq('Kontoinhaber Weissenstein AR');

    // Check Anschrift
    expect(data[3][7]).to.eq('Weissenstein Appenzell Ausserrhoden');

    // Check Strasse
    expect(data[3][8]).to.eq('Weberstrasse');

    // Check Strasse NR
    expect(data[3][9]).to.eq('5');

    // Check PLZ
    expect(data[3][10]).to.eq('9100');

    // Check Ort
    expect(data[3][11]).to.eq('Herisau');
}

function checkValuesOfZahlungElternLuzern(data: any): void {
    // Check Antragsteller/In
    expect(data[2][0]).to.eq('Dagmar Wälti');

    // Check Antragsteller/In 2
    expect(data[2][1]).to.eq('');

    // Check Betrag
    expect(data[2][4]).to.eq(15545.7);

    // Check IBAN-Nummer
    expect(data[2][5]).to.eq('CH9789144829733648596');

    // Check Kontoinhaber/in
    expect(data[2][6]).to.eq('kiBon Test');

    // Check Anschrift
    expect(data[2][7]).to.eq('');

    // Check Strasse
    expect(data[2][8]).to.eq('Testweg');

    // Check Strasse NR
    expect(data[2][9]).to.eq('10');

    // Check PLZ
    expect(data[2][10]).to.eq('3000');

    // Check Ort
    expect(data[2][11]).to.eq('Bern');
}
