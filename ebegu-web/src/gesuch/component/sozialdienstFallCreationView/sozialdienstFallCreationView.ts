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

import {MAX_FILE_SIZE} from '@kibon/shared/model/constants';
import {StateService} from '@uirouter/core';
import {IComponentOptions, IScope} from 'angular';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {
    TSDokumentUploadTyp,
    TSRole,
    TSSprache
} from '@kibon/shared/model/enums';
import {TSSozialdienstFallStatus} from '../../../models/enums/TSSozialdienstFallStatus';
import {TSWizardStepName, TSWizardStepStatus} from '@kibon/shared/model/enums';
import {TSSozialdienstFall} from '../../../models/sozialdienst/TSSozialdienstFall';
import {TSSozialdienstFallDokument} from '../../../models/sozialdienst/TSSozialdienstFallDokument';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSFall} from '../../../models/TSFall';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {OkHtmlDialogController} from '../../dialog/OkHtmlDialogController';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {INewFallStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {UnterstuetzungsdienstFallService} from '../../service/unterstuetzungsdienst-fall.service';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../abstractGesuchView';
import ITimeoutService = angular.ITimeoutService;
import ITranslateService = angular.translate.ITranslateService;

const okHtmlDialogTempl = require('../../../gesuch/dialog/okHtmlDialogTemplate.html');
const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');

const LOG = LogFactory.createLog('SozialdienstFallCreationViewController');

export class SozialdienstFallCreationViewComponentConfig
    implements IComponentOptions
{
    public transclude = false;
    public template = require('./sozialdienstFallCreationView.html');
    public controller = SozialdienstFallCreationViewController;
    public controllerAs = 'vm';
}

export class SozialdienstFallCreationViewController extends AbstractGesuchViewController<any> {
    public static readonly $inject = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        '$stateParams',
        'WizardStepManager',
        '$translate',
        '$scope',
        'AuthServiceRS',
        '$state',
        'UploadRS',
        'UnterstuetzungsdienstFallService',
        'DownloadRS',
        'DvDialog',
        '$timeout'
    ];

    private gesuchsperiodeId: string;

    public showAntragsteller2Error: boolean = false;
    public dokumente: TSSozialdienstFallDokument[];
    public fileTypes = [TSDokumentUploadTyp.PDF, TSDokumentUploadTyp.WORD];

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        private readonly $stateParams: INewFallStateParams,
        wizardStepManager: WizardStepManager,
        private readonly $translate: ITranslateService,
        $scope: IScope,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $state: StateService,
        private readonly uploadRS: UploadRS,
        private readonly unterstuetzungsdienstFallService: UnterstuetzungsdienstFallService,
        private readonly downloadRS: DownloadRS,
        private readonly dvDialog: DvDialog,
        $timeout: ITimeoutService
    ) {
        super(
            gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
            $timeout
        );
    }

    public $onInit(): void {
        super.$onInit();
        this.readStateParams();
        this.initViewModel();
    }

    private readStateParams(): void {
        if (
            this.$stateParams.gesuchsperiodeId &&
            this.$stateParams.gesuchsperiodeId !== ''
        ) {
            this.gesuchsperiodeId = this.$stateParams.gesuchsperiodeId;
        }
    }

    private initViewModel(): void {
        if (this.gesuchModelManager.getFall().sozialdienstFall.isNew()) {
            return;
        }
        this.unterstuetzungsdienstFallService
            .getAllVollmachtDokumente(
                this.gesuchModelManager.getFall().sozialdienstFall.id
            )
            .then(dokumente => (this.dokumente = dokumente));
    }

    private validateForm(): boolean {
        this.showAntragsteller2Error = false;
        this.validateZweiteAntragsteller();
        if (!this.isGesuchValid() || this.showAntragsteller2Error) {
            return false;
        }
        if (
            !this.form.$dirty &&
            !this.gesuchModelManager.getFall().sozialdienstFall.isNew()
        ) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            return false;
        }
        return true;
    }

    public save(): void {
        if (this.validateForm()) {
            this.saveData();
        }
    }

    private saveData(): angular.IPromise<void> {
        this.errorService.clearAll();
        return this.gesuchModelManager
            .saveFall()
            .then(fall => {
                if (
                    fall.sozialdienstFall.status ===
                    TSSozialdienstFallStatus.AKTIV
                ) {
                    this.wizardStepManager.updateCurrentWizardStepStatus(
                        TSWizardStepStatus.OK
                    );
                }
                this.navigateToSozialdienstFallCreation(fall);
            })
            .catch(err => {
                LOG.error('Could not save SozialdienstFall', err);
                this.navigateToSozialdienstFallCreation(
                    this.gesuchModelManager.getFall()
                );
            });
    }

    private navigateToSozialdienstFallCreation(fall: TSFall): void {
        const params: INewFallStateParams = {
            gesuchsperiodeId: this.gesuchsperiodeId,
            creationAction: null,
            gesuchId: EbeguUtil.isNotNullOrUndefined(
                this.gesuchModelManager.getGesuch()
            )
                ? this.gesuchModelManager.getGesuch().id
                : null,
            dossierId: null,
            gemeindeId: this.gesuchModelManager.getGemeinde().id,
            eingangsart: null,
            sozialdienstId: fall.sozialdienstFall.sozialdienst.id,
            fallId: fall.id
        };
        this.$state.go('gesuch.sozialdienstfallcreation', params);
    }

    public weiter(): void {
        const params: INewFallStateParams = {
            gesuchsperiodeId: this.gesuchsperiodeId,
            creationAction: null,
            gesuchId: EbeguUtil.isNotNullOrUndefined(
                this.gesuchModelManager.getGesuch()
            )
                ? this.gesuchModelManager.getGesuch().id
                : null,
            dossierId: null,
            gemeindeId: this.gesuchModelManager.getGemeinde().id,
            eingangsart: null,
            sozialdienstId: null,
            fallId: this.gesuchModelManager.getFall().id
        };
        this.$state.go('gesuch.fallcreation', params);
    }

    public getNextButtonText(): string {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getFall().sozialdienstFall.isNew()) {
                return this.$translate.instant('ERSTELLEN');
            }
            return this.$translate.instant('SPEICHERN');
        }
        return this.$translate.instant('WEITER');
    }

    public isSozialdienstFallReadOnly(): boolean {
        if (
            this.isSozialdienstFallAktiv() ||
            this.isSozialdienstFallEntzogen()
        ) {
            return true;
        }
        return false;
    }

    public isSozialdienstDokumentReadOnly(): boolean {
        if (
            this.isSozialdienstFallAktiv() ||
            (this.isSozialdienstFallEntzogen() &&
                !this.authServiceRS.isRole(TSRole.SUPER_ADMIN))
        ) {
            return true;
        }
        return false;
    }

    public isSozialdienstFallAktiv(): boolean {
        return (
            this.gesuchModelManager.getFall().sozialdienstFall?.status ===
            TSSozialdienstFallStatus.AKTIV
        );
    }

    public isSozialdienstFallEntzogen(): boolean {
        return (
            this.gesuchModelManager.getFall().sozialdienstFall?.status ===
            TSSozialdienstFallStatus.ENTZOGEN
        );
    }

    public isAktivierungMoeglich(): boolean {
        if (
            this.gesuchModelManager.getFall().sozialdienstFall?.status ===
                TSSozialdienstFallStatus.INAKTIV &&
            this.dokumente &&
            this.dokumente.length > 0
        ) {
            return true;
        }
        return false;
    }

    public isReaktivierungMoeglich(): boolean {
        if (
            this.isSozialdienstFallEntzogen() &&
            this.dokumente &&
            this.dokumente.length > 0
        ) {
            return true;
        }
        return false;
    }

    public async fallAktivieren(): Promise<void> {
        this.form.$dirty = true;
        if (
            this.validateForm() &&
            this.dokumente &&
            this.dokumente.length > 0
        ) {
            await this.saveData();
            this.unterstuetzungsdienstFallService
                .sozialdienstFallEroeffnen(this.gesuchModelManager.getFall().id)
                .subscribe(sozialdienstFall => {
                    this.gesuchModelManager.getFall().sozialdienstFall =
                        sozialdienstFall;
                    this.wizardStepManager.updateCurrentWizardStepStatus(
                        TSWizardStepStatus.OK
                    );
                });
        }
    }

    public fallEntziehen(): void {
        this.dvDialog
            .showRemoveDialog(
                removeDialogTempl,
                this.form,
                RemoveDialogController,
                {
                    title: 'CONFIRM_VOLLMACHT_ENTZIEHEN',
                    deleteText: 'BESCHREIBUNG_VOLLMACHT_ENTZIEHEN',
                    parentController: undefined,
                    elementID: undefined
                }
            )
            .then(() => {
                this.unterstuetzungsdienstFallService
                    .sozialdienstFallEntziehen(
                        this.gesuchModelManager.getFall().id
                    )
                    .then(fall => {
                        this.gesuchModelManager.getDossier().fall = fall;
                    });
            });
    }

    public uploadVollmachtDokument(event: any): void {
        const files = event.target.files;
        if (files.length <= 0) {
            return;
        }
        const selectedFile = files[0];
        if (selectedFile.size > MAX_FILE_SIZE) {
            this.dvDialog.showDialog(
                okHtmlDialogTempl,
                OkHtmlDialogController,
                {
                    title: this.$translate.instant('FILE_ZU_GROSS')
                }
            );
            return;
        }

        this.uploadRS
            .uploadVollmachtDokument(
                selectedFile,
                this.gesuchModelManager.getFall().id
            )
            .then(dokumente => {
                this.dokumente = this.dokumente.concat(dokumente);
            });
    }

    public removeVollmachtDokument(dokument: TSSozialdienstFallDokument): void {
        this.unterstuetzungsdienstFallService
            .removeVollmachtDokument(dokument.id)
            .then(() => {
                this.dokumente = this.dokumente.filter(
                    d => d.id !== dokument.id
                );
            });
    }

    public downloadVollmachtDokument(
        dokument: TSSozialdienstFallDokument,
        attachment: boolean
    ): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.downloadRS
            .getAccessTokenSozialdienstFallDokument(dokument.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownloadGeneratedPDF(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    attachment,
                    win
                );
            })
            .catch(() => {
                win.close();
            });
    }

    public generateVollmachtPDF(sprache: TSSprache): void {
        this.unterstuetzungsdienstFallService
            .getVollmachtDokumentAccessTokenGeneratedDokument(
                this.gesuchModelManager.getFall().id,
                sprache
            )
            .then(response => {
                this.openDownloadForFile(response);
            });
    }

    private openDownloadForFile(response: BlobPart): void {
        let filename;
        const file = new Blob([response], {type: 'application/pdf'});
        filename = this.$translate.instant('VOLLMACHT_DATEI_NAME');
        filename = `${filename}_${this.getSozialdienstFall().vorname}_${this.getSozialdienstFall().name}`;
        this.downloadRS.openDownload(file, filename);
    }

    private validateZweiteAntragsteller(): void {
        if (
            (!EbeguUtil.isEmptyStringNullOrUndefined(
                this.getSozialdienstFall().nameGs2
            ) ||
                !EbeguUtil.isEmptyStringNullOrUndefined(
                    this.getSozialdienstFall().vornameGs2
                ) ||
                EbeguUtil.isNotNullOrUndefined(
                    this.getSozialdienstFall().geburtsdatumGs2
                )) &&
            (EbeguUtil.isEmptyStringNullOrUndefined(
                this.getSozialdienstFall().nameGs2
            ) ||
                EbeguUtil.isEmptyStringNullOrUndefined(
                    this.getSozialdienstFall().vornameGs2
                ) ||
                EbeguUtil.isNullOrUndefined(
                    this.getSozialdienstFall().geburtsdatumGs2
                ))
        ) {
            this.showAntragsteller2Error = true;
        }
    }

    public getSozialdienstFall(): TSSozialdienstFall {
        return this.gesuchModelManager.getFall().sozialdienstFall;
    }

    public isFormDirty(): boolean {
        return this.form.$dirty;
    }

    public isAntragBearbeitbar(): boolean {
        return (
            this.isSozialdienstFallAktiv() &&
            this.gesuchModelManager.getGesuch() &&
            (this.gesuchModelManager.getGesuch().isMutation() ||
                this.gesuchModelManager.getGesuch().isFolgegesuch()) &&
            this.isGesuchInStatus(TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST)
        );
    }

    public bearbeiten(): void {
        this.unterstuetzungsdienstFallService
            .sozialdienstFallInaktivieren(this.gesuchModelManager.getFall().id)
            .subscribe(sozialdienstFall => {
                this.gesuchModelManager.getFall().sozialdienstFall =
                    sozialdienstFall;
                this.wizardStepManager.updateCurrentWizardStepStatus(
                    TSWizardStepStatus.NOK
                );
            });
    }

    public getSozialdienstName(): string {
        return this.gesuchModelManager.getFall().sozialdienstFall.sozialdienst
            .name;
    }
}
