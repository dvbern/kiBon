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

import {IComponentOptions, IController, IRootScopeService} from 'angular';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {KiBonMandant, MANDANTS} from '@kibon/shared-model-mandant';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {BUILDTSTAMP, VERSION} from '../../../../environments/version';
import {MomentUtil} from '@kibon/shared/util-fn/date';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {TSVersionCheckEvent} from '../../events/TSVersionCheckEvent';
import {HttpVersionInterceptor} from '../../service/version/HttpVersionInterceptor';
import {VersionService} from '../../service/version/version.service';
import IWindowService = angular.IWindowService;
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('DVVersionController');

export class DVVersionComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {};
    public template = require('./dv-version.html');
    public controller = DVVersionController;
    public controllerAs = 'vm';
}

export class DVVersionController implements IController {
    public static $inject = [
        '$rootScope',
        'HttpVersionInterceptor',
        '$window',
        'SharedUtilApplicationPropertyRsService',
        '$translate',
        'AuthServiceRS',
        'VersionService',
        'MandantService'
    ];

    public backendVersion: string;
    public readonly buildTime: string = BUILDTSTAMP;
    public readonly frontendVersion: string = VERSION;
    public showSingleVersion: boolean = true;
    public showBlog: boolean = false;
    public currentYear: number;
    public currentNode: string;
    public mandant: KiBonMandant;

    // We have two angular versions which both have an interceptor for a version mismatch, but we only want to
    // notify the users once, therefore we track here whether we already displayed a mismatch
    private alreadyHandledVersionMismatchByAnyAngular = false;

    public constructor(
        private readonly $rootScope: IRootScopeService,
        private readonly httpVersionInterceptor: HttpVersionInterceptor,
        private readonly $window: IWindowService,
        private readonly applicationPropertyRS: SharedUtilApplicationPropertyRsService,
        private readonly $translate: ITranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly versionService: VersionService,
        private readonly mandantService: MandantService
    ) {}

    public $onInit(): void {
        // AngularJS Version Mismatch
        this.backendVersion = this.httpVersionInterceptor.backendVersion;
        this.$rootScope.$on(
            TSVersionCheckEvent[TSVersionCheckEvent.VERSION_MISMATCH],
            () => {
                this.httpVersionInterceptor.eventCaptured = true;
                this.saveVersionAndHandleMismatch(
                    this.httpVersionInterceptor.backendVersion
                );
            }
        );
        // Anular X Version Mismatch
        this.versionService.$backendVersionChange.subscribe({
            next: version => {
                this.backendVersion = version;
            },
            error: error => LOG.error(error)
        });
        this.versionService.$versionMismatch.subscribe({
            next: backendVersion => {
                this.saveVersionAndHandleMismatch(backendVersion);
                this.versionService.versionMismatchHandled();
            },
            error: error => LOG.error(error)
        });

        this.currentYear = MomentUtil.currentYear();

        // we use this as a healthcheck after we register the listener for VERSION_MISMATCH
        this.applicationPropertyRS.getBackgroundColorFromServer().subscribe();
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(value => (this.currentNode = value.currentNode));
        // Den Blog für Gesuchsteller nicht anzeigen (Wird nur bei Reload angepasst,
        // sollte aber für unsere Zwecke genügen)
        this.mandantService.mandant$.subscribe(mandant => {
            this.mandant = mandant;
        });
        this.showBlog =
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAllRolesButGesuchsteller()
            ) && this.mandant === MANDANTS.BERN;
    }

    private saveVersionAndHandleMismatch(backendVersion: string): void {
        this.backendVersion = backendVersion;
        if (this.alreadyHandledVersionMismatchByAnyAngular) {
            return;
        }
        this.updateDisplayVersion();
        const msg = this.$translate.instant('VERSION_ERROR_TEXT', {
            frontendVersion: this.frontendVersion,
            backendVersion: this.backendVersion
        });
        this.$window.alert(msg);
        this.alreadyHandledVersionMismatchByAnyAngular = true;
    }

    private updateDisplayVersion(): void {
        this.showSingleVersion =
            this.frontendVersion === this.backendVersion ||
            this.backendVersion === null;
    }
}
