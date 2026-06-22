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

import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {CONSTANTS} from '@models/constants';
import {CoreModule} from '../../app/core/core.module';
import {GemeindeKennzahlenBatchjobResult} from './GemeindeKennzahlenBatchjobResult';

@Injectable({
    providedIn: CoreModule
})
export class YearlyBatchService {
    private readonly http = inject(HttpClient);

    public readonly serviceURL = `${CONSTANTS.REST_API}yearly-batch`;

    public runBatchCreateGemeindeKennzahlenAndSendReminder(): Observable<GemeindeKennzahlenBatchjobResult> {
        return this.http.post(
            `${this.serviceURL}/create-gemeinde-kennzahlen-active-gemeinden`,
            null,
            {
                responseType: 'text'
            }
        ) as Observable<GemeindeKennzahlenBatchjobResult>;
    }

    public runBatchGemeindeKennzahlenSendSecondReminder(): Observable<GemeindeKennzahlenBatchjobResult> {
        return this.http.post(
            `${this.serviceURL}/send-gemeinde-kennzahlen-second-reminder`,
            null,
            {
                responseType: 'text'
            }
        ) as Observable<GemeindeKennzahlenBatchjobResult>;
    }
}
