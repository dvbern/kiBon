import type {TestPeriode, User} from './types';

export function checkAuthenticated(
    retriesLeft = 5
): Cypress.Chainable<unknown> {
    const origin = new URL(Cypress.config().baseUrl).origin;

    return cy
        .request({
            url: new URL(
                '/ebegu/api/v1/auth/authenticated-user',
                origin
            ).toString(),
            failOnStatusCode: false
        })
        .then(response => {
            if (response.status === 200) {
                return cy.wrap(undefined);
            }

            if (retriesLeft <= 0) {
                throw new Error(
                    `authenticated-user check failed after retries, last status: ${response.status}`
                );
            }

            return cy.wait(500).then(() => checkAuthenticated(retriesLeft - 1));
        });
}

export function waitForAuthenticated(retriesLeft = 5) {
    cy.wait('@authCheck', {timeout: 15000}).then(interception => {
        const status = interception.response?.statusCode;
        if (status === 200) {
            return;
        }
        if (retriesLeft <= 0) {
            throw new Error(
                `authenticated-user check failed after retries, last status: ${status}`
            );
        }
        waitForAuthenticated(retriesLeft - 1);
    });
}

export const getUser = (user: User): User => {
    return user;
};

export const normalizeUser = (user: User) => {
    return /.*] (.*)/.exec(user)[1].split(' ').join('-');
};

const pad = (n: number): string => String(n).padStart(2, '0');

export const getPeriodeYears = (
    periode: TestPeriode
): {anfang: string; ende: string} => {
    const [anfang, endeShort] = periode.split('/');
    return {anfang, ende: `20${endeShort}`};
};

// '01.08.YYYY' — first day of the periode
export const getPeriodeStart = (periode: TestPeriode): string =>
    `01.08.${getPeriodeYears(periode).anfang}`;

// '31.07.YYYY' — last day of the periode
export const getPeriodeEnd = (periode: TestPeriode): string =>
    `31.07.${getPeriodeYears(periode).ende}`;

// dd.MM.yyyy date inside the periode
export const getDateInPeriode = (
    periode: TestPeriode,
    day: number,
    month: number
): string => {
    const {anfang, ende} = getPeriodeYears(periode);
    const year = month >= 8 ? anfang : ende;
    return `${pad(day)}.${pad(month)}.${year}`;
};

// Excel serial date for a 'dd.MM.yyyy' string (Excel epoch 1899-12-30, UTC).
export const toExcelSerialDate = (ddMMyyyy: string): number => {
    const [day, month, year] = ddMMyyyy.split('.').map(Number);
    const EPOCH = Date.UTC(1899, 11, 30);
    return Math.round((Date.UTC(year, month - 1, day) - EPOCH) / 86_400_000);
};

// 'DD.MM.YYYY' — x years before the periode's anfang year
//  Konkubinat-threshold tests
export const getYearsBeforePeriodeStart = (
    periode: TestPeriode,
    years: number,
    day = 1,
    month = 1
): string => {
    const anfang = Number(getPeriodeYears(periode).anfang);
    return `${pad(day)}.${pad(month)}.${anfang - years}`;
};
