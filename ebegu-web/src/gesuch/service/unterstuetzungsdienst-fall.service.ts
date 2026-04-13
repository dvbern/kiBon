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
import {Injectable, inject} from '@angular/core';
import {firstValueFrom, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '@models/constants';
import {TSSprache} from '../../models/enums/TSSprache';
import {TSSozialdienstFall} from '../../models/sozialdienst/TSSozialdienstFall';
import {TSSozialdienstFallDokument} from '../../models/sozialdienst/TSSozialdienstFallDokument';
import {TSFall} from '../../models/TSFall';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class UnterstuetzungsdienstFallService {
    http = inject(HttpClient);

    public serviceURL: string =
        CONSTANTS.REST_API + 'unterstuetzungsdienstfall';
    private ebeguRestUtil = new EbeguRestUtil();

    public removeVollmachtDokument(
        sozialdienstFallDokumentId: string
    ): Promise<TSFall> {
        return firstValueFrom(
            this.http.delete<TSFall>(
                `${this.serviceURL}/vollmachtDokument/${encodeURIComponent(sozialdienstFallDokumentId)}`
            )
        );
    }

    public getAllVollmachtDokumente(
        sozialdienstFallId: string
    ): Promise<TSSozialdienstFallDokument[]> {
        return firstValueFrom(
            this.http
                .get(
                    `${this.serviceURL}/vollmachtDokumente/${encodeURIComponent(sozialdienstFallId)}`
                )
                .pipe(
                    map(restDokumente =>
                        this.ebeguRestUtil.parseSozialdienstFallDokumente(
                            restDokumente
                        )
                    )
                )
        );
    }

    public getVollmachtDokumentAccessTokenGeneratedDokument(
        fallId: string,
        sprache: TSSprache
    ): Promise<BlobPart> {
        return firstValueFrom(
            this.http.get(
                `${this.serviceURL}/generateVollmachtDokument/${encodeURIComponent(fallId)}/${sprache}`,
                {responseType: 'blob'}
            )
        );
    }

    public sozialdienstFallEntziehen(fallId: string): Promise<TSFall> {
        return firstValueFrom(
            this.http
                .put(
                    `${this.serviceURL}/sozialdienstFallEntziehen/${encodeURIComponent(fallId)}`,
                    {}
                )
                .pipe(
                    map((response: TSFall) => {
                        return this.ebeguRestUtil.parseFall(
                            new TSFall(),
                            response
                        );
                    })
                )
        );
    }

    public sozialdienstFallInaktivieren(
        fallId: string
    ): Observable<TSSozialdienstFall> {
        return this.http
            .put<TSSozialdienstFall>(
                `${this.serviceURL}/${encodeURIComponent(fallId)}/inaktivieren`,
                {}
            )
            .pipe(
                map((response: TSSozialdienstFall) => {
                    return this.ebeguRestUtil.parseSozialdienstFall(
                        new TSSozialdienstFall(),
                        response
                    );
                })
            );
    }

    public sozialdienstFallEroeffnen(
        fallId: string
    ): Observable<TSSozialdienstFall> {
        return this.http
            .put<TSSozialdienstFall>(
                `${this.serviceURL}/${encodeURIComponent(fallId)}/eroeffnen`,
                {}
            )
            .pipe(
                map((response: TSSozialdienstFall) => {
                    return this.ebeguRestUtil.parseSozialdienstFall(
                        new TSSozialdienstFall(),
                        response
                    );
                })
            );
    }
}
