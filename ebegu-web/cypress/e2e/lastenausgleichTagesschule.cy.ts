import {
    AntragBetreuungPO,
    ConfirmDialogPO,
    FreigabePO,
    SidenavPO,
    TestFaellePO
} from '@dv-e2e/page-objects';
import {getUser} from '@dv-e2e/types';
import {MANDANTS} from '../../libs/shared/model/shared-model-mandant/src/lib/MANDANTS';
import {GemeindeAntraegePO} from '../page-objects/gemeindeantraege';
import {LastenausgleichTagesschulePo} from '../page-objects/gemeindeantraege/lastenausgleichTagesschule.po';

describe('Kibon - Lastenausgleich Tagesschule', () => {
    const adminUser = getUser('[1-Superadmin] Super User');
    const userSBMandant = getUser('[2-SB-Kanton-Bern] Benno Röthlisberger');
    const userSBGemeinde = getUser('[6-P-SB-Gemeinde] Stefan Wirth');
    const userSBInstitution = getUser('[3-SB-TS-Paris] Charlotte Gainsbourg');

    before(() => {
        cy.changeMandant(MANDANTS.BERN);
        cy.intercept({resourceType: 'xhr'}, {log: true}); // don't log XHRs
    });

    it('should delete LATS for current Periode', () => {
        cy.login(adminUser);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.getLatestPeriod();
        GemeindeAntraegePO.selectCreateGemeindeAntragTyp(
            'LASTENAUSGLEICH_TAGESSCHULEN'
        );
        cy.getByData('gemeindeantraege-antraege-loeschen').click();
        cy.waitForRequest(
            'DELETE',
            '**/gemeindeantrag/deleteAntraege/**',
            () => {
                cy.getByData('remove-ok').click();
            }
        );
    });

    it('should create new LATS for current Periode', () => {
        cy.login(userSBMandant);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.getLatestPeriod();
        GemeindeAntraegePO.selectCreateGemeindeAntragTyp(
            'LASTENAUSGLEICH_TAGESSCHULEN'
        );
        cy.getByData('gemeindeantraege-antraege-erstellen').click();
        GemeindeAntraegePO.getLATSGemeindeCheckbox('Paris').click();
        cy.waitForRequest(
            'POST',
            '**/gemeindeantrag/createAllAntraege/LASTENAUSGLEICH_TAGESSCHULEN/**',
            () => {
                cy.getByData('LATS-erstellen-Ok').click();
            }
        );
    });

    it('should check details of LATS', () => {
        cy.login(userSBGemeinde);
        cy.visit('/#/gemeinde-antraege');
        cy.getByData('gemeindeantraege-typ-tabelle').click();
        cy.getByData('LASTENAUSGLEICH_TAGESSCHULEN').click();
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('angaben-gemeinde-nein').click();
        cy.wait(1500);
        cy.getByData('gemeinde-antraege-antrag-erstellen').click();
        LastenausgleichTagesschulePo.checkSideNavStatus(
            'FREIGABE',
            'be.disabled'
        );
        cy.getByData('LATS-sidenav').should('have.length', 3);
        LastenausgleichTagesschulePo.checkSideNavStatus(
            'LASTENAUSGLEICH',
            'not.exist'
        );
    });

    it('should create Gemeinde antrag', () => {
        cy.login(userSBInstitution);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('institutionName,status').click();
        cy.getByData('anzahl-kinder-woche').clear().type('17');
        cy.getByData('anzahl-kinder-basisstufe-woche').clear().type('3');
        cy.getByData('anzahl-kinder-kindergarten-woche').clear().type('6');
        cy.getByData('anzahl-kinder-primar-woche').clear().type('4');
        cy.getByData('anzahl-kinder-sekundar-woche').clear().type('4');
        cy.getByData('anzahl-kinder-besonders-woche').clear().type('1');
        cy.getByData('anzahl-kinder-volksschule-woche').clear().type('2');
        cy.getByData('anzahl-kinder-fruehbetreuung').clear().type('8');
        cy.getByData('anzahl-kinder-mittagsbetreuung').clear().type('6');
        cy.getByData('anzahl-kinder-nachmittagsbetreuung-eins')
            .clear()
            .type('7');
        cy.getByData('tagesschule-gemeindeantrag-speichern').click();
        cy.getByData('tagesschule-freigeben-gemeindeantrag').click();
        SidenavPO.getLATSSidenavStepStatus('ANGABEN_TAGESSCHULEN').should(
            'have.class',
            'fa-pencil'
        );
        cy.url().should('include', '/angaben-tagesschulen/');
        cy.getByData('tagesschule-kein-lehrbetrieb-schuljahr')
            .contains('Nein')
            .click({force: true});
        cy.getByData('anzahl-kinder-nachmittagsbetreuung-zwei')
            .clear()
            .type('10');
    });

    it('should change Tagesschulanmeldung frage', () => {
        cy.login(adminUser);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('tagesschulanmeldungen-erfassen')
            .contains('Ja')
            .click({force: true});
        cy.getByData('angaben-gemeindeantrag-speichern').click();
    });

    it('should create testdaten', () => {
        cy.login(adminUser);
        cy.visit('/#/faelle');
        TestFaellePO.createPapierTestfall({
            testFall: 'testfall-2',
            gemeinde: 'Paris',
            periode: '2024/25',
            betreuungsstatus: 'warten'
        });
        SidenavPO.goTo('BETREUUNG');
        AntragBetreuungPO.getBetreuungErstellenButton(0).click();
        AntragBetreuungPO.selectTagesschulBetreuung();
        cy.getByData('institution').click().type('Par');
        cy.getByData('institutions-suchtext').first().click();
        cy.getByData('keineKesbPlatzierung.radio-value.nein').click();
        cy.getByData('modul-Morgen-MONDAY').click();
        cy.getByData('agb-tsakzeptiert').click();
        cy.getByData('container.save').click();
        cy.getByData('container.confirm').click();
        cy.getByData('container.betreuung#1').click();
        cy.getByData('container.akzeptieren').click();
        cy.getByData('container.confirm').click();
        AntragBetreuungPO.getBetreuungLoeschenButton(0, 0).click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
        cy.wait(1500);
        AntragBetreuungPO.getBetreuungLoeschenButton(1, 0).click();
        ConfirmDialogPO.getDvLoadingConfirmButton().click();
    });

    it('should aus kibon berechnen', () => {
        cy.login(userSBInstitution);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('institutionName,status').click();
        cy.getByData('lats-durch-kibon-ausfuellen').click();
        FreigabePO.getConfirmButton().click();
        cy.getByData('tagesschule-kein-lehrbetrieb-schuljahr')
            .contains('Nein')
            .click({force: true});
        cy.getByData('betreuungsstunden-schuljahr-inkl-kinder')
            .clear()
            .type('15');
        cy.getByData('tagesschul-basis-organisatorisches-konzept')
            .contains('Nein')
            .click({force: true});
        cy.getByData('tagesschule-basis-paedagogisches-konzept')
            .contains('Nein')
            .click({force: true});
        cy.getByData('tagesschule-raum-vorschriften-erfuellt')
            .contains('Ja')
            .click({force: true});
        cy.getByData('tagesschule-betreuungsverhaeltnis')
            .contains('Ja')
            .click({force: true});
        cy.getByData('tagesschule-ausgewogene-erhaehrung')
            .contains('Ja')
            .click({force: true});
        cy.wait(1500);
        cy.getByData('tagesschule-freigeben-gemeindeantrag').click();
        cy.wait(1500);
        cy.getByData('container.confirm').click();
        SidenavPO.getLATSSidenavStepStatus('ANGABEN_TAGESSCHULEN').should(
            'have.class',
            'fa-check'
        );
    });

    it('should check tagesschule formular', () => {
        cy.login(userSBGemeinde);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        LastenausgleichTagesschulePo.getSideNav('ANGABEN_TAGESSCHULEN');
        cy.getByData('institutionName,status').contains('In Prüfung');
        cy.getByData('institutionName,status').click();
        GemeindeAntraegePO.checkInstitutionValue(
            '5',
            'anzahl-kinder-fruehbetreuung',
            'anzahl-kinder-woche-bisher'
        );
        cy.getByData('tagesschule-gemeindeantrag-speichern').click();
        cy.getByData('tagesschule-gemeindeantrag-geprueft').click();
        cy.getByData('container.confirm').click();
        cy.getByData('institutionName,status').click();
        cy.getByData('anzahl-kinder-fruehbetreuung').should('be.disabled');
        cy.getByData('tagesschule-angaben-korrigieren').click();
        cy.getByData('anzahl-kinder-fruehbetreuung').should('be.enabled');
        cy.getByData('tagesschule-gemeindeantrag-geprueft').click();
        cy.wait(1500);
        cy.getByData('container.confirm').click();
        cy.wait(1500);
    });

    it('should fill out gemeinde antrag', () => {
        cy.login(userSBGemeinde);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('bedarf-tagesschulangebot-abeklaert')
            .contains('Ja')
            .click({force: true});
        cy.getByData('angebot-schullkinder-betreuung-ferien')
            .contains('Nein')
            .click({force: true});
        cy.getByData('tagesschulangebot-steht-allen-offen')
            .contains('Ja')
            .click({force: true});
        cy.getByData('geleistete-betreuungsstunden').clear().type('5');
        cy.getByData(
            'geleistete-betreuungsstunden-besondere-betreuungsanforderungen'
        )
            .clear()
            .type('5');
        cy.getByData('geleistete-betreuungsstunden-besondere-volksschulangebot')
            .clear()
            .type('5');
        cy.getByData('angaben-gemeindeantrag-speichern').click();
        cy.getByData('angaben-gemeinde-abschliessen').click();
        SidenavPO.getLATSSidenavStepStatus('ANGABEN_GEMEINDE').should(
            'have.class',
            'fa-pencil'
        );
        cy.getByData('normlohnkosten-sozialpaedagogisch-ueber-50-prozent')
            .clear()
            .type('10');
        cy.getByData('normlohnkosten-sozialpaedagogisch-unter-50-prozent')
            .clear()
            .type('5');
        cy.getByData('einnahmen-elterngebuehren-ohne-verpflegung')
            .clear()
            .type('7000');
        cy.getByData('erste-rate-dezember').clear().type('3000');
        cy.getByData('gesamtkosten-tagesschule-jahressrechnung')
            .clear()
            .type('9000');
        cy.getByData('einnahmen-verpflegung').clear().type('1200');
        cy.getByData('einnahmen-subventionen-dritter').clear().type('0');
        cy.getByData('vorangehendes-schuljahr-ueberschuesse')
            .contains('Nein')
            .click({force: true});
        cy.getByData('betreuungstunden-dokumentiert-ueberprueft')
            .contains('Ja')
            .click({force: true});
        cy.getByData('elterngebuehren-tagesschulverordnung-berechnet')
            .contains('Ja')
            .click({force: true});
        cy.getByData('belege-erlaubnis-einsicht-steuerdaten')
            .contains('Ja')
            .click({force: true});
        cy.getByData('maximaltarif-angewendet')
            .contains('Ja')
            .click({force: true});
        cy.getByData('halbe-betreuungszeit-sozialpaedagogisch')
            .contains('Ja')
            .click({force: true});
        cy.getByData('sozialpaedagogische-mitarbeitende-zertifiziert')
            .contains('Ja')
            .click({force: true});
        cy.wait(1500);
        LastenausgleichTagesschulePo.getAngabengemeindeAbschliessen();
        SidenavPO.getLATSSidenavStepStatus('ANGABEN_GEMEINDE').should(
            'have.class',
            'fa-check'
        );
        LastenausgleichTagesschulePo.getSideNav('ANGABEN_GEMEINDE');
        cy.getByData('gemeinde-antrag-angaben-korrigieren').click();
        SidenavPO.getLATSSidenavStepStatus('ANGABEN_GEMEINDE').should(
            'have.class',
            'fa-pencil'
        );
        LastenausgleichTagesschulePo.getAngabengemeindeAbschliessen();
        GemeindeAntraegePO.freigabeGemeindeAntragKanton();
    });

    it('should check antrag', () => {
        cy.login(userSBMandant);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        GemeindeAntraegePO.checkInstitutionValue(
            '6500',
            'einnahmen-elterngebuehren-ohne-verpflegung',
            'einnahmen-elterngebuehren-ohne-verpflegung-bisher'
        );
        cy.getByData('angaben-gemeindeantrag-speichern').click();
        cy.wait(1500);
        LastenausgleichTagesschulePo.getSideNav('FREIGABE');
        cy.getByData('zurueck-an-die-gemeinde').click();
        cy.getByData('container.confirm').click();
        cy.wait(1500);
    });

    it('should antrag ueberarbeiten und freigeben', () => {
        cy.login(userSBGemeinde);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        LastenausgleichTagesschulePo.getAngabengemeindeAbschliessen();
        GemeindeAntraegePO.freigabeGemeindeAntragKanton();
    });

    it('should check LATS as SB Mandant', () => {
        cy.login(userSBMandant);
        cy.visit('/#/gemeinde-antraege');
        GemeindeAntraegePO.filterGemeindeAntrageByPeriode('2024/25');
        cy.getByData('gemeinde-antrag-angaben-korrigieren').click();
        LastenausgleichTagesschulePo.getAngabengemeindeAbschliessen();
        LastenausgleichTagesschulePo.getSideNav('ANGABEN_TAGESSCHULEN');
        cy.getByData('institutionName,status,kontrollfragenOk').click();
        cy.getByData('anzahl-kinder-basisstufe-woche').should('be.disabled');
        LastenausgleichTagesschulePo.getSideNav('FREIGABE');
        cy.getByData('freigabe-geprueft-von-kanton').click();
        cy.getByData('container.confirm').click();
        SidenavPO.getLATSSidenavStepStatus('FREIGABE').should(
            'have.class',
            'fa-check'
        );
    });
});
