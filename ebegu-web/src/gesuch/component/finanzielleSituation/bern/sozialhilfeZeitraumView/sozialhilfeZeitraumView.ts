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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {HybridFormBridgeService} from '@kibon/shared/util/hybrid-form-bridge';
import {
    copy,
    IComponentOptions,
    IPromise,
    IQService,
    IScope,
    ITimeoutService
} from 'angular';
import {map} from 'rxjs/operators';
import {MANDANTS} from '@kibon/shared-model-mandant';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {TSWizardStepName} from '@kibon/shared/model/enums';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {SozialhilfeZeitraumRS} from '../../../../../app/core/service/sozialhilfeZeitraumRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSFamiliensituationContainer} from '../../../../../models/TSFamiliensituationContainer';
import {TSSozialhilfeZeitraum} from '../../../../../models/TSSozialhilfeZeitraum';
import {TSSozialhilfeZeitraumContainer} from '../../../../../models/TSSozialhilfeZeitraumContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {ISozialhilfeZeitraumStateParams} from '../../../../gesuch.route';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../../abstractGesuchView';
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('SozialhilfeZeitraumViewController');

export class SozialhilfeZeitraumViewComponentConfig
    implements IComponentOptions
{
    public transclude = false;
    public bindings = {};
    public template = require('./sozialhilfeZeitraumView.html');
    public controller = SozialhilfeZeitraumViewController;
    public controllerAs = 'vm';
}

export class SozialhilfeZeitraumViewController extends AbstractGesuchViewController<TSSozialhilfeZeitraumContainer> {
    public static $inject: string[] = [
        '$stateParams',
        'GesuchModelManager',
        'BerechnungsManager',
        '$scope',
        'ErrorService',
        'AuthServiceRS',
        'WizardStepManager',
        '$q',
        '$translate',
        '$timeout',
        'SozialhilfeZeitraumRS',
        'MandantService',
        'HybridFormBridgeService'
    ];

    public familiensituation: TSFamiliensituationContainer;
    private isLuzern: boolean;

    public constructor(
        $stateParams: ISozialhilfeZeitraumStateParams,
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        $scope: IScope,
        private readonly errorService: ErrorService,
        private readonly authServiceRS: AuthServiceRS,
        wizardStepManager: WizardStepManager,
        private readonly $q: IQService,
        private readonly $translate: ITranslateService,
        $timeout: ITimeoutService,
        private readonly sozialhilfeZeitraumRS: SozialhilfeZeitraumRS,
        private readonly mandantService: MandantService,
        private readonly hybridFormBridgeService: HybridFormBridgeService
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout
        );

        this.familiensituation =
            this.gesuchModelManager.getGesuch().familiensituationContainer;

        if (this.familiensituation) {
            if ($stateParams.sozialhilfeZeitraumNum) {
                const ewpNum =
                    parseInt($stateParams.sozialhilfeZeitraumNum, 10) || 0;
                this.model = copy(
                    this.familiensituation.sozialhilfeZeitraumContainers[ewpNum]
                );
            } else {
                this.model = this.initEmptyShZContainer();
            }
        } else {
            errorService.addMesageAsError(
                'Unerwarteter Zustand: Familiensituation unbekannt'
            );
        }
        this.mandantService.mandant$
            .pipe(map(mandant => mandant === MANDANTS.LUZERN))
            .subscribe(
                isLuzern => {
                    this.isLuzern = isLuzern;
                },
                err => LOG.error(err)
            );
    }

    public save(): IPromise<any> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        if (
            !this.form.$dirty &&
            !this.hybridFormBridgeService.hasAnyDirtyForm()
        ) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return this.$q.when(this.model);
        }
        this.errorService.clearAll();
        return this.saveSozialhilfeZeitraum(this.familiensituation, this.model);
    }

    public saveSozialhilfeZeitraum(
        familiensituation: TSFamiliensituationContainer,
        sozialhilfeZeitraum: TSSozialhilfeZeitraumContainer
    ): IPromise<TSSozialhilfeZeitraumContainer> {
        if (sozialhilfeZeitraum.id) {
            return this.sozialhilfeZeitraumRS
                .saveSozialhilfeZeitraum(
                    sozialhilfeZeitraum,
                    familiensituation.id
                )
                .then((response: TSSozialhilfeZeitraumContainer) => {
                    const i = EbeguUtil.getIndexOfElementwithID(
                        sozialhilfeZeitraum,
                        familiensituation.sozialhilfeZeitraumContainers
                    );
                    if (i >= 0) {
                        familiensituation.sozialhilfeZeitraumContainers[i] =
                            sozialhilfeZeitraum;
                    }
                    return response;
                });
        }
        return this.sozialhilfeZeitraumRS
            .saveSozialhilfeZeitraum(sozialhilfeZeitraum, familiensituation.id)
            .then(
                (storedSozialhilfeZeitraum: TSSozialhilfeZeitraumContainer) => {
                    familiensituation.sozialhilfeZeitraumContainers.push(
                        storedSozialhilfeZeitraum
                    );
                    return storedSozialhilfeZeitraum;
                }
            );
    }

    public cancel(): void {
        this.form.$setPristine();
    }

    private initEmptyShZContainer(): TSSozialhilfeZeitraumContainer {
        const shz = new TSSozialhilfeZeitraum();
        const shzContainer = new TSSozialhilfeZeitraumContainer();
        shzContainer.sozialhilfeZeitraumJA = shz;
        return shzContainer;
    }

    public sozialhilfeZeitraumDisabled(): boolean {
        // Disabled wenn Mutation, ausser bei Bearbeiter Jugendamt oder Schulamt
        if (this.model && this.model.sozialhilfeZeitraumJA) {
            return (
                this.model.sozialhilfeZeitraumJA.vorgaengerId &&
                !this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getAdministratorOrAmtRole()
                )
            );
        }
        return false;
    }

    get dateError(): any {
        const errors = this.form.sozialhilfeZeitraumAb.$error;
        if (errors.required) {
            return {required: true};
        }
        return {};
    }
}
