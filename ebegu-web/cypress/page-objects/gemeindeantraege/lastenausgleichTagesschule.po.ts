const getSideNav = (sideNav: string) => {
    return cy.getByData('LATS-sidenav.' + sideNav).click();
};

const checkSideNavStatus = (sideNav: string, status: string) => {
    return cy.getByData('LATS-sidenav.' + sideNav).should(status);
};

const getAngabengemeindeAbschliessen = () => {
    cy.getByData('angaben-gemeinde-abschliessen').click();
    cy.getByData('container.confirm').click();
    cy.getByData('label-ok').click();
};

export const LastenausgleichTagesschulePo = {
    getSideNav,
    checkSideNavStatus,
    getAngabengemeindeAbschliessen
};
