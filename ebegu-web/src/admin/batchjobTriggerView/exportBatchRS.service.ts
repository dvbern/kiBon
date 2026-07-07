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

@Injectable({
    providedIn: CoreModule
})
export class ExportBatchRS {
    $http = inject(HttpClient);

    public readonly serviceURL: string;

    public constructor() {
        this.serviceURL = `${CONSTANTS.REST_API}exportbatch`;
    }

    public getServiceName(): string {
        return 'ExportBatchRS';
    }

    public runBatchPublishExistingGemeinden(): Observable<void> {
        return this.callServer(`${this.serviceURL}/publishExistingGemeinden`);
    }

    public runBatchPublishExistingInstitutionen(): Observable<void> {
        return this.callServer(
            `${this.serviceURL}/publishExistingInstitutionen`
        );
    }

    public runBatchPublishWartendeAnmeldungen(): Observable<void> {
        return this.callServer(`${this.serviceURL}/publishWartendeAnmeldungen`);
    }

    public runBatchPublishExistingGemeindeKennzahlen(): Observable<void> {
        return this.callServer(
            `${this.serviceURL}/publishExistingGemeindeKennzahlen`
        );
    }

    public runBatchPublishWartendeBetreuung(): Observable<void> {
        return this.callServer(`${this.serviceURL}/publishWartendeBetreuung`);
    }

    public runBatchMigrateVerfuegung(): Observable<void> {
        return this.callServer(`${this.serviceURL}/migrateVerfuegung`);
    }

    private callServer(url: string): Observable<void> {
        return this.$http.post<void>(url, {});
    }
}
