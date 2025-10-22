/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

function getCookie(url) {
    const envVar = `COOKIE_${getEnvName(url).toUpperCase()}`
    if (!process.env[envVar]) {
        throw new Error(`no cookie ${envVar}`)
    }
    return `JSESSIONID=${process.env[envVar]};mandant=${mandantCookies[getMandant()]}`
}

const mandanten = ['be', 'so', 'ar', 'sz', 'lu', 'zg', 'dv']
const mandantCookies = {
    'be': 'Kanton+Bern',
    'sz': 'Kanton+Schwyz',
    'so': 'Kanton+Solothurn',
    'ar': 'Appenzell+Ausserrhoden',
    'lu': 'Stadt+Luzern',
    'zg': 'Zug & Risch',
    'dv': 'Standard Mandant'
}

function getMandant() {
    const mandant = process.env.MANDANT ?? '';
    if (!mandanten.includes(mandant)) {
        throw new Error(`Invalid Mandant: ${mandant}`)
    }
    return mandant
}

async function fetchJson(url) {
    const response = await fetch(url, {headers: {cookie: getCookie(url)}})
    if (response.ok) {
        return response.json();
    }
    const message = await response.text();
    throw new Error(`${url}: ${message}`)
}

async function getEinstellungen(baseUrl, gesuchsperiodeId) {
    const url = new URL(`/ebegu/api/v1/einstellung/gesuchsperiode/${gesuchsperiodeId}/mandant-active`,
            baseUrl);
    return fetchJson(url);
}

async function getGesuchsperioden(baseUrl) {
    const url = new URL('/ebegu/api/v1/gesuchsperioden', baseUrl);
    return fetchJson(url)
}

function getEnvs(mandant) {
    return [
        `https://preview-${mandant}.kibon.ch`,
        `https://dev-${mandant}.kibon.ch`,
        `https://uat-${mandant}.kibon.ch`,
        `https://demo-${mandant}.kibon.ch`,
        `https://${mandant}.kibon.ch`
    ]
}

function getSubdomain(envUrl) {
    return new URL(envUrl).hostname.split(
            '.')[0];
}

function getEnvName(url) {
    const tokens = getSubdomain(url).split('-');
    if (tokens.length === 1) {
        return 'prod'
    } else if (tokens.length === 2) {
        return tokens[0]
    }
    throw Error(`Impossible to determine env name for: ${url}`)
}

function headerGS(gesuchsperiode, envUrl) {
    return `${gesuchsperiode.gueltigAb.slice(0,
            4)}-${gesuchsperiode.gueltigBis.slice(0,
            4)} ${(getSubdomain(envUrl))}`
}

const sheet = {}

for (const envUrl of getEnvs(getMandant())) {
    const gesuchsperioden = await getGesuchsperioden(envUrl)
    for (const gesuchsperiode of gesuchsperioden) {
        const einstellungen = await getEinstellungen(envUrl, gesuchsperiode.id)
        for (const einstellung of einstellungen) {
            if (!sheet[einstellung.key]) {
                sheet[einstellung.key] = {
                    [headerGS(gesuchsperiode, envUrl)]: einstellung.value
                }
            } else {
                sheet[einstellung.key][headerGS(gesuchsperiode, envUrl)] =
                        einstellung.value
            }
        }
    }
}

const perioden = Object.keys(Object.values(sheet)[0])
const rows = ['einstellung', ...perioden]

console.log(rows.join(','))

for (const einstellung in sheet) {
    const row = [einstellung, Object.values(sheet[einstellung])]
    console.log(row.join(','))
}
