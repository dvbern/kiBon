/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {IHttpService, ILogService, IPromise} from 'angular';
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class NotrechtRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'WizardStepManager'];
    public serviceURL: string;

    public constructor(
        public $http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
        public $log: ILogService,
    ) {
        this.serviceURL = `${REST_API}notrecht`;

    }

    public initializeRueckforderungFormulare(): IPromise<TSRueckforderungFormular[]> {
        return this.$http.post(`${this.serviceURL}/initialize`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormularList(response.data);
            });
    }

    public getRueckforderungFormulareForCurrentBenutzer(): IPromise<TSRueckforderungFormular[]> {
        return this.$http.get(`${this.serviceURL}/currentuser`, {})
            .then(response => {
                return this.ebeguRestUtil.parseRueckforderungFormularList(response.data);
            });
    }

    public currentUserHasFormular(): IPromise<boolean> {
        return this.$http.get(`${this.serviceURL}/currentuser/hasformular`, {}).then(response => {
            return response.data as boolean;
        });
    }

    public getServiceName(): string {
        return 'NotrechtRS';
    }

    public findRueckforderungFormular(rueckforderungFormularID: string): IPromise<TSRueckforderungFormular> {
        return this.$http.get(`${this.serviceURL}/${encodeURIComponent(rueckforderungFormularID)}`)
            .then((response: any) => {
                this.$log.debug('PARSING RueckforderungFormular REST object ', response.data);
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            });
    }

    public saveRueckforderungFormular(
        rueckforderungFormular: TSRueckforderungFormular
    ): IPromise<TSRueckforderungFormular> {
        let restRueckforderungFormular = {};
        restRueckforderungFormular =
            this.ebeguRestUtil.rueckforderungFormularToRestObject(restRueckforderungFormular, rueckforderungFormular);

        return this.$http.put(this.serviceURL, restRueckforderungFormular).then((response: any) => {
                return this.ebeguRestUtil.parseRueckforderungFormular(new TSRueckforderungFormular(), response.data);
            },
        );
    }

    public sendMitteilung(
        mitteilung: TSRueckforderungMitteilung,
        statusList: TSRueckforderungStatus[]
    ): IPromise<void> {
        let restRueckforderungMitteilung = {};
        restRueckforderungMitteilung =
            this.ebeguRestUtil.rueckforderungMitteilungToRestObject(restRueckforderungMitteilung, mitteilung);
        const data = {mitteilung: restRueckforderungMitteilung, statusList: statusList};
        return this.$http.post(`${this.serviceURL}/mitteilung`, data)
            .then(() => {
                    return;
                },
            );
    }

    public sendEinladung(
        mitteilung: TSRueckforderungMitteilung
    ): IPromise<void> {
        let restRueckforderungMitteilung = {};
        restRueckforderungMitteilung =
            this.ebeguRestUtil.rueckforderungMitteilungToRestObject(restRueckforderungMitteilung, mitteilung);

        return this.$http.post(`${this.serviceURL}/einladung`, restRueckforderungMitteilung)
            .then(() => {
                return;
            },
        );
    }
}
