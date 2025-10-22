import {TestPeriode} from '@dv-e2e/types';

const getLATSGemeindeCheckbox = (gemeindeName: string) => {
    return cy.getByData('gemeinde-' + gemeindeName).find('input');
};

const getPeriodeOption = (periode: TestPeriode) => {
    return cy.getByData('periode-' + periode);
};

const getLatestPeriod = () => {
    return cy
        .getByData('gemeindeantraege-periode')
        .find('option')
        .eq(1)
        .invoke('val')
        .then(val => {
            cy.getByData('gemeindeantraege-periode').select(val);
        });
};

const selectCreateGemeindeAntragTyp = (gemeindeAntragTyp: string) => {
    return cy.getByData('gemeindeantraege-typ').select(gemeindeAntragTyp);
};

const freigabeGemeindeAntragKanton = () => {
    cy.getByData('freigabe-pruefung-kanton').click();
    cy.getByData('container.confirm').click();
    cy.wait(1500);
};

const filterGemeindeAntrageByPeriode = (periode: TestPeriode) => {
    cy.getByData('gemeindeantraege-periode-tabelle').click();
    cy.waitForRequest('GET', '**/gemeindeantrag', () => {
        GemeindeAntraegePO.getPeriodeOption(periode).click();
    });
    cy.getByData('antrag-entry#undefined').click();
};

const checkInstitutionValue = (
    newValue: string,
    inputField: string,
    previousValueLabel: string
) => {
    cy.getByData(inputField)
        .invoke('val')
        .then((prevIousValueFromInstitution: string) => {
            cy.getByData(inputField).clear().type(newValue);
            cy.getByData(previousValueLabel).contains(
                prevIousValueFromInstitution
            );
        });
};

export const GemeindeAntraegePO = {
    getLATSGemeindeCheckbox,
    getPeriodeOption,
    getLatestPeriod,
    selectCreateGemeindeAntragTyp,
    freigabeGemeindeAntragKanton,
    filterGemeindeAntrageByPeriode,
    checkInstitutionValue
};
