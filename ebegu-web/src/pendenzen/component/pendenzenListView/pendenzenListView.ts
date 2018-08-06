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

import {IComponentOptions} from 'angular';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import TSAntragDTO from '../../../models/TSAntragDTO';
import IPromise = angular.IPromise;
import TSAntragSearchresultDTO from '../../../models/TSAntragSearchresultDTO';
import ILogService = angular.ILogService;
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {StateService} from '@uirouter/core';
import SearchRS from '../../../gesuch/service/searchRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

let template = require('./pendenzenListView.html');
require('./pendenzenListView.less');

export class PendenzenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = PendenzenListViewController;
    controllerAs = 'vm';
}

export class PendenzenListViewController {

    totalResultCount: string = '0';

    static $inject: string[] = ['GesuchModelManager', '$state', '$log', 'SearchRS', 'AuthServiceRS'];

    constructor(private gesuchModelManager: GesuchModelManager, private $state: StateService, private $log: ILogService,
                private searchRS: SearchRS, private authServiceRS: AuthServiceRS) {
    }

    public passFilterToServer = (tableFilterState: any): IPromise<TSAntragSearchresultDTO> => {
        this.$log.debug('Triggering ServerFiltering with Filter Object', tableFilterState);
        return this.searchRS.getPendenzenList(tableFilterState).then((response: TSAntragSearchresultDTO) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize.toString() : '0';
            return response;
        });

    }

    public editpendenzJA(pendenz: TSAntragDTO, event: any): void {
        if (pendenz) {
            let isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openPendenz(pendenz, isCtrlKeyPressed);
        }
    }

    private openPendenz(pendenz: TSAntragDTO, isCtrlKeyPressed: boolean) {
        this.gesuchModelManager.clearGesuch();
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())) {
            let navObj: any = {
                gesuchId: pendenz.antragId
            };
            this.navigate('gesuch.familiensituation', navObj, isCtrlKeyPressed);
        } else {
            let navObj: any = {
                createNewFall: false,
                gesuchId: pendenz.antragId,
                dossierId: pendenz.dossierId
            };
            this.navigate('gesuch.fallcreation', navObj, isCtrlKeyPressed);
        }
    }

    private navigate(path: string, navObj: any, isCtrlKeyPressed: boolean): void {
        if (isCtrlKeyPressed) {
            let url = this.$state.href(path, navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go(path, navObj);
        }
    }
}
