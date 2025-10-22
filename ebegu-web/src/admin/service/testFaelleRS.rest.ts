/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import moment from 'moment';
import {Observable} from 'rxjs';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {TSGemeindeAntragTyp} from '../../models/enums/TSGemeindeAntragTyp';
import {TSGemeinde, TSGesuchsperiode} from '@kibon/shared/model/entity';
import {MomentUtil} from '@kibon/shared/util-fn/date';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class TestFaelleRS {
    public serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(public http: HttpClient) {
        this.serviceURL = `${CONSTANTS.REST_API}testfaelle`;
    }

    public createTestFallGS(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
        username: string
    ): Observable<string> {
        // TODO that is a strange API path. Configuration does not belong in a hierarchy. Use POST and move the
        // parameter to the method body
        const url = `${this.serviceURL}/testfallgs/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}/${encodeURIComponent(
            username
        )}`;
        return this.http.get(url, {responseType: 'text'});
    }

    public removeFaelleOfGS(username: string): Observable<string> {
        return this.http.delete<string>(
            `${this.serviceURL}/testfallgs/${encodeURIComponent(username)}`
        );
    }

    public createTestFall(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean
    ): Observable<string> {
        const url = `${this.serviceURL}/testfall/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}`;

        return this.http.get(url, {responseType: 'text'});
    }

    public mutiereFallHeirat(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment
    ): Observable<string> {
        return this.http.get<string>(
            `${this.serviceURL}/mutationHeirat/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`,
            {
                params: {
                    mutationsdatum:
                        MomentUtil.momentToLocalDate(mutationsdatum),
                    aenderungper: MomentUtil.momentToLocalDate(aenderungper)
                }
            }
        );
    }

    public testAllMails(
        mailadresse: string,
        gemeindeId: string
    ): Observable<void> {
        return this.http.get<void>(`${this.serviceURL}/mailtest`, {
            params: {mailadresse, gemeindeId}
        });
    }

    public mutiereFallScheidung(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment
    ): Observable<string> {
        const url = `${this.serviceURL}/mutationScheidung/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`;
        return this.http.get(url, {
            params: {
                mutationsdatum: MomentUtil.momentToLocalDate(mutationsdatum),
                aenderungper: MomentUtil.momentToLocalDate(aenderungper)
            },
            responseType: 'text'
        });
    }

    public createTutorialdaten(): Observable<string> {
        return this.http.get(`${this.serviceURL}/schulung/tutorial/create`, {
            responseType: 'text'
        });
    }

    public processScript(scriptNr: string): Observable<any> {
        return this.http.get(`${this.serviceURL}/processscript/${scriptNr}`);
    }

    public createGemeindeAntragTestDaten(
        antragTyp: TSGemeindeAntragTyp,
        gesuchsperiode: TSGesuchsperiode,
        gemeinde: TSGemeinde,
        status: string
    ): Observable<string> {
        return this.http.post(
            `${this.serviceURL}/gemeinde-antraege/${antragTyp}`,
            {
                gesuchsperiode: this.ebeguRestUtil.gesuchsperiodeToRestObject(
                    {},
                    gesuchsperiode
                ),
                gemeinde: this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde),
                status
            },
            {responseType: 'text'}
        );
    }
}
