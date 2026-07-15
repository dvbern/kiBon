export function checkAuthenticated() {
    const origin = new URL(Cypress.config().baseUrl).origin;

    return cy.window().then(win =>
        win
            .fetch(
                new URL(
                    '/ebegu/api/v1/auth/authenticated-user',
                    origin
                ).toString(),
                {credentials: 'include'}
            )
            .then(res => {
                expect(res.status).to.eq(200);
            })
    );
}
