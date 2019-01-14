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

import {IComponentOptions, IController} from 'angular';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import DateUtil from '../../../../utils/DateUtil';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import HttpVersionInterceptor from '../../service/version/HttpVersionInterceptor';
import IRootScopeService = angular.IRootScopeService;
import IWindowService = angular.IWindowService;

export class DVVersionComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-version.html');
    public controller = DVVersionController;
    public controllerAs = 'vm';
}

export class DVVersionController implements IController {

    public static $inject = ['$rootScope', 'HttpVersionInterceptor', '$window'];

    public backendVersion: string;
    public readonly buildTime: string = BUILDTSTAMP;
    public readonly frontendVersion: string = VERSION;
    public showSingleVersion: boolean = true;
    public currentYear: number;

    public constructor(
        private readonly $rootScope: IRootScopeService,
        private readonly httpVersionInterceptor: HttpVersionInterceptor,
        private readonly $window: IWindowService,
    ) {

    }

    public $onInit(): void {

        this.backendVersion = this.httpVersionInterceptor.backendVersion;
        this.currentYear = DateUtil.currentYear();

        this.$rootScope.$on(TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH], () => {
            this.backendVersion = this.httpVersionInterceptor.backendVersion;
            this.updateDisplayVersion();
            const msg = `Der Client (${this.frontendVersion}) hat eine andere Version als der Server(${this.backendVersion}). Bitte laden sie die Seite komplett neu (F5)`;
            this.$window.alert(msg);

        });

    }

    private updateDisplayVersion(): void {
        this.showSingleVersion = this.frontendVersion === this.backendVersion || this.backendVersion === null;
    }

}