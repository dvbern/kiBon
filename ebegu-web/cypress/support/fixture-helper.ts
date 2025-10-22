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

import type * as Gemeinde from '../fixtures/admin/gemeinde.json';
import type * as Beschaeftigungspensum from '../fixtures/antrag/beschaeftigungspensum.json';
import type * as BeschaeftigungspensumFeutz from '../fixtures/antrag/schwyz/beschaeftigungspensum-feutz.json';
import type * as BeschaeftigungspensumLuzern from '../fixtures/antrag/luzern-horw/beschaeftigungspensum.json';
import type * as BeschaeftigungspensumSolothurn from '../fixtures/antrag/solothurn/beschaeftigungspensum-feutz.json';
import type * as Betreuung from '../fixtures/antrag/betreuung.json';
import type * as BetreuungSchwyz from '../fixtures/antrag/schwyz/betreuung-schwyz.json';
import type * as BetreuungLuzern from '../fixtures/antrag/luzern-horw/betreuung.json';
import type * as EinkommensverschlechterungInfo from '../fixtures/antrag/einkommensverschlechterung-info.json';
import type * as Einkommensverschlechterung from '../fixtures/antrag/einkommensverschlechterung.json';
import type * as FamSit from '../fixtures/antrag/famsit.json';
import type * as FamSitSchwyz from '../fixtures/antrag/schwyz/famsit-feutz.json';
import type * as FamSitLuzern from '../fixtures/antrag/luzern-horw/famsit-feutz.json';
import type * as FamSitSolothurn from '../fixtures/antrag/solothurn/famsit-feutz.json';
import type * as FamSitAppenzell from '../fixtures/antrag/appenzell/famsit-feutz.json';
import type * as FinSit from '../fixtures/antrag/finsit.json';
import type * as Kind from '../fixtures/antrag/kind-boy.json';
import type * as KindTamara from '../fixtures/antrag/kind-tamara-feutz.json';
import type * as KindLeonard from '../fixtures/antrag/kind-leonard-feutz.json';
import type * as Papier from '../fixtures/antrag/papier.json';
import type * as EinkommensverschlechterungSchwyz from '../fixtures/antrag/schwyz/einkommensverschlechterung-feutz.json';
import type * as EinkommensverschlechterungLuzern from '../fixtures/antrag/luzern-horw/einkommensverschlechterung-feutz.json';
import type * as EinkommensverschlechterungSolothurn from '../fixtures/antrag/solothurn/einkommensverschlechterung-feutz.json';
import type * as EinkommensveraenderungAppenzell from '../fixtures/antrag/appenzell/einkommensverschlechterung-feutz.json';
import type * as FinSitFeutz from '../fixtures/antrag/schwyz/finsit-feutz.json';
import type * as FinSitFeutzLuzern from '../fixtures/antrag/luzern-horw/finsit-feutz.json';
import type * as FinSitFeutzSolothurn from '../fixtures/antrag/solothurn/finsit-feutz.json';
import type * as FinSitFeutzAppenzell from '../fixtures/antrag/appenzell/finsit-feutz.json';
import type * as CreateTagesschule from '../fixtures/institution/create-tagesschule.json';
import type * as Tagesschule from '../fixtures/institution/tagesschule.json';

const fromFixture =
    <T, FixturePart extends keyof T = keyof T>(
        fixture: string,
        fixturePart: FixturePart
    ) =>
    <R>(fn: (data: T[FixturePart]) => R) =>
        cy.fixture(fixture).then((data: T) => fn(data[fixturePart]));

export const FixtureKind = {
    withValidBoy: fromFixture<typeof Kind>('antrag/kind-boy.json', 'valid'),
    withValidGirl: fromFixture<typeof Kind>('antrag/kind-girl.json', 'valid')
};

export const FixtureKinderFeutz = {
    withValidGirl: fromFixture<typeof KindTamara>(
        'antrag/kind-tamara-feutz.json',
        'valid'
    ),
    withValidBoy: fromFixture<typeof KindLeonard>(
        'antrag/kind-leonard-feutz.json',
        'valid'
    )
};

export const FixtureBeschaeftigungspensum = {
    withValid: fromFixture<typeof Beschaeftigungspensum>(
        'antrag/beschaeftigungspensum.json',
        'valid'
    )
};

export const FixtureBeschaeftigungspensumLuzern = {
    withValid: fromFixture<typeof BeschaeftigungspensumLuzern>(
        'antrag/luzern-horw/beschaeftigungspensum.json',
        'valid'
    )
};

export const FixtureBeschaeftigungspensumSolothurn = {
    withValid: fromFixture<typeof BeschaeftigungspensumSolothurn>(
        'antrag/solothurn/beschaeftigungspensum.json',
        'valid'
    )
};

export const FixtureBeschaeftigungspensumFeutz = {
    withValid: fromFixture<typeof BeschaeftigungspensumFeutz>(
        'antrag/schwyz/beschaeftigungspensum-feutz.json',
        'valid'
    )
};

export const FixtureBetreuung = {
    withValid: fromFixture<typeof Betreuung>('antrag/betreuung.json', 'valid'),
    withSchwyz: fromFixture<typeof Betreuung>('antrag/betreuung.json', 'schwyz')
};

export const FixtureBetreuungFeutzSchwyz = {
    withValid: fromFixture<typeof BetreuungSchwyz>(
        'antrag/schwyz/betreuung-schwyz.json',
        'valid'
    )
};

export const FixtureBetreuungFeutzLuzern = {
    withValid: fromFixture<typeof BetreuungLuzern>(
        'antrag/luzern-horw/betreuung.json',
        'valid'
    )
};

export const FixtureFamSit = {
    withValid: fromFixture<typeof FamSit>('antrag/famsit.json', 'valid')
};

export const FixtureFamSitFeutz = {
    withValid: fromFixture<typeof FamSitSchwyz>(
        'antrag/schwyz/famsit-feutz.json',
        'valid'
    )
};

export const FixtureFamSitFeutzLuzern = {
    withValid: fromFixture<typeof FamSitLuzern>(
        'antrag/luzern-horw/famsit-feutz.json',
        'valid'
    )
};

export const FixtureFamSitFeutzSolothurn = {
    withValid: fromFixture<typeof FamSitSolothurn>(
        'antrag/solothurn/famsit-feutz.json',
        'valid'
    )
};

export const FixtureFamSitFeutzAppenzell = {
    withValid: fromFixture<typeof FamSitAppenzell>(
        'antrag/appenzell/famsit-feutz.json',
        'valid'
    )
};

export const FixtureFinSit = {
    withValid: fromFixture<typeof FinSit>('antrag/finsit.json', 'valid')
};

export const FixtureFinSitFeutz = {
    withValid: fromFixture<typeof FinSitFeutz>(
        'antrag/schwyz/finsit-feutz.json',
        'valid'
    )
};

export const FixtureFinSitFeutzLuzern = {
    withValid: fromFixture<typeof FinSitFeutzLuzern>(
        'antrag/luzern-horw/finsit-feutz.json',
        'valid'
    )
};

export const FixtureFinSitFeutzAppenzell = {
    withValid: fromFixture<typeof FinSitFeutzAppenzell>(
        'antrag/appenzell/finsit-feutz.json',
        'valid'
    )
};

export const FixtureFinSitFeutzSolothurn = {
    withValid: fromFixture<typeof FinSitFeutzSolothurn>(
        'antrag/solothurn/finsit-feutz.json',
        'valid'
    )
};

export const FixturePapierAntrag = {
    withValid: fromFixture<typeof Papier>('antrag/papier.json', 'valid')
};

export const FixtureEinkommensverschlechterung = {
    withValid: fromFixture<typeof Einkommensverschlechterung>(
        'antrag/einkommensverschlechterung.json',
        'valid'
    )
};

export const FixtureEinkommensverschlechterungSchwyz = {
    withValid: fromFixture<typeof EinkommensverschlechterungSchwyz>(
        'antrag/schwyz/einkommensverschlechterung-feutz.json',
        'valid'
    )
};

export const FixtureEinkommensverschlechterungLuzern = {
    withValid: fromFixture<typeof EinkommensverschlechterungLuzern>(
        'antrag/luzern-horw/einkommensverschlechterung-feutz.json',
        'valid'
    )
};

export const FixtureEinkommensverschlechterungAppenzell = {
    withValid: fromFixture<typeof EinkommensveraenderungAppenzell>(
        'antrag/appenzell/einkommensverschlechterung-feutz.json',
        'valid'
    )
};

export const FixtureEinkommensverschlechterungSolothurn = {
    withValid: fromFixture<typeof EinkommensverschlechterungSolothurn>(
        'antrag/solothurn/einkommensverschlechterung-feutz.json',
        'valid'
    )
};

export const FixtureEinkommensverschlechterungInfo = {
    withValid: fromFixture<typeof EinkommensverschlechterungInfo>(
        'antrag/einkommensverschlechterung-info.json',
        'valid'
    )
};

export const FixtureCreateTagesschule = {
    withValid: fromFixture<typeof CreateTagesschule>(
        'institution/create-tagesschule.json',
        'valid'
    )
};

export const FixtureTagesschule = {
    withValid: fromFixture<typeof Tagesschule>(
        'institution/tagesschule.json',
        'valid'
    )
};

export const GemeindeFixture = {
    withValid: fromFixture<typeof Gemeinde>('admin/gemeinde.json', 'valid')
};
