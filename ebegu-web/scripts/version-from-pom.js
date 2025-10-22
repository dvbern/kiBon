/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

const {resolve} = require('path');
const {readFileSync, writeFileSync} = require('fs');

const kibonVersion = process.env.VERSION || '2023-SNAPSHOT';

if (!kibonVersion) {
    throw new Error('environment variable VERSION not set');
}

console.log('Kibon version is', kibonVersion);

const file = resolve(__dirname, '..', 'src', 'environments', 'version.ts');
writeFileSync(
    file,
    `// IMPORTANT: THIS FILE IS AUTO GENERATED! DO NOT MANUALLY EDIT OR CHECKIN!
export const VERSION = '${kibonVersion}';

export const BUILDTSTAMP = '${new Date().toISOString()}';
`,
    {encoding: 'utf-8'}
);
