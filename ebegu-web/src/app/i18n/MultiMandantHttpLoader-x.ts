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

import {HttpClient} from '@angular/common/http';
import {HTTP_CODES} from '@models/constants';
import {TranslateLoader} from '@ngx-translate/core';
import {combineLatest, Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {MANDANTS} from '@models/mandant';
import {LogFactory} from '../../utils/log-factory/LogFactory';
import {I18nServiceRSRest} from './services/i18nServiceRS.rest';

const LOG = LogFactory.createLog('MultiMandantHttpLoaderX');

export class MultiMandantHttpLoaderX implements TranslateLoader {
    private readonly SEPARATOR = '_';

    public constructor(private readonly http: HttpClient) {}

    public getTranslation(lang: string): Observable<any> {
        const langguage = lang.split(this.SEPARATOR)[0];
        const mandant = lang.split(this.SEPARATOR)[1];
        const gemeinde = lang.split(this.SEPARATOR)[2];

        return combineLatest([
            this.getBaseLangTranslations$(langguage),
            this.getMandantTranslations$(langguage, mandant),
            this.getGemeindeTranslations(langguage, gemeinde)
        ]).pipe(
            map(
                ([
                    baseTranslations,
                    mandantTranslations,
                    gemeindeTranslations
                ]) => {
                    const a = {
                        ...baseTranslations,
                        ...mandantTranslations,
                        ...gemeindeTranslations
                    };
                    return a;
                }
            )
        );
    }

    private getBaseLangTranslations$(lang: string): Observable<any> {
        return this.http.get(I18nServiceRSRest.getBaseTranslationsUrl(lang));
    }

    private getMandantTranslations$(
        lang: string,
        mandant: string | undefined
    ): Observable<any> {
        if (!mandant || mandant === MANDANTS.DVB.hostname) {
            return of({});
        }
        return this.http
            .get(I18nServiceRSRest.getMandantTranslationsUrl(lang, mandant))
            .pipe(
                catchError(err => {
                    if (err.status !== HTTP_CODES.NOT_FOUND) {
                        LOG.error(err);
                    }
                    return of({});
                })
            );
    }

    private getGemeindeTranslations(
        lang: string,
        gemeinde: string | null
    ): Observable<any> {
        if (!gemeinde) {
            return of({});
        }
        return this.http
            .get(I18nServiceRSRest.getGemeindeTranslationsUrl(lang, gemeinde))
            .pipe(
                catchError(err => {
                    if (err.status !== HTTP_CODES.NOT_FOUND) {
                        LOG.error(err);
                    }
                    return of({});
                })
            );
    }
}
