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
    const adminGemeindeUserLondon = getUser(
        '[6-L-Admin-Gemeinde] Gerlinde Bader'
    );
    const adminGemeindeUser = getUser(
        '[6-P-Admin-Gemeinde] Gerlinde Hofstetter'
    );

    const testgemeindeLondon = 'London';
    const testgemeindeParis = 'Paris';
    const testgemeindeSchwyz = 'Testgemeinde Schwyz';

    const zahlungBeschrieb = 'zahlungslauf';
    const institution = 'Weissenstein';
    const bestaetigt = 'Bestätigt';

    // It's expect that there is no open Zahlung
    beforeEach(() => {
        cy.intercept({resourceType: 'xhr'}, {log: false}); // don't log XHRs
    });

    it('should create a Zahlung Institution and check its Values Bern', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.BERN, adminUser);
        ZahlungslaufPO.clearAndEditZahlungslaufAction(testgemeindeLondon);

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: testgemeindeLondon,
            periode: '2024/25',
            betreuungsstatus: 'verfuegt'
        });

        ZahlungslaufPO.userChangeAction(adminGemeindeUserLondon);
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeLondon
        );
        ZahlungslaufPO.waitForRefreshAction();
        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();

        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowInstitution(data);
            checkValuesOfZahlungInstitutionBern(data);
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
        cy.wait(500);

        ZahlungslaufPO.isZahlungBestaetigtAction(
            adminGemeindeUserLondon,
            zahlungBeschrieb,
            bestaetigt
        );
    });

    it('should create a Zahlung Mahlzeitenverguenstigung and check its Values Bern', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.BERN, adminUser);
        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.clearAndEditZahlungslaufAction(testgemeindeParis);

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: testgemeindeParis,
            periode: '2024/25',
            betreuungsstatus: 'warten'
        });

        ZahlungslaufPO.mahlzeitenveruenstigungAnfordernAction();

        ZahlungslaufPO.mandantAndUserChangeAction(
            MANDANTS.BERN,
            adminGemeindeUser
        );

        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeParis
        );
        ZahlungslaufPO.waitForRefreshAction();

        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();
        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowMahlzeitenVerguenstigung(data);
            checkValuesOfZahlungMahlzeitenverguenstigungBern(data);
        });

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Entwurf').click();
        ZahlungslaufPO.getElementWithContent('mat-cell', 'Dagmar Wälti');
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.goToZahlungsauftragPage();
        ZahlungslaufPO.waitForZahlungAusloesenAction();

        ZahlungslaufPO.getElementWithContent('mat-cell', 'Ausgelöst');
    });

    it('should create a Zahlung Eltern and check its Values Schwyz', () => {
        ZahlungslaufPO.mandantAndUserChangeAction(MANDANTS.SCHWYZ, adminUser);
        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.createZahlungTodayAction('clear', testgemeindeSchwyz);
        ZahlungslaufPO.waitForRefreshAction();
        ZahlungslaufPO.zahlungAusloesenAction();

        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-1',
            gemeinde: testgemeindeSchwyz,
            periode: '2024/25',
            betreuungsstatus: 'warten'
        });

        ZahlungslaufPO.zahlungAnElternAnfordernAction();

        ZahlungslaufPO.mandantAndUserChangeAction(
            MANDANTS.SCHWYZ,
            adminGemeindeUser
        );

        ZahlungslaufPO.getZahlungenSwitchButton().click();
        ZahlungslaufPO.createZahlungTodayAction(
            zahlungBeschrieb,
            testgemeindeSchwyz
        );
        ZahlungslaufPO.waitForRefreshAction();

        ZahlungslaufPO.getElementWithContent(
            '.mat-sort-header-container',
            'Status'
        ).dblclick();
        ZahlungslaufPO.downloadExcelAndCheckValuesAction().then(([{data}]) => {
            checkFirstHeaderRow(data);
            checkSecondHeaderRowElternSchwyz(data);
            checkValuesOfZahlungElternSchwyz(data);
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

function checkSecondHeaderRowMahlzeitenVerguenstigung(data: any): void {
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

function checkSecondHeaderRowElternSchwyz(data: any): void {
    // Check second header row
    expect(data[1][0]).to.eq('Gesuchsteller/In');
    expect(data[1][1]).to.eq('Gesuchsteller/In 2');
    expect(data[1][4]).to.eq('Betrag');
    expect(data[1][5]).to.eq('IBAN-Nummer');
    expect(data[1][6]).to.eq('Kontoinhaber/in');
    expect(data[1][7]).to.eq('Anschrift');
    expect(data[1][8]).to.eq('Strasse');
    expect(data[1][9]).to.eq('Nr');
    expect(data[1][10]).to.eq('PLZ');
    expect(data[1][11]).to.eq('Ort');
}

function checkValuesOfZahlungInstitutionBern(data: any): void {
    // Check Insitution Name
    expect(data[2][0]).to.eq('Weissenstein');

    // Check Angebot Typ
    expect(data[2][2]).to.eq('Kita');

    // Check Traegerschaft Typ
    expect(data[2][3]).to.eq('Kitas & Tagis Stadt Bern');

    // Check Betrag
    expect(data[2][4]).to.eq(14391);

    // Check IBAN-Nummer
    expect(data[2][5]).to.eq('CH8209000000100150006');

    // Check Kontoinhaber/in
    expect(data[2][6]).to.eq('Kontoinhaber Weissenstein');

    // Check Anschrift
    expect(data[2][7]).to.eq('Weissenstein');

    // Check Strasse
    expect(data[2][8]).to.eq('Weberstrasse');

    // Check Strasse NR
    expect(data[2][9]).to.eq('5');

    // Check PLZ
    expect(data[2][10]).to.eq('3007');

    // Check Ort
    expect(data[2][11]).to.eq('Bern');
}

function checkValuesOfZahlungMahlzeitenverguenstigungBern(data: any): void {
    // Check Antragsteller/In
    expect(data[2][0]).to.eq('Dagmar Wälti');

    // Check Antragsteller/In 2
    expect(data[2][1]).to.eq('');

    // Check Betrag
    expect(data[2][4]).to.eq(288);

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

function checkValuesOfZahlungElternSchwyz(data: any): void {
    // Check Antragsteller/In
    expect(data[2][0]).to.eq('Dagmar Wälti');

    // Check Antragsteller/In 2
    expect(data[2][1]).to.eq('');

    // Check Betrag
    expect(data[2][4]).to.eq(13968);

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
