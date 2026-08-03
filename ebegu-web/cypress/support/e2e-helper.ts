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
