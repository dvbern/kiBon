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

import {GemeindeTestFall, User} from '@dv-e2e/types';
import {KiBonMandant} from '@models/mandant';
import dayjs from 'dayjs';
import {GesuchPO} from '../antrag/gesuch.po';

const downloadsPath = Cypress.config('downloadsFolder');
const fileName = `Zahlungen_${dayjs().format('YYYY-MM-DD')}.xlsx`;
const dateToday = dayjs().format('DD.MM.YYYY');

const getZahlungErstellenButton = () => {
    return cy.getByData('container.senden', 'navigation-button');
};

const createZahlungTodayAction = (
    zahlungBeschrieb: string,
    gemeinde: string
) => {
    cy.getByData('gemeinde').select(gemeinde);
    cy.getByData('faelligkeitsdatum').type(dateToday);
    cy.getByData('generiertdatum').type(dateToday);
    cy.getByData('beschrieb').type(zahlungBeschrieb + ' ' + dateToday);
    ZahlungslaufPO.getZahlungErstellenButton().click();
    cy.getByData('remove-ok').click();
    cy.wait(1500);
};

const zahlungAusloesenAction = () => {
    cy.getByData('ausloesen', 'navigation-button').click();
    cy.getByData('remove-ok').click();
};

const userChangeAction = (user: User) => {
    cy.ignoreUncaughtException();
    cy.wait(1500);
    cy.login(user);
    cy.visit('/#/zahlungsauftrag');
    cy.wait(1500);
};

const mandantAndUserChangeAction = (mandant: KiBonMandant, user: User) => {
    cy.changeMandant(mandant);
    ZahlungslaufPO.userChangeAction(user);
};

const downloadExcelAndCheckValuesAction = () => {
    // let any re-render triggered by the preceding sort dblclick settle
    // before we try to click inside a row
    cy.get('mat-row', {timeout: 10000}).should('have.length.greaterThan', 0);
    cy.wait(500);

    getElementWithContent('mat-row', 'Entwurf')
        .findByData('ExcelDownloadButton')
        .should('be.visible')
        .as('excelDownloadButton');

    cy.getDownloadUrl(() => {
        cy.get('@excelDownloadButton').click();
    }).as('downloadUrl');

    cy.get<string>('@downloadUrl', {timeout: 15000}).then(url => {
        cy.log(`downloading ${url}`);
        cy.downloadFile(url, fileName).as('download');
    });

    cy.get('@download', {timeout: 15000}).should('not.equal', false);
    return cy.get<string>('@download').then(fileName =>
        cy.task(
            'convertXlsxToJson',
            {
                dirPath: downloadsPath,
                fileName,
                refs: 'A7:CL50000'
            },
            {custom: true}
        )
    );
};

const getDownloadExcel = (institution: string) => {
    cy.getDownloadUrl(() => {
        cy.contains('mat-row', institution)
            .findByData('ExcelDownloadButton')
            .should('be.visible')
            .click();
    }).as('downloadUrl');
    cy.get<string>('@downloadUrl', {timeout: 15000}).then(url => {
        cy.log(`downloading ${url}`);
        cy.downloadFile(url, fileName).as('download');
    });
};

const getElementWithContent = (selector: string, text: string) => {
    return cy
        .contains(selector, text, {timeout: 10000})
        .should('exist')
        .and('be.visible');
};

const zahlungAlsInstitutionBestaetigenAction = (
    zahlungBeschrieb: string,
    institution: string,
    bestaetigungText: string
) => {
    ZahlungslaufPO.getElementWithContent(
        '.mat-sort-header-container',
        'Generiert'
    ).dblclick();
    cy.wait(500);
    ZahlungslaufPO.getElementWithContent(
        'mat-cell',
        zahlungBeschrieb + ' ' + dateToday
    ).click();
    cy.wait(500);
    ZahlungslaufPO.getElementWithContent('mat-cell', institution).click();
    cy.wait(500);
    ZahlungslaufPO.getElementWithContent(
        'mat-cell',
        'Zahlung erhalten'
    ).click();
    cy.wait(500);
    cy.getByData('zahlungErhalten').eq(0).should('be.visible').click();
    ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigungText);
};

const goToZahlungsauftragPage = () => {
    return cy.visit('/#/zahlungsauftrag');
};

const mahlzeitenveruenstigungAnfordernAction = () => {
    cy.getByData('sidenav.FINANZIELLE_SITUATION').click();
    cy.getByData('keine-mahlzeitenverguenstigung-beantragen').click({
        force: true
    });
    cy.getByData('container.navigation-save').click();
    cy.wait(1500);
    cy.getByData('sidenav.BETREUUNG').click();
    cy.getByData('container.betreuung#0').click();
    cy.getByData('monatliche-hauptmahlzeiten#0').type('2000');
    cy.getByData('tarif-pro-hauptmahlzeit#0').type('200');
    ZahlungslaufPO.betreuungBestaetigenAction();
    cy.getByData('container.betreuung#1').click();
    ZahlungslaufPO.betreuungBestaetigenAction();
    ZahlungslaufPO.gesuchVerfueungZahlungAnElternAction();
    GesuchPO.betreuungVerfuegen(0, 0);
    GesuchPO.betreuungVerfuegen(0, 1);
};

const zahlungAnElternAnfordernAction = () => {
    cy.getByData('sidenav.BETREUUNG').click();
    ZahlungslaufPO.betreuungBestaetigenZahlungAnElternAction(
        'container.betreuung#0'
    );
    cy.wait(1500);
    ZahlungslaufPO.betreuungBestaetigenZahlungAnElternAction(
        'container.betreuung#1'
    );
    ZahlungslaufPO.gesuchVerfueungZahlungAnElternAction();
    GesuchPO.betreuungVerfuegen(0, 0);
    GesuchPO.betreuungVerfuegen(0, 1);
};

const betreuungBestaetigenZahlungAnElternAction = (betreuung: string) => {
    cy.getByData(betreuung).click();
    cy.getByData('abrechnungGutscheine.radio-value.ja').click();
    ZahlungslaufPO.betreuungBestaetigenAction();
};

const gesuchVerfueungZahlungAnElternAction = () => {
    cy.wait(1500);
    cy.getByData('sidenav.VERFUEGEN').click();
    cy.wait(1500);
    cy.getByData('finSitStatus.radio-value.AKZEPTIERT').click();
    cy.wait(1500);
    cy.getByData('container.geprueft').click();
    cy.wait(1500);
    cy.getByData('container.confirm').click();
    cy.wait(1500);
    cy.getByData('container.verfuegen').click();
    cy.wait(1500);
    cy.getByData('container.confirm').click();
    cy.wait(1500);
};

const editZahlungsauftragAction = () => {
    cy.wait(1500);
    cy.getByData('editZahlungsauftrag').should('be.visible').click();
    cy.getByData('zahlungsauftragBeschrieb').type(' edit');
    cy.getByData('saveZahlungsauftrag').click();
    ZahlungslaufPO.getElementWithContent(
        'mat-cell',
        'clear ' + dateToday + ' edit'
    );
};

const betreuungBestaetigenAction = () => {
    cy.getByData('korrekte-kosten-bestaetigung').click();
    cy.getByData('container.platz-bestaetigen').click();
    cy.go('back');
};

const clearAndEditZahlungslaufAction = (gemeinde: GemeindeTestFall) => {
    ZahlungslaufPO.createZahlungTodayAction('clear', gemeinde);
    ZahlungslaufPO.editZahlungsauftragAction();
    ZahlungslaufPO.zahlungAusloesenAction();
};

const isZahlungBestaetigtAction = (
    user: User,
    zahlungBeschrieb: string,
    bestaetigungText: string
) => {
    ZahlungslaufPO.userChangeAction(user);
    cy.ignoreUncaughtException();
    ZahlungslaufPO.getElementWithContent(
        '.mat-sort-header-container',
        'Generiert'
    ).dblclick();
    cy.wait(500);
    ZahlungslaufPO.getElementWithContent(
        'mat-cell',
        zahlungBeschrieb + ' ' + dateToday
    ).click();
    ZahlungslaufPO.getElementWithContent('mat-cell', bestaetigungText);
};

const waitForZahlungAusloesenAction = () => {
    cy.waitForRequest('PUT', '**/v1/zahlungen/ausloesen/**', () => {
        ZahlungslaufPO.zahlungAusloesenAction();
    });
};

const waitForRefreshAction = () => {
    cy.waitForRequest(
        'GET',
        '**/v1/zahlungen/**',
        () => {
            // Trigger a refresh by re-selecting the current filter value.
            cy.get('body').then($body => {
                if ($body.find('select[name="gemeindeFilter"]').length > 0) {
                    cy.get('select[name="gemeindeFilter"]').then($select => {
                        const currentValue = $select.val();
                        cy.get('select[name="gemeindeFilter"]').select(
                            currentValue as string,
                            {force: true}
                        );
                    });
                } else if (
                    $body.find('.dv-switch-animation-container').length > 0
                ) {
                    cy.get('.dv-switch-animation-container').first().click();
                    cy.wait(500);
                    cy.get('.dv-switch-animation-container').first().click();
                }
            });
        },
        {waitOptions: {timeout: 35000}}
    );
};

const getZahlungenSwitchButton = () => {
    return cy.get('.dv-switch-animation-container');
};

export const ZahlungslaufPO = {
    getZahlungErstellenButton,
    createZahlungTodayAction,
    zahlungAusloesenAction,
    userChangeAction,
    mandantAndUserChangeAction,
    downloadExcelAndCheckValuesAction,
    getDownloadExcel,
    getElementWithContent,
    zahlungAlsInstitutionBestaetigenAction,
    goToZahlungsauftragPage,
    mahlzeitenveruenstigungAnfordernAction,
    zahlungAnElternAnfordernAction,
    betreuungBestaetigenZahlungAnElternAction,
    gesuchVerfueungZahlungAnElternAction,
    editZahlungsauftragAction,
    betreuungBestaetigenAction,
    clearAndEditZahlungslaufAction,
    isZahlungBestaetigtAction,
    waitForZahlungAusloesenAction,
    waitForRefreshAction,
    getZahlungenSwitchButton
};
