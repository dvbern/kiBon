export function checkAuthenticated() {
    const origin = new URL(Cypress.config().baseUrl).origin;

    cy.request({
        url: new URL(
            '/ebegu/api/v1/auth/authenticated-user',
            origin
        ).toString(),
        failOnStatusCode: false
    })
        .its('status')
        .should('eq', 200);
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
