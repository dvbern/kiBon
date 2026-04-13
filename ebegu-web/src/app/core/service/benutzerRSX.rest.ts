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
import {map} from 'rxjs/operators';
import {TSBenutzerTableFilterDTO} from '../../../models/dto/TSBenutzerTableFilterDTO';
import {TSTraegerschaft} from '../../../models/entity/TSTraegerschaft';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSBerechtigungHistory} from '../../../models/TSBerechtigungHistory';
import {TSUserSearchresultDTO} from '../../../models/TSUserSearchresultDTO';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '@models/constants';
import {CoreModule} from '../core.module';
import {LogFactory} from '@utils/log';
import {firstValueFrom} from 'rxjs';

@Injectable({
    providedIn: CoreModule
})
export class BenutzerRSX {
    $http = inject(HttpClient);

    private readonly LOG = LogFactory.createLog(BenutzerRSX.name);

    public readonly serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor() {
        this.serviceURL = `${CONSTANTS.REST_API}benutzer`;
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerBgOrGemeindeForGemeinde(
        gemeindeId: string
    ): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(
            `${this.serviceURL}/BgOrGemeinde/active/${encodeURIComponent(gemeindeId)}`
        );
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsOrGemeindeForGemeinde(
        gemeindeId: string
    ): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(
            `${this.serviceURL}/TsOrGemeinde/active/${encodeURIComponent(gemeindeId)}`
        );
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_TS oder Admin_TS oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getBenutzerTsBgOrGemeindeForGemeinde(
        gemeindeId: string
    ): Promise<TSBenutzer[]> {
        return this.getBenutzer(
            `${this.serviceURL}/TsBgOrGemeinde/${encodeURIComponent(gemeindeId)}`
        );
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/BgOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerTsOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/TsOrGemeinde/all`);
    }

    /**
     * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG, Admin_BG,
     * Sachbearbeiter_TS, Admin_TS
     * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
     */
    public getAllBenutzerBgTsOrGemeinde(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(
            `${this.serviceURL}/BgTsOrGemeinde/all`
        );
    }

    /**
     * Gibt alle existierenden, aktiven Benutzer mit den Rollen SACHBEARBEITER_MANDANT und ADMIN_MANDANT zurueck.
     */
    public getAllActiveBenutzerMandant(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/mandant/all`);
    }

    public getAllGesuchsteller(): Promise<TSBenutzerNoDetails[]> {
        return this.getBenutzerNoDetail(`${this.serviceURL}/gesuchsteller`);
    }

    private getBenutzerNoDetail(url: string): Promise<TSBenutzerNoDetails[]> {
        return firstValueFrom(
            this.$http
                .get(url)
                .pipe(
                    map((response: any) =>
                        this.ebeguRestUtil.parseUserNoDetailsList(response)
                    )
                )
        );
    }

    private getBenutzer(url: string): Promise<TSBenutzer[]> {
        return firstValueFrom(
            this.$http.get(url).pipe(
                map((response: any) => {
                    this.LOG.debug(
                        'PARSING benutzer REST array object',
                        response
                    );
                    return this.ebeguRestUtil.parseUserList(response);
                })
            )
        );
    }

    private getSingleBenutzer(url: string): Promise<TSBenutzer> {
        return firstValueFrom(
            this.$http.get(url).pipe(
                map((response: any) => {
                    this.LOG.debug('PARSING benutzer REST object ', response);
                    return this.ebeguRestUtil.parseUser(
                        new TSBenutzer(),
                        response
                    );
                })
            )
        );
    }

    public searchUsers(
        userSearch: TSBenutzerTableFilterDTO
    ): Promise<TSUserSearchresultDTO> {
        return firstValueFrom(
            this.$http
                .post(
                    `${this.serviceURL}/search/`,
                    this.ebeguRestUtil.benutzerTableFilterDTOToRestObject(
                        userSearch
                    )
                )
                .pipe(
                    map((response: any) => {
                        this.LOG.debug(
                            'PARSING benutzer REST array object',
                            response
                        );
                        const tsBenutzers = this.ebeguRestUtil.parseUserList(
                            response.benutzerDTOs
                        );

                        return new TSUserSearchresultDTO(
                            tsBenutzers,
                            response.paginationDTO.totalItemCount
                        );
                    })
                )
        );
    }

    public findBenutzer(username: string): Promise<TSBenutzer> {
        return this.getSingleBenutzer(
            `${this.serviceURL}/username/${encodeURIComponent(username)}`
        );
    }

    public findBenutzerById(username: string): Promise<TSBenutzer> {
        return this.getSingleBenutzer(
            `${this.serviceURL}/id/${encodeURIComponent(username)}`
        );
    }

    public inactivateBenutzer(user: TSBenutzer): Promise<TSBenutzer> {
        const userRest = this.ebeguRestUtil.userToRestObject({}, user);
        return firstValueFrom(
            this.$http
                .put(`${this.serviceURL}/inactivate/`, userRest)
                .pipe(
                    map((response: any) =>
                        this.ebeguRestUtil.parseUser(new TSBenutzer(), response)
                    )
                )
        );
    }

    public reactivateBenutzer(benutzer: TSBenutzer): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return firstValueFrom(
            this.$http
                .put(`${this.serviceURL}/reactivate/`, benutzerRest)
                .pipe(
                    map((response: any) =>
                        this.ebeguRestUtil.parseUser(new TSBenutzer(), response)
                    )
                )
        );
    }

    public einladen(benutzer: TSBenutzer): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return firstValueFrom(
            this.$http
                .post(`${this.serviceURL}/einladen/`, benutzerRest)
                .pipe(
                    map((response: any) =>
                        this.ebeguRestUtil.parseUser(new TSBenutzer(), response)
                    )
                )
        );
    }

    public erneutEinladen(benutzer: TSBenutzer): Promise<any> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return firstValueFrom(
            this.$http.post(`${this.serviceURL}/erneutEinladen/`, benutzerRest)
        );
    }

    public saveBenutzerBerechtigungen(
        benutzer: TSBenutzer
    ): Promise<TSBenutzer> {
        const benutzerRest = this.ebeguRestUtil.userToRestObject({}, benutzer);
        return firstValueFrom(
            this.$http
                .put(
                    `${this.serviceURL}/saveBenutzerBerechtigungen/`,
                    benutzerRest
                )
                .pipe(
                    map((response: any) =>
                        this.ebeguRestUtil.parseUser(new TSBenutzer(), response)
                    )
                )
        );
    }

    public getBerechtigungHistoriesForBenutzer(
        username: string
    ): Promise<TSBerechtigungHistory[]> {
        return firstValueFrom(
            this.$http
                .get(
                    `${this.serviceURL}/berechtigunghistory/${encodeURIComponent(username)}`
                )
                .pipe(
                    map((response: any) => {
                        this.LOG.debug(
                            'PARSING benutzer REST object ',
                            response
                        );
                        return this.ebeguRestUtil.parseBerechtigungHistoryList(
                            response
                        );
                    })
                )
        );
    }

    public isBenutzerDefaultBenutzerOfAnyGemeinde(
        username: string
    ): Promise<boolean> {
        return firstValueFrom(
            this.$http
                .get(
                    `${this.serviceURL}/isdefaultuser/${encodeURIComponent(username)}`
                )
                .pipe(map((response: any) => JSON.parse(response)))
        );
    }

    public removeBenutzer(username: string): Promise<boolean> {
        return firstValueFrom(
            this.$http
                .delete(
                    `${this.serviceURL}/delete/${encodeURIComponent(username)}`
                )
                .pipe(map((response: any) => response))
        );
    }

    public getAllEmailAdminForTraegerschaft(
        traegerschaft: TSTraegerschaft
    ): Promise<string[]> {
        return firstValueFrom(
            this.$http
                .get(
                    `${this.serviceURL}/mailAdminTraegerschaft/${traegerschaft.id}`
                )
                .pipe(map((response: any) => response))
        );
    }
}
