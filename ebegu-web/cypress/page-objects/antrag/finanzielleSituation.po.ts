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
    FixtureFinSit,
    FixtureFinSitFeutz,
    FixtureFinSitFeutzSolothurn,
    FixtureFinSitFeutzAppenzell,
    FixtureFinSitFeutzLuzern
} from '@dv-e2e/fixtures';
import {NavigationPO} from './navigation.po';

// !! -- PAGE OBJECTS -- !!
const getPageTitle = () => {
    return cy.getByData('page-title');
};

const getNettoLohn = () => {
    return cy.getByData('nettolohn');
};

const getBruttoLohn = () => {
    return cy.getByData('bruttoLohn');
};

const getQuellenbesteuert = (answer: string) => {
    return cy.getByData('quellenbesteuert', 'radio-value.' + answer);
};

const getGemeinsameSteuererklaerung = (answer: string) => {
    return cy.getByData('gemeinsameSteuererklaerung', 'radio-value.' + answer);
};

const getVeranlagt = (answer: string) => {
    return cy.getByData('veranlagt', 'radio-value.' + answer);
};

const getVeranlagtVorJahr = (answer: string) => {
    return cy.getByData('veranlagtVorjahr', 'radio-value.' + answer);
};

const getNettoEinkommen = () => {
    return cy.getByData('nettoEinkommen');
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

const getEinkaeufeVorsorge = () => {
    return cy.getByData('einkaeufeVorsorge');
};

const getLiegenschaftsertraege = () => {
    return cy.getByData('liegenschaftsErtraege');
};

const getAbzuegeLiegenschaft = () => {
    return cy.getByData('abzuegeLiegenschaft');
};

const getSteuerbaresVermoegen = () => {
    return cy.getByData('steuerbaresVermoegen');
};

const getFamilienzulagen = () => {
    return cy.getByData('familienzulage');
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
    return cy.getByData('nettoertraege_erbengemeinschaften');
};

const getEinkommenInVereinfachtemVerfahrenNein = () => {
    return cy.getByData(
        'einkommenInVereinfachtemVerfahrenAbgerechnet1.radio-value.nein'
    );
};

const getGeleisteteAlimente = () => {
    return cy.getByData('geleistete-alimente');
};

const getAbzugSchuldzinsen = () => {
    return cy.getByData('abzug-schuldzinsen');
};

const getGewinnungskosten = () => {
    return cy.getByData('gewinnungskosten');
};

const getSteuerdatenzugriff = (answer: string) => {
    return cy.getByData('steuerdatenzugriff.radio-value.' + answer);
};

const getAutomatischePruefung = (answer: string) => {
    return cy.getByData('automatischePruefung.radio-value.' + answer);
};

const getShowSelbstaendig = () => {
    return cy.getByData('show-selbstaendig');
};

const getSelbststaendigErwerbend = (answer: string) => {
    return cy.getByData('momentanSelbststaendig', 'radio-value.' + answer);
};

const getGeschaeftsgewinnBasisjahr = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr');
};

const getGeschaeftsgewinnBasisjahrMinus1 = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr-minus-1');
};

const getGeschaeftsgewinnBasisjahrMinus2 = () => {
    return cy.getByData('geschaeftsgewinn-basisjahr-minus-2');
};
const getShowErsatzeinkommenSelbststaendigkeit = () => {
    return cy.getByData('show-ersatzeinkommen-selbststaendigkeit');
};

const getShowErsatzeinkommenSelbststaendigkeitRadioButton = (
    answer: string
) => {
    return cy.getByData(
        'showErsatzeinkommenSelbststaendigkeit.radio-value.' + answer
    );
};

const getErsatzeinkommenSelbststaendigkeitBasisjahr = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr');
};

const getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1 = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-1');
};

const getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2 = () => {
    return cy.getByData('ersatzeinkommen-selbststaendigkeit-basisjahr-minus-2');
};

const getErsatzeinkommenInvalidErrorMessage = () => {
    return cy.getByData('ersatzeinkommen-invalid-error-message');
};

const getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage = () => {
    return cy.getByData(
        'all-ersatzeinkommen-selbststaendigkeit-zero-error-message'
    );
};

const getSteuerveranlagungErhalten = (answer: string) => {
    return cy.getByData('steuerveranlagungErhalten', 'radio-value.' + answer);
};

const getUnterhaltsBeitraege = () => {
    return cy.getByData('unterhaltsBeitraege');
};

const getAbzuegeKinderAusbildung = () => {
    return cy.getByData('abzuegeKinderAusbildung');
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

const getSteuerveranlagungGemeinsam = (answer: string) => {
    return cy.getByData(
        'steuerveranlagungGemeinsam.radio-group',
        'radio-value.' + answer
    );
};

// !! -- PAGE ACTIONS -- !!
const fillFinanzielleSituationForm = (
    dataset: keyof typeof FixtureFinSit,
    gs: 'GS1' | 'GS2'
) => {
    FixtureFinSit[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');
        cy.wait(2000);
        FinanzielleSituationPO.getNettoLohn()
            .find('input')
            .type(allData[gs].nettolohn);
        FinanzielleSituationPO.getFamilienzulagen()
            .find('input')
            .type(allData[gs].familienzulage);
        FinanzielleSituationPO.getErsatzeinkommen()
            .find('input')
            .type(allData[gs].ersatzeinkommen);
        FinanzielleSituationPO.getErhalteneAlimente()
            .find('input')
            .type(allData[gs].erhalteneAlimente);
        FinanzielleSituationPO.getBruttoertraegeVermoegen()
            .find('input')
            .type(allData[gs].bruttoErtraegeVermoegen);
        FinanzielleSituationPO.getNettoertraegeErbengemeinschaft()
            .find('input')
            .type(allData[gs].nettoertraegeErbengemeinschaften);
        FinanzielleSituationPO.getEinkommenInVereinfachtemVerfahrenNein().click();
        FinanzielleSituationPO.getGeleisteteAlimente()
            .find('input')
            .type(allData[gs].geleisteteAlimente);
        FinanzielleSituationPO.getAbzugSchuldzinsen()
            .find('input')
            .type(allData[gs].abzugSchuldzinsen);
        FinanzielleSituationPO.getGewinnungskosten()
            .find('input')
            .type(allData[gs].gewinnungskosten);
    });
};

const fillFinanzielleSituationFormFeutzSolothurn = (
    dataset: keyof typeof FixtureFinSitFeutzSolothurn,
    gs: 'GS1' | 'GS2'
) => {
    FixtureFinSitFeutzSolothurn[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');

        // steuerveranlagung erhalten
        if (gs === 'GS1') {
            cy.wait(1500);
            getSelbststaendigErwerbend(allData[gs].selbststaendig)
                .find('label')
                .click();
            cy.wait(1500);
            getSteuerveranlagungErhalten(allData[gs].steuerveranlagungErhalten)
                .find('label')
                .click();
            cy.wait(1500);
            getNettoEinkommen().find('input').type(allData[gs].nettoEinkommen);
            getUnterhaltsBeitraege()
                .find('input')
                .type(allData[gs].unterhaltsBeitraege);
            getAbzuegeKinderAusbildung()
                .find('input')
                .type(allData[gs].abzuegeKinderAusbildung);
            getSteuerbaresVermoegen()
                .find('input')
                .type(allData[gs].steuerbaresVermoegen);
        }

        // quellenbesteuert
        if (gs === 'GS2') {
            cy.wait(1500);
            getSelbststaendigErwerbend(allData[gs].selbststaendig)
                .find('label')
                .click();
            cy.wait(1500);
            getSteuerveranlagungErhalten(allData[gs].steuerveranlagungErhalten)
                .find('label')
                .click();
            cy.wait(1500);
            getBruttoLohn().find('input').type(allData[gs].bruttoLohn);
            getSteuerbaresVermoegen()
                .find('input')
                .type(allData[gs].steuerbaresVermoegen);
        }
    });
};

const fillFinanzielleSituationFormFeutzSchwyz = (
    dataset: keyof typeof FixtureFinSitFeutz,
    gs: 'GS1' | 'GS2'
) => {
    FixtureFinSitFeutz[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');
        cy.wait(2000);

        if (gs === 'GS1') {
            FinanzielleSituationPO.getQuellenbesteuert(
                allData[gs].quellenbesteuert
            )
                .find('input')
                .click();
            FinanzielleSituationPO.getBruttoLohn()
                .find('input')
                .type(allData[gs].bruttoLohn);
        }

        if (gs === 'GS2') {
            FinanzielleSituationPO.getQuellenbesteuert(
                allData[gs].quellenbesteuert
            )
                .find('input')
                .click();
            FinanzielleSituationPO.getNettoEinkommen()
                .find('input')
                .type(allData[gs].nettoEinkommen);
            FinanzielleSituationPO.getEinkaeufeVorsorge()
                .find('input')
                .type(allData[gs].einkaeufeVorsorge);

            FinanzielleSituationPO.getLiegenschaftsertraege()
                .find('input')
                .type(allData[gs].einkaeufeVorsorge);

            FinanzielleSituationPO.getAbzuegeLiegenschaft()
                .find('input')
                .type(allData[gs].abzuegeLiegenschaft);

            FinanzielleSituationPO.getSteuerbaresVermoegen()
                .find('input')
                .type(allData[gs].steuerbaresVermoegen);
        }
    });
};

const fillFinanzielleSituationFormFeutzLuzern = (
    dataset: keyof typeof FixtureFinSitFeutzLuzern
) => {
    FixtureFinSitFeutzLuzern[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');
        cy.wait(1500);

        // START
        FinanzielleSituationPO.getQuellenbesteuert(
            allData.Start.quellenbesteuert
        )
            .find('input')
            .click();
        cy.wait(1500);

        FinanzielleSituationPO.getGemeinsameSteuererklaerung(
            allData.Start.gemeinsameSteuererklaerung
        )
            .find('input')
            .click();
        cy.wait(1500);

        FinanzielleSituationPO.getVeranlagt(allData.Start.veranlagt)
            .find('input')
            .click();
        cy.wait(1500);

        FinanzielleSituationPO.getVeranlagtVorJahr(
            allData.Start.veranlagtVorjahr
        )
            .find('input')
            .click();
        cy.wait(1500);

        // SELBSTDEKLARATION
        FinanzielleSituationPO.getEinkunftErwerb()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftErwerb);

        FinanzielleSituationPO.getEinkunftVersicherung()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftVersicherung);

        FinanzielleSituationPO.getEinkunftWertschriften()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftWertschriften);

        FinanzielleSituationPO.getEinkunftUnterhaltsbeitragKinder()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftUnterhaltsbeitragKinder);

        FinanzielleSituationPO.getEinkunftUeberige()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftUeberige);

        FinanzielleSituationPO.getEinkunftLiegenschaften()
            .find('input')
            .type(allData.Selbstdeklaration.einkunftLiegenschaften);

        FinanzielleSituationPO.getFinanzielleSituationEinkommenGs()
            .find('input')
            .should('have.value', "115'000");

        // ABZUEGE
        FinanzielleSituationPO.getAbzugBerufsauslagen()
            .find('input')
            .type(allData.Abzuege.abzugBerufsauslagen);

        FinanzielleSituationPO.getAbzugSchuldzinsen()
            .find('input')
            .type(allData.Abzuege.abzugSchuldzinsen);

        FinanzielleSituationPO.getAbzugUnterhaltsbeitragKinder()
            .find('input')
            .type(allData.Abzuege.abzugUnterhaltsbeitragKinder);

        FinanzielleSituationPO.getAbzugSaeule3A()
            .find('input')
            .type(allData.Abzuege.abzugSaeule3A);

        FinanzielleSituationPO.getAbzugVersicherungspraemien()
            .find('input')
            .type(allData.Abzuege.abzugVersicherungspraemien);

        FinanzielleSituationPO.getAbzugKrankheitsUnfallKosten()
            .find('input')
            .type(allData.Abzuege.abzugKrankheitsUnfallKosten);

        FinanzielleSituationPO.getSonderabzugErwerbstaetigkeitEhegatten()
            .find('input')
            .type(allData.Abzuege.sonderabzugErwerbstaetigkeitEhegatten);

        FinanzielleSituationPO.getAbzugKinderVorschule()
            .find('input')
            .type(allData.Abzuege.abzugKinderVorschule);

        FinanzielleSituationPO.getAbzugKinderSchule()
            .find('input')
            .type(allData.Abzuege.abzugKinderSchule);

        FinanzielleSituationPO.getAbzugEigenbetreuung()
            .find('input')
            .type(allData.Abzuege.abzugEigenbetreuung);

        FinanzielleSituationPO.getAbzugFremdbetreuung()
            .find('input')
            .type(allData.Abzuege.abzugFremdbetreuung);

        FinanzielleSituationPO.getAbzugErwerbsunfaehigePersonen()
            .find('input')
            .type(allData.Abzuege.abzugErwerbsunfaehigePersonen);

        FinanzielleSituationPO.getFinanzielleSituationTotalAbzuege()
            .find('input')
            .should('have.value', '0');

        // VERMOEGEN
        FinanzielleSituationPO.getVermoegen()
            .find('input')
            .type(allData.Vermoegen.vermoegen);

        FinanzielleSituationPO.getAbzugSteuerfreierBetragErwachsene()
            .find('input')
            .type(allData.Vermoegen.abzugSteuerfreierBetragErwachsene);

        FinanzielleSituationPO.getAbzugSteuerfreierBetragKinder()
            .find('input')
            .type(allData.Vermoegen.abzugSteuerfreierBetragKinder);

        FinanzielleSituationPO.getVermoegen().find('input').focus();

        FinanzielleSituationPO.getAnrechenbaresVermoegenSelbstdeklaration()
            .find('input')
            .should('have.value', "2'500");
    });
};

const fillFinanzielleSituationFormAppenzell = (
    dataset: keyof typeof FixtureFinSitFeutzAppenzell,
    gs: 'GS1' | 'GS2'
) => {
    FixtureFinSitFeutzAppenzell[dataset](allData => {
        cy.url().should('include', 'finanzielleSituation');
        cy.wait(2000);
        getSteuerbaresEinkommen()
            .find('input')
            .type(allData[gs].Eckdaten.steuerbaresEinkommen);
        getSteuerbaresVermoegen()
            .find('input')
            .type(allData[gs].Eckdaten.steuerbaresVermoegen);
        getSaeule3aBvgVersichert()
            .find('input')
            .type(allData[gs].PhaseEins.saeule3aBvgVersichert);
        getSaeule3aBvgAngehoerig()
            .find('input')
            .type(allData[gs].PhaseEins.saeule3aBvgAngehoerig);
        getEinkaufsBeitraege()
            .find('input')
            .type(allData[gs].PhaseEins.einkaufsBeitraege);
        getLiegenschaftsaufwand()
            .find('input')
            .type(allData[gs].PhaseEins.liegenschaftsaufwand);
        getEinkuenfteBGSA()
            .find('input')
            .type(allData[gs].PhaseEins.einkuenfteBGSA);
        getVorjahresverluste()
            .find('input')
            .type(allData[gs].PhaseEins.vorjahresverluste);
        getMitgliederbeitraegeZuwendungen()
            .find('input')
            .type(allData[gs].PhaseEins.mitgliederbeitraegeZuwendungen);
        getLeistungenJuristischePersonen()
            .find('input')
            .type(allData[gs].PhaseEins.leistungenJuristischePersonen);
    });
};

const saveFormAndGoNext = () => {
    cy.waitForRequest('POST', '**/finanzielleSituation/calculateTemp', () => {
        NavigationPO.saveAndGoNext();
    });
};

export const FinanzielleSituationPO = {
    // page objects
    getAnrechenbaresVermoegenSelbstdeklaration,
    getAbzugBerufsauslagen,
    getAbzugUnterhaltsbeitragKinder,
    getAbzugSaeule3A,
    getAbzugVersicherungspraemien,
    getAbzugKrankheitsUnfallKosten,
    getSonderabzugErwerbstaetigkeitEhegatten,
    getAbzugKinderVorschule,
    getAbzugKinderSchule,
    getAbzugEigenbetreuung,
    getAbzugFremdbetreuung,
    getAbzugErwerbsunfaehigePersonen,
    getAbzugSteuerfreierBetragErwachsene,
    getAbzugSteuerfreierBetragKinder,
    getVermoegen,
    getFinanzielleSituationTotalAbzuege,
    getNettoLohn,
    getBruttoLohn,
    getFamilienzulagen,
    getErsatzeinkommen,
    getErhalteneAlimente,
    getEinkaeufeVorsorge,
    getEinkunftErwerb,
    getEinkunftVersicherung,
    getEinkunftWertschriften,
    getEinkunftUnterhaltsbeitragKinder,
    getEinkunftUeberige,
    getEinkunftLiegenschaften,
    getFinanzielleSituationEinkommenGs,
    getBruttoertraegeVermoegen,
    getNettoertraegeErbengemeinschaft,
    getNettoEinkommen,
    getEinkommenInVereinfachtemVerfahrenNein,
    getGeleisteteAlimente,
    getGemeinsameSteuererklaerung,
    getLiegenschaftsertraege,
    getAbzugSchuldzinsen,
    getAbzuegeLiegenschaft,
    getGewinnungskosten,
    getSteuerdatenzugriff,
    getAutomatischePruefung,
    getShowSelbstaendig,
    getGeschaeftsgewinnBasisjahr,
    getGeschaeftsgewinnBasisjahrMinus1,
    getGeschaeftsgewinnBasisjahrMinus2,
    getShowErsatzeinkommenSelbststaendigkeit,
    getShowErsatzeinkommenSelbststaendigkeitRadioButton,
    getErsatzeinkommenSelbststaendigkeitBasisjahr,
    getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1,
    getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2,
    getErsatzeinkommenInvalidErrorMessage,
    getAllErsatzeinkommenSelbststaendigkeitZeroErrorMessage,
    getQuellenbesteuert,
    getSteuerbaresVermoegen,
    getSteuerbaresEinkommen,
    getVeranlagt,
    getVeranlagtVorJahr,
    getSaeule3aBvgVersichert,
    getSaeule3aBvgAngehoerig,
    getEinkaufsBeitraege,
    getLiegenschaftsaufwand,
    getEinkuenfteBGSA,
    getVorjahresverluste,
    getMitgliederbeitraegeZuwendungen,
    getLeistungenJuristischePersonen,
    getSteuerveranlagungGemeinsam,
    getPageTitle,
    // page actions
    fillFinanzielleSituationForm,
    fillFinanzielleSituationFormFeutzSchwyz,
    fillFinanzielleSituationFormFeutzLuzern,
    fillFinanzielleSituationFormFeutzSolothurn,
    fillFinanzielleSituationFormAppenzell,
    saveFormAndGoNext
};
