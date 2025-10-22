import {
    FixtureEinkommensverschlechterung,
    FixtureEinkommensverschlechterungAppenzell,
    FixtureEinkommensverschlechterungLuzern,
    FixtureEinkommensverschlechterungSchwyz,
    FixtureEinkommensverschlechterungSolothurn
} from '@dv-e2e/fixtures';
import {TestFaellePO} from '../admin';
import {NavigationPO} from './navigation.po';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getNettoLohn = () => {
    return cy.getByData('nettolohn');
};

const getNettoEinkommen = () => {
    return cy.getByData('nettoEinkommen');
};

const getEinkaeufeVorsorge = () => {
    return cy.getByData('einkaeufeVorsorge');
};
const getAbzuegeLiegenschaft = () => {
    return cy.getByData('abzuegeLiegenschaft');
};

const getLiegenschaftsertraege = () => {
    return cy.getByData('liegenschaftsErtraege');
};
const getSteuerbaresVermoegen = () => {
    return cy.getByData('steuerbaresVermoegen');
};

const getFamilienzulagen = () => {
    return cy.getByData('familienzulage');
};

const getBruttoLohn = () => {
    return cy.getByData('bruttoLohn');
};

const getErsatzeinkommen = () => {
    return cy.getByData('ersatzeinkommen');
};

const getErhalteneAlimente = () => {
    return cy.getByData('erhaltene-alimente');
};

const getBruttoertraegeVermoegen = () => {
    return cy.getByData('brutto-ertraege-vermoegen');
};

const getNettoertraegeErbengemeinschaft = () => {
    return cy.getByData('netto-ertraege-erbengemeinschaften');
};

const getEinkommenInVereinfachtemVerfahren = (answer: string) => {
    return cy.getByData(
        'einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.' + answer
    );
};

const getAbzugSchuldzinsen = () => {
    return cy.getByData('abzug-schuldzinsen');
};

const getGewinnungskosten = () => {
    return cy.getByData('gewinnungskosten');
};

const getGeleisteteAlimente = () => {
    return cy.getByData('geleistete-alimente');
};

const getMassgebendesEinkommenAbweichung = (answer: string) => {
    return cy.getByData(
        'einkommensverschlechterung.radio-group',
        'einkommensverschlechterung.radio-value.' + answer
    );
};

const getEkvJahr = (option: number) => {
    return cy.getByData('ekv-fuer-basis-jahr-plus#' + option);
};

const getEinkunftErwerb = () => {
    return cy.getByData('einkunftErwerb');
};

const getEinkunftVersicherung = () => {
    return cy.getByData('einkunftVersicherung');
};

const getEinkunftWertschriften = () => {
    return cy.getByData('einkunftWertschriften');
};

const getEinkunftUnterhaltsbeitragKinder = () => {
    return cy.getByData('einkunftUnterhaltsbeitragKinder');
};

const getEinkunftUeberige = () => {
    return cy.getByData('einkunftUeberige');
};

const getEinkunftLiegenschaften = () => {
    return cy.getByData('einkunftLiegenschaften');
};

const getFinanzielleSituationEinkommenGs = () => {
    return cy.getByData('FINANZIELLE_SITUATION_EINKOMMEN_EIN_GS');
};

const getAbzugBerufsauslagen = () => {
    return cy.getByData('abzugBerufsauslagen');
};

const getAbzugUnterhaltsbeitragKinder = () => {
    return cy.getByData('abzugUnterhaltsbeitragKinder');
};

const getAbzugSaeule3A = () => {
    return cy.getByData('abzugSaeule3A');
};

const getAbzugVersicherungspraemien = () => {
    return cy.getByData('abzugVersicherungspraemien');
};

const getAbzugKrankheitsUnfallKosten = () => {
    return cy.getByData('abzugKrankheitsUnfallKosten');
};

const getSonderabzugErwerbstaetigkeitEhegatten = () => {
    return cy.getByData('sonderabzugErwerbstaetigkeitEhegatten');
};

const getAbzugKinderVorschule = () => {
    return cy.getByData('abzugKinderVorschule');
};

const getAbzugKinderSchule = () => {
    return cy.getByData('abzugKinderSchule');
};

const getAbzugEigenbetreuung = () => {
    return cy.getByData('abzugEigenbetreuung');
};

const getAbzugFremdbetreuung = () => {
    return cy.getByData('abzugFremdbetreuung');
};

const getAbzugErwerbsunfaehigePersonen = () => {
    return cy.getByData('abzugErwerbsunfaehigePersonen');
};

const getFinanzielleSituationTotalAbzuege = () => {
    return cy.getByData('FINANZIELLE_SITUATION_TOTAL_ABZUEGE');
};

const getVermoegen = () => {
    return cy.getByData('vermoegen');
};

const getAbzugSteuerfreierBetragErwachsene = () => {
    return cy.getByData('abzugSteuerfreierBetragErwachsene');
};

const getAbzugSteuerfreierBetragKinder = () => {
    return cy.getByData('abzugSteuerfreierBetragKinder');
};

const getAnrechenbaresVermoegenSelbstdeklaration = () => {
    return cy.getByData('ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION');
};

const getBruttoLohnX = (number: string) => {
    return cy.getByData('bruttolohnAbrechnung' + number);
};

const getExtraLohn = (answer: string) => {
    return cy.getByData('extraLohn', 'radio-value.' + answer);
};

const getBruttoLohnJahr = () => {
    return cy.getByData('bruttoLohnJahr');
};

const getNettoVermoegen = () => {
    return cy.getByData('nettoVermoegen');
};

const getMassgebendesEinkVorAbzFamGrGSX = () => {
    return cy.getByData('massgebendesEinkVorAbzFamGrGSX');
};

const getSteuerbaresEinkommen = () => {
    return cy.getByData('steuerbaresEinkommen');
};

const getSaeule3aBvgVersichert = () => {
    return cy.getByData('saeule3aBvgVersichert');
};

const getSaeule3aBvgAngehoerig = () => {
    return cy.getByData('saeule3aBvgAngehoerig');
};

const getEinkaufsBeitraege = () => {
    return cy.getByData('einkaufsBeitraege');
};

const getLiegenschaftsaufwand = () => {
    return cy.getByData('liegenschaftsaufwand');
};

const getEinkuenfteBGSA = () => {
    return cy.getByData('einkuenfteBGSA');
};

const getVorjahresverluste = () => {
    return cy.getByData('vorjahresverluste');
};

const getMitgliederbeitraegeZuwendungen = () => {
    return cy.getByData('mitgliederbeitraegeZuwendungen');
};

const getLeistungenJuristischePersonen = () => {
    return cy.getByData('leistungenJuristischePersonen');
};

// !! -- PAGE ACTIONS -- !!
const fillEinkommensverschlechterungForm = (
    dataset: keyof typeof FixtureEinkommensverschlechterung,
    jahr: 'jahr1' | 'jahr2',
    gesuchsteller: 'GS1' | 'GS2'
) => {
    FixtureEinkommensverschlechterung[dataset](
        ({[jahr]: {[gesuchsteller]: GS}}) => {
            getNettoLohn().find('input').type(GS.nettolohn);
            getFamilienzulagen().find('input').type(GS.familienzulage);
            getErsatzeinkommen().find('input').type(GS.ersatzeinkommen);
            getErhalteneAlimente().find('input').type(GS.erhalteneAlimente);
            getBruttoertraegeVermoegen()
                .find('input')
                .type(GS.bruttoertraegeVermoegen);
            getNettoertraegeErbengemeinschaft()
                .find('input')
                .type(GS.nettoertraegeErbengemeinschaften);
            getEinkommenInVereinfachtemVerfahren(
                GS.einkommenInVereinfachtemVerfahrenAbgerechnet
            ).click();
            getGeleisteteAlimente().find('input').type(GS.geleisteteAlimente);
            getAbzugSchuldzinsen().find('input').type(GS.abzugSchuldzinsen);
            getGewinnungskosten().find('input').type(GS.gewinnungskosten);
        }
    );
};

const fillEinkommensverschlechterungFormSolothurn = (
    dataset: keyof typeof FixtureEinkommensverschlechterungSolothurn,
    jahr: 'jahr1' | 'jahr2',
    gesuchsteller: 'GS1' | 'GS2'
) => {
    if (gesuchsteller === 'GS1' && jahr === 'jahr1') {
        FixtureEinkommensverschlechterungSolothurn[dataset](data => {
            getMassgebendesEinkommenAbweichung(
                data.start.einkommensverschlechterung
            ).click();
            getEkvJahr(data.start.ekvFuerBasisJahrPlus1).click();
            getEkvJahr(data.start.ekvFuerBasisJahrPlus2).click();
            cy.waitForRequest(
                'PUT',
                '**/einkommensverschlechterungInfo/**',
                () => {
                    NavigationPO.saveAndGoNext();
                }
            );
        });
    }

    FixtureEinkommensverschlechterungSolothurn[dataset](
        ({[jahr]: {[gesuchsteller]: GS}}) => {
            getBruttoLohnX('1').find('input').type(GS.bruttoLohn1);
            getBruttoLohnX('2').find('input').type(GS.bruttoLohn2);
            getBruttoLohnX('3').find('input').type(GS.bruttoLohn3);
            getExtraLohn(GS.extraLohn).find('input').click();
            getNettoVermoegen().find('input').type(GS.nettovermoegen);
            getBruttoLohnJahr().find('input').should('be.disabled');
            getBruttoLohnJahr()
                .find('input')
                .should('have.value', GS.bruttoLohnJahr);
            getMassgebendesEinkVorAbzFamGrGSX()
                .find('input')
                .should('be.disabled');
            getMassgebendesEinkVorAbzFamGrGSX()
                .find('input')
                .should('have.value', GS.massgebendesEinkVorAbzFamGrGSX);
        }
    );
};

const fillEinkommensverschlechterungFormSchwyz = (
    dataset: keyof typeof FixtureEinkommensverschlechterungSchwyz,
    gs: 'GS1' | 'GS2'
) => {
    FixtureEinkommensverschlechterungSchwyz[dataset](data => {
        cy.url().should('include', 'einkommensverschlechterung');

        if (gs === 'GS1') {
            getMassgebendesEinkommenAbweichung(
                data.einkommensVerschlechterung
            ).click();
            NavigationPO.saveAndGoNext();
            getBruttoLohn().find('input').type(data[gs].bruttoLohn);
            NavigationPO.saveAndGoNext();
        }

        if (gs === 'GS2') {
            getNettoEinkommen().find('input').type(data[gs].nettoEinkommen);
            getEinkaeufeVorsorge()
                .find('input')
                .type(data[gs].einkaeufeVorsorge);
            getLiegenschaftsertraege()
                .find('input')
                .type(data[gs].liegenschaftsErtraege);
            getAbzuegeLiegenschaft()
                .find('input')
                .type(data[gs].abzuegeLiegenschaft);
            getSteuerbaresVermoegen()
                .find('input')
                .type(data[gs].steuerbaresVermoegen);

            NavigationPO.saveAndGoNext();
        }
    });
};

const fillEinkommensverschlechterungFormLuzern = (
    dataset: keyof typeof FixtureEinkommensverschlechterungLuzern
) => {
    FixtureEinkommensverschlechterungLuzern[dataset](data => {
        cy.url().should('include', 'einkommensverschlechterung');
        getMassgebendesEinkommenAbweichung(
            data.einkommensVerschlechterung
        ).click();
        getEkvJahr(1).click();
        getEkvJahr(2).click();
        NavigationPO.saveAndGoNext();

        fillEinkommensverschlechterungandSaveLuzern(data); // 2023
        cy.wait(1500);
        fillEinkommensverschlechterungandSaveLuzern(data); // 2024
    });
};

function fillEinkommensverschlechterungandSaveLuzern(data: any) {
    // SELBSTDEKLARATION
    getEinkunftErwerb()
        .find('input')
        .type(data.Selbstdeklaration.einkunftErwerb);
    getEinkunftVersicherung()
        .find('input')
        .type(data.Selbstdeklaration.einkunftVersicherung);
    getEinkunftWertschriften()
        .find('input')
        .type(data.Selbstdeklaration.einkunftWertschriften);
    getEinkunftUnterhaltsbeitragKinder()
        .find('input')
        .type(data.Selbstdeklaration.einkunftUnterhaltsbeitragKinder);
    getEinkunftUeberige()
        .find('input')
        .type(data.Selbstdeklaration.einkunftUeberige);
    getEinkunftLiegenschaften()
        .find('input')
        .type(data.Selbstdeklaration.einkunftLiegenschaften);

    // ABZUEGE
    getAbzugBerufsauslagen()
        .find('input')
        .type(data.Abzuege.abzugBerufsauslagen);
    getAbzugSchuldzinsen().find('input').type(data.Abzuege.abzugSchuldzinsen);
    getAbzugUnterhaltsbeitragKinder()
        .find('input')
        .type(data.Abzuege.abzugUnterhaltsbeitragKinder);
    getAbzugSaeule3A().find('input').type(data.Abzuege.abzugSaeule3A);
    getAbzugVersicherungspraemien()
        .find('input')
        .type(data.Abzuege.abzugVersicherungspraemien);
    getAbzugKrankheitsUnfallKosten()
        .find('input')
        .type(data.Abzuege.abzugKrankheitsUnfallKosten);
    getSonderabzugErwerbstaetigkeitEhegatten()
        .find('input')
        .type(data.Abzuege.sonderabzugErwerbstaetigkeitEhegatten);
    getAbzugKinderVorschule()
        .find('input')
        .type(data.Abzuege.abzugKinderVorschule);
    getAbzugKinderSchule().find('input').type(data.Abzuege.abzugKinderSchule);
    getAbzugEigenbetreuung()
        .find('input')
        .type(data.Abzuege.abzugEigenbetreuung);
    getAbzugFremdbetreuung()
        .find('input')
        .type(data.Abzuege.abzugFremdbetreuung);
    getAbzugErwerbsunfaehigePersonen()
        .find('input')
        .type(data.Abzuege.abzugErwerbsunfaehigePersonen);

    // VERMOEGEN
    getVermoegen().find('input').type(data.Vermoegen.vermoegen);
    getAbzugSteuerfreierBetragErwachsene()
        .find('input')
        .type(data.Vermoegen.abzugSteuerfreierBetragErwachsene);
    getAbzugSteuerfreierBetragKinder()
        .find('input')
        .type(data.Vermoegen.abzugSteuerfreierBetragKinder);

    cy.waitForRequest(
        'POST',
        '**/einkommensverschlechterung/calculateTemp/1',
        () => {
            NavigationPO.saveAndGoNext();
        }
    );
}

const fillEinkommensverschlechterungFormAppenzell = (
    dataset: keyof typeof FixtureEinkommensverschlechterungAppenzell,
    jahr: 'jahr1' | 'jahr2',
    gesuchsteller: 'GS1' | 'GS2'
) => {
    FixtureEinkommensverschlechterungAppenzell[dataset](
        ({[jahr]: {[gesuchsteller]: gs}}) => {
            getSteuerbaresEinkommen()
                .find('input')
                .type(gs.Eckdaten.steuerbaresEinkommen);
            getSteuerbaresVermoegen()
                .find('input')
                .type(gs.Eckdaten.steuerbaresVermoegen);
            getSaeule3aBvgVersichert()
                .find('input')
                .type(gs.PhaseEins.saeule3aBvgVersichert);
            getSaeule3aBvgAngehoerig()
                .find('input')
                .type(gs.PhaseEins.saeule3aBvgAngehoerig);
            getEinkaufsBeitraege()
                .find('input')
                .type(gs.PhaseEins.einkaufsBeitraege);
            getLiegenschaftsaufwand()
                .find('input')
                .type(gs.PhaseEins.liegenschaftsaufwand);
            getEinkuenfteBGSA().find('input').type(gs.PhaseEins.einkuenfteBGSA);
            getVorjahresverluste()
                .find('input')
                .type(gs.PhaseEins.vorjahresverluste);
            getMitgliederbeitraegeZuwendungen()
                .find('input')
                .type(gs.PhaseEins.mitgliederbeitraegeZuwendungen);
            getLeistungenJuristischePersonen()
                .find('input')
                .type(gs.PhaseEins.leistungenJuristischePersonen);
        }
    );
};

export const EinkommensverschlechterungPO = {
    // page objects
    getPageTitle,
    getNettoLohn,
    getFamilienzulagen,
    getErsatzeinkommen,
    getErhalteneAlimente,
    getBruttoertraegeVermoegen,
    getNettoertraegeErbengemeinschaft,
    getEinkommenInVereinfachtemVerfahren,
    getGeleisteteAlimente,
    getAbzugSchuldzinsen,
    getGewinnungskosten,
    getLiegenschaftsertraege,
    // page actions
    fillEinkommensverschlechterungForm,
    fillEinkommensverschlechterungFormSchwyz,
    fillEinkommensverschlechterungFormLuzern,
    fillEinkommensverschlechterungFormAppenzell,
    fillEinkommensverschlechterungFormSolothurn
};
