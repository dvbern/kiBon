/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {IHttpService, IPromise} from 'angular';
import {MANDANTS} from '@models/mandant';
import {I18nServiceRSRest} from '../i18n/services/i18nServiceRS.rest';
import {HTTP_CODES} from '@models/constants';
import {LogFactory} from '@utils/log';

const LOG = LogFactory.createLog('customTranslateLoader');

export function customTranslateLoader(
    $http: IHttpService
): (options: any) => Promise<object> {
    return (options: any) => {
        const language = options.key.split('_')[0];
        const mandant = options.key.split('_')[1];
        const gemeinde = options.key.split('_')[2];

        return Promise.all([
            getBaseLangTranslations$($http, language),
            getMandantTranslations$($http, language, mandant),
            getGemeindeTranslations($http, language, gemeinde)
        ]).then(
            ([
                baseTranslations,
                mandantTranslations,
                gemeindeTranslations
            ]) => ({
                ...baseTranslations,
                ...mandantTranslations,
                ...gemeindeTranslations
            })
        );
    };
}

function getBaseLangTranslations$(
    $http: IHttpService,
    lang: string
): IPromise<any> {
    return $http
        .get(I18nServiceRSRest.getBaseTranslationsUrl(lang))
        .then(res => res.data);
}

function getMandantTranslations$(
    $http: IHttpService,
    lang: string,
    mandant: string | undefined
): IPromise<any> {
    if (!mandant || mandant === MANDANTS.DVB.hostname) {
        return Promise.resolve({});
    }
    return $http
        .get(I18nServiceRSRest.getMandantTranslationsUrl(lang, mandant))
        .catch(err => {
            if (err.status !== HTTP_CODES.NOT_FOUND) {
                LOG.error(err);
            }
            return {data: {}};
        })
        .then(res => res.data);
}

function getGemeindeTranslations(
    $http: IHttpService,
    lang: string,
    gemeinde: string | null
): IPromise<any> {
    if (!gemeinde) {
        return Promise.resolve({});
    }
    return $http
        .get(I18nServiceRSRest.getGemeindeTranslationsUrl(lang, gemeinde))
        .catch(err => {
            if (err.status !== HTTP_CODES.NOT_FOUND) {
                LOG.error(err);
            }
            return {data: {}};
        })
        .then(res => res.data);
}
