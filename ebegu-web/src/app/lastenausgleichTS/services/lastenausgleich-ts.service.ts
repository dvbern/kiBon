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
import {Injectable} from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';
import {TSLastenausgleichTagesschuleAngabenGemeinde} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeinde';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {LogFactory} from '../../core/logging/LogFactory';

const LOG = LogFactory.createLog('LastenausgleichTSService');

@Injectable({
    providedIn: 'root',
})
export class LastenausgleichTSService {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}lats/gemeinde`;
    private readonly ebeguRestUtil = new EbeguRestUtil();
    // return last item but don't provide initial value like BehaviourSubject does
    private lATSAngabenGemeindeContainerStore =
        new ReplaySubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(1);

    public constructor(private readonly http: HttpClient) {
    }

    public updateLATSAngabenGemeindeContainerStore(id: string): void {
        const url = `${this.API_BASE_URL}/find/${encodeURIComponent(id)}`;
        this.http.get<TSLastenausgleichTagesschuleAngabenGemeinde[]>(url)
            .subscribe(container => {
                this.next(container);
            }, error => LOG.error(error));
    }

    public getLATSAngabenGemeindeContainer(): Observable<TSLastenausgleichTagesschuleAngabenGemeindeContainer> {
        return this.lATSAngabenGemeindeContainerStore.asObservable();
    }

    public lATSAngabenGemeindeFuerInstitutionenFreigeben(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/freigebenInstitution`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        }, err => LOG.error(err));
    }

    public saveLATSAngabenGemeindeContainer(container: TSLastenausgleichTagesschuleAngabenGemeindeContainer): void {
        this.http.put(
            `${this.API_BASE_URL}/save`,
            this.ebeguRestUtil.lastenausgleichTagesschuleAngabenGemeindeContainerToRestObject({}, container)
        ).subscribe(result => {
            this.next(result);
        }, error => LOG.error(error));
    }

    public saveLATSKommentar(containerId: string, kommentar: string): Observable<void> {
        return this.http.put<void>(
            `${this.API_BASE_URL}/saveKommentar/${encodeURIComponent(containerId)}`,
            kommentar
        );
    }

    public emptyStore(): void {
        this.lATSAngabenGemeindeContainerStore =
            new ReplaySubject<TSLastenausgleichTagesschuleAngabenGemeindeContainer>(1);
    }

    private next(result: any): void {
        const savedContainer = this.ebeguRestUtil.parseLastenausgleichTagesschuleAngabenGemeindeContainer(
            new TSLastenausgleichTagesschuleAngabenGemeindeContainer(),
            result
        );
        this.lATSAngabenGemeindeContainerStore.next(savedContainer);
    }
}
