/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {StateService} from '@uirouter/core';
import {
    copy,
    element,
    equals,
    IComponentOptions,
    IFormController,
    ILogService
} from 'angular';
import {map} from 'rxjs/operators';
import {Permission} from '../../../app/authorisation/Permission';
import {PERMISSIONS} from '../../../app/authorisation/Permissions';
import {IDVFocusableController} from '../../../app/core/component/IDVFocusableController';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {MitteilungRS} from '../../../app/core/service/mitteilungRS.rest';
import {SozialdienstRS} from '../../../app/core/service/SozialdienstRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSGesuchsperiode} from '../../../models/entity/TSGesuchsperiode';
import {TSInstitutionStammdatenSummary} from '../../../models/entity/TSInstitutionStammdatenSummary';
import {
    isAnyStatusOfVerfuegt,
    isAtLeastFreigegebenOrFreigabequittung,
    isStatusVerfuegenVerfuegt
} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSCreationAction} from '../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSMitteilungEvent} from '../../../models/enums/TSMitteilungEvent';
import {TSRole} from '../../../models/enums/TSRole';
import {TSSozialdienstFallStatus} from '../../../models/enums/TSSozialdienstFallStatus';
import {MANDANTS} from '@models/mandant';
import {TSSozialdienstStammdaten} from '../../../models/sozialdienst/TSSozialdienstStammdaten';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSDossier} from '../../../models/TSDossier';
import {TSGesuch} from '../../../models/TSGesuch';
import {ApplicationPropertyRsService} from '../../../utils/application-property-rs/application-property-rs.service';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {MandantService} from '../../../utils/mandant-service/mandant.service';
import {NavigationUtil} from '../../../utils/NavigationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import {DossierRS} from '../../service/dossierRS.rest';
import {GemeindeRS} from '../../service/gemeindeRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITranslateService = angular.translate.ITranslateService;
import {GemeindeKontaktdatenController} from '../../dialog/GemeindeKontaktdatenController';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {firstValueFrom} from 'rxjs';

const showKontaktTemplate = require('../../../gesuch/dialog/showKontaktTemplate.html');
const removeDialogTempl = require('../../dialog/removeDialogTemplate.html');

// TODO hefa multiple components in 1 file!?

export class DossierToolbarComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        gesuchid: '@',
        dossierId: '@',
        isDashboardScreen: '@',
        hideActionButtons: '@',
        forceLoadingFromFall: '@'
    };

    public template = require('./dossierToolbar.html');
    public controller = DossierToolbarController;
    public controllerAs = 'vmx';
}

export class DossierToolbarGesuchstellerComponentConfig
    implements IComponentOptions
{
    public transclude = false;
    public bindings = {
        gesuchid: '@',
        dossierId: '@',
        fallId: '@',
        isDashboardScreen: '@',
        hideActionButtons: '@',
        forceLoadingFromFall: '@'
    };
    public template = require('./dossierToolbarGesuchsteller.html');
    public controller = DossierToolbarController;
    // Darf, wie es scheint nicht 'vm' heissen, sonst werden im dossierToolBarGesuchsteller.html keine Funktionen
    // gefunden. Bug?!
    public controllerAs = 'vmgs';
}

export class DossierToolbarController implements IDVFocusableController {
    public static $inject = [
        'EbeguUtil',
        'GesuchRS',
        '$state',
        '$scope',
        'GesuchModelManager',
        'AuthServiceRS',
        '$mdSidenav',
        '$log',
        'GesuchsperiodeRS',
        'DvDialog',
        'unsavedWarningSharedService',
        'MitteilungRS',
        'DossierRS',
        'GemeindeRS',
        'SozialdienstRS',
        '$translate',
        'MandantService',
        'ApplicationPropertyRsService'
    ];

    public antragList: Array<TSAntragDTO>;
    public gesuchid: string;
    public isDashboardScreen: boolean;
    public hideActionButtons: boolean;
    public readonly TSRoleUtil = TSRoleUtil;
    public forceLoadingFromFall: boolean;
    public dossierId: string;
    public fallId: string;
    public dossier: TSDossier;
    public kontaktdatenGemeindeAsHtml: string;
    public isLuzern: boolean;

    public gesuchsperiodeList: {[key: string]: Array<TSAntragDTO>} = {};
    public gesuchNavigationList: {[key: string]: Array<string>} = {}; // mapped z.B. '2006 / 2007' auf ein array
    // mit den
    // Namen der Antraege
    public antragTypList: {[key: string]: TSAntragDTO} = {};
    public gemeindeId: string;
    public gemeindeInstitutionKontakteHtml: string;
    public gemeindeSozialdienstKontakteHtml: string;
    public mutierenPossibleForCurrentAntrag: boolean = false;
    public erneuernPossibleForCurrentAntrag: boolean = false;
    public neuesteGesuchsperiode: TSGesuchsperiode;
    public amountNewMitteilungenGS: number = 0;
    private angebotTS: boolean;

    public constructor(
        private readonly ebeguUtil: EbeguUtil,
        private readonly gesuchRS: GesuchRS,
        private readonly $state: StateService,
        private readonly $scope: IScope,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $mdSidenav: ng.material.ISidenavService,
        private readonly $log: ILogService,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly dvDialog: DvDialog,
        private readonly unsavedWarningSharedService: any,
        private readonly mitteilungRS: MitteilungRS,
        private readonly dossierRS: DossierRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly sozialdienstRS: SozialdienstRS,
        private readonly $translate: ITranslateService,
        private readonly mandantService: MandantService,
        private readonly applicationPropertyRS: ApplicationPropertyRsService
    ) {}

    public $onInit(): void {
        this.updateAntragDTOList();
        // add watchers
        this.addWatchers(this.$scope);
        if (EbeguUtil.isEmptyStringNullOrUndefined(this.dossierId)) {
            return;
        }
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(res => {
                this.angebotTS = res.angebotTSActivated;
            });
        this.gesuchsperiodeRS
            .getActiveGesuchsperiodenForDossier(this.dossierId)
            .then((response: TSGesuchsperiode[]) => {
                // Die neueste ist zuoberst
                this.neuesteGesuchsperiode = response[0];
                this.updateAntragErneuernPossible();
            });
        this.mandantService.mandant$
            .pipe(map(mandant => mandant === MANDANTS.LUZERN))
            .subscribe(
                isLuzern => {
                    this.isLuzern = isLuzern;
                },
                error => this.$log.error(error)
            );
    }

    private updateAmountNewMitteilungenGS(): void {
        if (
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())
        ) {
            return;
        }
        this.mitteilungRS
            .getAmountNewMitteilungenOfDossierForCurrentRolle(this.dossierId)
            .then((response: number) => {
                this.amountNewMitteilungenGS = response;
            });
    }

    public getAmountNewMitteilungenGS(): string {
        return `(${this.amountNewMitteilungenGS})`;
    }

    public toggleSidenav(componentId: string): void {
        this.$mdSidenav(componentId).toggle();
    }

    public closeSidenav(componentId: string): void {
        this.$mdSidenav(componentId).close();
    }

    public logout(): void {
        this.$state.go('authentication.login', {type: 'logout'});
    }

    private addWatchers($scope: IScope): void {
        // needed because of test is not able to inject $scope!
        if (!$scope) {
            return;
        }

        $scope.$watch(
            () => this.gesuchid,
            (newValue, oldValue) => {
                if (newValue === oldValue) {
                    return;
                }
                if (this.gesuchid) {
                    this.updateAntragDTOList();
                    return;
                }
                this.gemeindeId = null;
                this.antragTypList = {};
                this.gesuchNavigationList = {};
                this.gesuchsperiodeList = {};
                this.antragList = [];
                this.updateAntragMutierenPossible(); // neu berechnen ob mutieren moeglich ist
                this.updateAntragErneuernPossible();
            }
        );
        if (this.gesuchModelManager && this.getGesuch()) {
            $scope.$watch(
                () => {
                    if (this.getGesuch()) {
                        return this.getGesuch().status;
                    }
                    return undefined;
                },
                (newValue, oldValue) => {
                    if (
                        newValue !== oldValue &&
                        isAnyStatusOfVerfuegt(newValue)
                    ) {
                        this.updateAntragDTOList();
                    }
                }
            );
        }
        $scope.$watch(
            () => this.dossierId,
            (newValue, oldValue) => {
                if (newValue === oldValue) {
                    return;
                }
                if (this.dossierId) {
                    this.updateAntragDTOList();
                    this.updateAmountNewMitteilungenGS();
                } else {
                    // Fall-ID hat auf undefined gewechselt -> Fall zuruecksetzen
                    this.dossierId = undefined;
                    this.antragTypList = {};
                    this.gesuchNavigationList = {};
                    this.gesuchsperiodeList = {};
                    this.antragList = [];
                    this.updateAntragMutierenPossible(); // neu berechnen ob mutieren moeglich ist
                    this.updateAntragErneuernPossible();
                }
            }
        );
        $scope.$on(
            TSMitteilungEvent[
                TSMitteilungEvent.MUTATIONSMITTEILUNG_NEUE_MUTATION
            ],
            () => {
                this.updateAntragDTOList();
            }
        );
    }

    public showGesuchPeriodeNavigationMenu(): boolean {
        return (
            !this.isDashboardScreen &&
            !equals(this.gesuchsperiodeList, {}) &&
            !this.authServiceRS.isRole(TSRole.STEUERAMT)
        );
    }

    /**
     * Die Liste wird nicht angezeigt wenn sie leer ist oder wenn der Benutzer sich auf dem Dashboard befindet
     */
    public showAntragTypListNavigationMenu(): boolean {
        return (
            !this.isDashboardScreen &&
            !equals(this.antragTypList, {}) &&
            !this.authServiceRS.isRole(TSRole.STEUERAMT)
        );
    }

    public showKontaktMenu(): boolean {
        return (
            this.getGesuch() &&
            !!this.getGesuch().gesuchsteller1 &&
            !this.authServiceRS.isOneOfRoles(TSRoleUtil.getSozialdienstRolle())
        );
    }

    public updateAntragDTOList(): void {
        if (this.dossierId) {
            this.dossierRS
                .findDossier(this.dossierId)
                .then((response: TSDossier) => {
                    if (!response) {
                        return;
                    }
                    this.dossier = response;
                    this.gemeindeId = this.dossier.gemeinde.id;

                    this.setGemeindeInstitutionKontakteHtmlIfTraegerschaftInstitution();
                    this.setGemeindeSozialdienstKontakteHtmIfSozialdienstFall();

                    if (!this.forceLoadingFromFall && this.getGesuch()?.id) {
                        this.getAllAntragDTOAndUpdate();
                    } else if (this.dossier) {
                        this.getAllAntragDTOForDossierAndUpdateAndInitialize();
                        this.updateAmountNewMitteilungenGS();
                    } else {
                        this.resetNavigationParameters();
                    }

                    if (
                        this.authServiceRS.isOneOfRoles(
                            PERMISSIONS[Permission.ROLE_GEMEINDE]
                        ) &&
                        this.getGesuch()
                    ) {
                        this.gemeindeInstitutionKontakteHtml =
                            this.institutionenStammdatenToHtml();
                    }
                });
        }
        this.forceLoadingFromFall = false; // reset it because it's not needed any more
    }

    private gemeindeStammdatenToHtml(stammdaten: TSGemeindeStammdaten): string {
        const htmlIntro = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGesuchstellerSozialdienstRolle()
        )
            ? `<h3 class="margin-top-20">${this.$translate.instant('BEI_FRAGEN_GEMEINDE_KONTAKTIEREN')}</h3>`
            : ``;
        let html = ``;
        html += `${htmlIntro}`;
        if (
            !stammdaten.bgAdresse ||
            !stammdaten.tsAdresse ||
            !stammdaten.gemeinde.angebotTS ||
            !stammdaten.gemeinde.angebotBG
        ) {
            html += `<h3>${
                stammdaten.adresse.organisation
                    ? stammdaten.adresse.organisation
                    : ''
            }</h3>`;
            html += `
                <span>${stammdaten.adresse.strasse} ${stammdaten.adresse.hausnummer}</span>
                <span>${stammdaten.adresse.plz} ${stammdaten.adresse.ort}</span>
                ${
                    !stammdaten.tsEmail || !stammdaten.bgEmail
                        ? `<a href="mailto:${stammdaten.mail}">${stammdaten.mail}</a>`
                        : ``
                }`;
            if (!stammdaten.bgTelefon || !stammdaten.tsTelefon) {
                html += `<a href="tel:${stammdaten.telefon}">${stammdaten.telefon}</a>`;
            }
        }
        if (stammdaten.bgAdresse && stammdaten.gemeinde.angebotBG) {
            html += `<h3 class="margin-top-20">${this.$translate.instant('KONTAKT_BETREUUNGSGUTSCHEINE_TITLE')}</h3>`;
            html += `<span>${
                stammdaten.bgAdresse.organisation
                    ? stammdaten.bgAdresse.organisation
                    : ''
            }</span>`;
            html += `<span>${stammdaten.bgAdresse.strasse} ${stammdaten.bgAdresse.hausnummer}</span>`;
            html += `<span>${stammdaten.bgAdresse.plz} ${stammdaten.bgAdresse.ort}</span>`;
            html += `<a href="mailto:${stammdaten.bgEmail || stammdaten.mail}">${stammdaten.bgEmail || stammdaten.mail}</a>`;
            html += `<a href="tel:${stammdaten.bgTelefon || stammdaten.telefon}">${stammdaten.bgTelefon || stammdaten.telefon}</a>`;
        }
        if (stammdaten.tsAdresse && stammdaten.gemeinde.angebotTS) {
            html += `<h3 class="margin-top-20">${this.$translate.instant('KONTAKT_TAGESCHSCHULANMELDUNGEN_TITLE')}</h3>`;
            html += `<span>${
                stammdaten.tsAdresse.organisation
                    ? stammdaten.tsAdresse.organisation
                    : ''
            }</span>`;
            html += `<span>${stammdaten.tsAdresse.strasse} ${stammdaten.tsAdresse.hausnummer}</span>`;
            html += `<span>${stammdaten.tsAdresse.plz} ${stammdaten.tsAdresse.ort}</span>`;
            html += `<a href="tel:${stammdaten.tsEmail || stammdaten.mail}">${stammdaten.tsEmail || stammdaten.mail}</a>`;
            html += `<a href="tel:${stammdaten.tsTelefon || stammdaten.telefon}">${stammdaten.tsTelefon || stammdaten.telefon}</a>`;
        }
        return html;
    }

    private resetNavigationParameters(): void {
        this.gesuchsperiodeList = {};
        this.gesuchNavigationList = {};
        this.antragTypList = {};
        this.updateAntragMutierenPossible();
        this.updateAntragErneuernPossible();
    }

    private updateGesuchperiodeList(): void {
        this.gesuchsperiodeList = {};
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const gs = this.antragList[i].gesuchsperiodeString;

            if (!this.gesuchsperiodeList[gs]) {
                this.gesuchsperiodeList[gs] = [];
            }
            this.gesuchsperiodeList[gs].push(this.antragList[i]);
        }
    }

    private updateGesuchNavigationList(): void {
        this.gesuchNavigationList = {}; // clear
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const gs = this.antragList[i].gesuchsperiodeString;
            const antrag = this.antragList[i];

            if (!this.gesuchNavigationList[gs]) {
                this.gesuchNavigationList[gs] = [];
            }
            this.gesuchNavigationList[gs].push(
                this.ebeguUtil.getAntragTextDateAsString(
                    antrag.antragTyp,
                    antrag.eingangsdatum,
                    antrag.laufnummer
                )
            );
        }
    }

    private updateAntragTypList(): void {
        this.antragTypList = {}; // clear
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antrag = this.antragList[i];
            if (
                !this.getGesuch().gesuchsperiode.gueltigkeit.gueltigAb.isSame(
                    antrag.gesuchsperiodeGueltigAb
                )
            ) {
                continue;
            }

            const txt = this.ebeguUtil.getAntragTextDateAsString(
                antrag.antragTyp,
                antrag.eingangsdatum,
                antrag.laufnummer
            );
            this.antragTypList[txt] = antrag;
        }
    }

    // eslint-disable-next-line @typescript-eslint/no-shadow
    public getKeys(map: {[key: string]: Array<TSAntragDTO>}): Array<string> {
        const keys: Array<string> = [];
        for (const key in map) {
            if (map.hasOwnProperty(key)) {
                keys.push(key);
            }
        }
        return keys;
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getCurrentGesuchsperiode(): string {
        return this.getGesuch() && this.getGesuch().gesuchsperiode
            ? this.getGesuchsperiodeAsString(this.getGesuch().gesuchsperiode)
            : '';
    }

    public getAntragTyp(): string {
        return this.getGesuch()
            ? this.ebeguUtil.getAntragTextDateAsString(
                  this.getGesuch().typ,
                  this.getGesuch().eingangsdatum,
                  this.getGesuch().laufnummer
              )
            : '';
    }

    public getGesuchsperiodeAsString(
        tsGesuchsperiode: TSGesuchsperiode
    ): string {
        return tsGesuchsperiode.gesuchsperiodeString;
    }

    public setGesuchsperiode(gesuchsperiodeKey: string): void {
        const selectedGesuche = this.gesuchsperiodeList[gesuchsperiodeKey];
        const selectedGesuch = this.getNewest(selectedGesuche);

        this.goToOpenGesuch(selectedGesuch.antragId);
    }

    private getNewest(arrayTSAntragDTO: Array<TSAntragDTO>): TSAntragDTO {
        let newest = arrayTSAntragDTO[0];
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < arrayTSAntragDTO.length; i++) {
            // Wenn eines noch gar kein Eingangsdatum hat ist es sicher das neueste
            if (!arrayTSAntragDTO[i].eingangsdatum) {
                return arrayTSAntragDTO[i];
            }
            if (
                arrayTSAntragDTO[i].eingangsdatum.isAfter(newest.eingangsdatum)
            ) {
                newest = arrayTSAntragDTO[i];
            }
        }
        return newest;
    }

    /**
     * Institutionen werden zum Screen Betreuungen geleitet, waehrend alle anderen Benutzer zu fallCreation gehen
     */
    private goToOpenGesuch(gesuchId: string): void {
        if (!gesuchId) {
            return;
        }

        NavigationUtil.navigateToStartsiteOfGesuchForRole(
            this.authServiceRS.getPrincipalRole(),
            this.$state,
            gesuchId
        );
    }

    public setAntragTypDatum(antragTypDatumKey: string): void {
        const selectedAntragTypGesuch = this.antragTypList[antragTypDatumKey];
        this.goToOpenGesuch(selectedAntragTypGesuch.antragId);
    }

    public setAntragTypDatumMobile(
        gesuchperiodeKey: string,
        antragTypDatumKey: string
    ): void {
        const tmpAntragList: {[key: string]: TSAntragDTO} = {};
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antrag = this.antragList[i];
            const gesuchsperiodeGueltigAb =
                this.gesuchsperiodeList[gesuchperiodeKey][0]
                    .gesuchsperiodeGueltigAb;
            if (
                !gesuchsperiodeGueltigAb.isSame(antrag.gesuchsperiodeGueltigAb)
            ) {
                continue;
            }

            const txt = this.ebeguUtil.getAntragTextDateAsString(
                antrag.antragTyp,
                antrag.eingangsdatum,
                antrag.laufnummer
            );
            tmpAntragList[txt] = antrag;
        }
        const selectedAntragTypGesuch = tmpAntragList[antragTypDatumKey];
        this.goToOpenGesuch(selectedAntragTypGesuch.antragId);
    }

    public showButtonMutieren(): boolean {
        if (this.hideActionButtons) {
            return false;
        }
        if (this.getGesuch()) {
            if (this.getGesuch().isNew()) {
                return false;
            }
            // Wenn die Gesuchsperiode geschlossen ist, kann sowieso keine Mutation mehr gemacht werden
            if (
                this.getGesuch().gesuchsperiode &&
                this.getGesuch().gesuchsperiode.status ===
                    TSGesuchsperiodeStatus.GESCHLOSSEN
            ) {
                return false;
            }
            // der Button soll für den Gesuchsteller ausgeblendet werden,
            if (
                this.authServiceRS.isRole(TSRole.GESUCHSTELLER) &&
                (!this.gesuchModelManager.isNeuestesGesuch() ||
                    !this.gesuchModelManager.getGesuch().isOnlineGesuch()) // GS must not mutate "Papieranträge"
            ) {
                return false;
            }
        }
        return this.mutierenPossibleForCurrentAntrag;
    }

    private updateAntragMutierenPossible(): void {
        if (!this.antragList || this.antragList.length === 0) {
            this.mutierenPossibleForCurrentAntrag = false;
            return;
        }

        let mutierenGesperrt = false;
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antragItem = this.antragList[i];
            // Wir muessen nur die Antraege der aktuell ausgewaehlten Gesuchsperiode beachten
            if (
                antragItem.gesuchsperiodeString !==
                this.getCurrentGesuchsperiode()
            ) {
                continue;
            }

            if (!antragItem.verfuegt || antragItem.beschwerdeHaengig) {
                mutierenGesperrt = true;
                break;
            }
        }
        if (this.isSZFallGesperrtFuerSZ()) {
            mutierenGesperrt = true;
        }
        this.mutierenPossibleForCurrentAntrag = !mutierenGesperrt;
    }

    public antragMutieren(): void {
        this.mutierenPossibleForCurrentAntrag = false;
        const eingangsart = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGesuchstellerSozialdienstRolle()
        )
            ? TSEingangsart.ONLINE
            : TSEingangsart.PAPIER;
        this.$state.go('gesuch.mutation', {
            creationAction: TSCreationAction.CREATE_NEW_MUTATION,
            eingangsart,
            gesuchsperiodeId: this.getGesuch().gesuchsperiode.id,
            gesuchId: this.getGesuchIdFuerMutationOrErneuerung(),
            dossierId: this.getGesuch().dossier.id
        });
    }

    private updateAntragErneuernPossible(): void {
        if (!this.antragList || this.antragList.length === 0) {
            this.erneuernPossibleForCurrentAntrag = false;
            return;
        }
        let erneuernGesperrt = false;
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let i = 0; i < this.antragList.length; i++) {
            const antragItem = this.antragList[i];
            // Wir muessen nur die Antraege der aktuell ausgewaehlten Gesuchsperiode beachten
            if (
                antragItem.gesuchsperiodeString ===
                this.getGesuchsperiodeAsString(this.neuesteGesuchsperiode)
            ) {
                // Es gibt schon (mindestens 1) Gesuch für die neueste Periode
                erneuernGesperrt = true;
                break;
            }
            // Wenn das Erstgesuch der Periode ein Online Gesuch war, darf dieser *nur* durch den GS selber
            // erneuert werden. JA/SCH muss einen neuen Fall eröffnen, da Papier und Online Gesuche nie vermischt
            // werden duerfen!
            if (
                antragItem.eingangsart !== TSEingangsart.ONLINE ||
                antragItem.antragTyp === TSAntragTyp.MUTATION
            ) {
                continue;
            }
            if (
                !this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getGesuchstellerSozialdienstRolle()
                )
            ) {
                erneuernGesperrt = true;
                break;
            }
        }
        if (this.isSZFallGesperrtFuerSZ()) {
            erneuernGesperrt = true;
        }
        this.erneuernPossibleForCurrentAntrag = !erneuernGesperrt;
    }

    public antragErneuern(): void {
        this.erneuernPossibleForCurrentAntrag = false;
        const eingangsart = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGesuchstellerSozialdienstRolle()
        )
            ? TSEingangsart.ONLINE
            : TSEingangsart.PAPIER;
        this.$state.go('gesuch.erneuerung', {
            creationAction: TSCreationAction.CREATE_NEW_FOLGEGESUCH,
            eingangsart,
            gesuchsperiodeId: this.neuesteGesuchsperiode.id,
            dossierId: this.dossier.id,
            gesuchId: this.getGesuchIdFuerMutationOrErneuerung()
        });
    }

    private getGesuchIdFuerMutationOrErneuerung(): string {
        // GesuchId ermitteln fuer Mutation ermitteln: Falls wir auf der Verlauf-View sind, nehmen wir einfach
        // irgendeines der Liste (es wird auf dem Server sichergestellt, dass die Mutation ab dem neuesten Gesuch
        // der Periode gemacht wird), wichtig ist nur, dass es sich um die richtige Gesuchsperiode handelt.
        if (this.gesuchid) {
            return this.gesuchid;
        }

        return this.getGesuch() ? this.getGesuch().id : undefined;
    }

    public openMitteilungen(): void {
        this.$state.go('mitteilungen.view', {
            dossierId: this.dossier.id,
            fallId: this.dossier.fall.id
        });
    }

    public showVerlauf(): boolean {
        return (
            this.getGesuch() !== null &&
            this.getGesuch() !== undefined &&
            !this.getGesuch().isNew()
        );
    }

    public openVerlauf(): void {
        this.$state.go('verlauf.view', {
            gesuchId: this.getGesuch().id
        });
    }

    public showGesuchLoeschen(): boolean {
        if (!this.getGesuch() || this.getGesuch().isNew()) {
            return false;
        }

        if (
            this.authServiceRS.isOneOfRoles(
                this.TSRoleUtil.getGesuchstellerSozialdienstRolle()
            )
        ) {
            // GS darf nur vor der Freigabe loeschen
            if (
                this.hideActionButtons ||
                this.isDashboardScreen ||
                isAtLeastFreigegebenOrFreigabequittung(this.getGesuch().status)
            ) {
                return false;
            }
        } else if (
            isStatusVerfuegenVerfuegt(this.getGesuch().status) ||
            this.getGesuch().eingangsart === TSEingangsart.ONLINE
        ) {
            // JA: Darf nicht verfuegen oder verfuegt sein und muss Papier sein
            return false;
        }
        return !(
            this.getGesuch().dossier.fall.sozialdienstFall &&
            this.getGesuch().dossier.fall.sozialdienstFall.status ===
                TSSozialdienstFallStatus.ENTZOGEN
        );
    }

    public gesuchLoeschen(): IPromise<void> {
        const titleDialog = this.getGesuch().isMutation()
            ? 'CONFIRM_MUTATION_LOESCHEN'
            : 'CONFIRM_GESUCH_LOESCHEN';
        return this.dvDialog
            .showRemoveDialog(
                removeDialogTempl,
                undefined,
                RemoveDialogController,
                {
                    title: titleDialog,
                    deleteText: 'BESCHREIBUNG_GESUCH_LOESCHEN',
                    parentController: this,
                    elementID: 'gesuchLoeschenButton'
                }
            )
            .then(() => {
                this.setAllFormsPristine();
                this.gesuchRS.removeAntrag(this.getGesuch().id).then(() => {
                    if (
                        this.authServiceRS.isOneOfRoles(
                            this.TSRoleUtil.getGesuchstellerOnlyRoles()
                        )
                    ) {
                        this.gesuchModelManager.setGesuch(new TSGesuch());
                        this.resetNavigationParameters();
                        this.$state.go('gesuchsteller.dashboard');
                    } else {
                        if (this.antragList.length <= 1) {
                            this.$state.go('pendenzen.list-view');
                            return;
                        }
                        const navObj: any = {
                            gesuchId: this.antragList[0].antragId,
                            dossierId: this.antragList[0].dossierId
                        };
                        this.$state.go('gesuch.fallcreation', navObj);
                    }
                });
            });
    }

    public gesuchLoeschenForced(): IPromise<void> {
        return this.dvDialog
            .showRemoveDialog(
                removeDialogTempl,
                undefined,
                RemoveDialogController,
                {
                    title: 'CONFIRM_GESUCH_LOESCHEN_FORCED',
                    deleteText: 'BESCHREIBUNG_GESUCH_LOESCHEN_FORCED',
                    parentController: this,
                    elementID: 'gesuchLoeschenForcedButton'
                }
            )
            .then(() => {
                this.setAllFormsPristine();
                this.gesuchRS
                    .removeAntragForced(this.getGesuch().id)
                    .then(() => {
                        this.resetNavigationParameters();
                        this.$state.go('faelle.list');
                    });
            });
    }

    private setAllFormsPristine(): void {
        const forms: [IFormController] =
            this.unsavedWarningSharedService.allForms();
        // eslint-disable-next-line @typescript-eslint/prefer-for-of
        for (let index = 0; index < forms.length; index++) {
            const form = forms[index];
            form.$setPristine();
            form.$setUntouched();
        }
    }

    public openAlleVerfuegungen(): void {
        if (!this.dossier) {
            return;
        }
        this.$state.go('alleVerfuegungen.view', {
            dossierId: this.dossier.id
        });
    }

    public showKontakt(): void {
        this.gemeindeRS
            .getGemeindeStammdaten(this.gemeindeId)
            .then(stammdaten => {
                this.dvDialog.showDialog(
                    showKontaktTemplate,
                    GemeindeKontaktdatenController,
                    {
                        parentController: this,
                        stammdaten: stammdaten
                    }
                );
            });
    }

    private institutionStammdatenToHtml(
        stammdaten: TSInstitutionStammdatenSummary
    ): string {
        let html = '';
        if (stammdaten.adresse.organisation === stammdaten.institution.name) {
            html += `<span class="margin-top-20">${stammdaten.institution.name}</span>`;
        } else {
            html += `<span class="margin-top-20">${stammdaten.adresse.organisation ? stammdaten.adresse.organisation : ''}
                          ${stammdaten.institution.name}</span>`;
        }
        html += `<span>${stammdaten.adresse.strasse} ${stammdaten.adresse.hausnummer}</span>
                    <span>${stammdaten.adresse.plz} ${stammdaten.adresse.ort}</span>
                    <a href="mailto:${stammdaten.mail}">${stammdaten.mail}</a>`;
        html += stammdaten.telefon
            ? `<a href="tel:${stammdaten.telefon}">${stammdaten.telefon}</a>`
            : '';
        return html;
    }

    private institutionenStammdatenToHtml(): string {
        let html = '';
        const institutionIds: Array<string> = [];
        for (const kc of this.getGesuch().kindContainers) {
            for (const be of kc.betreuungen) {
                if (
                    !institutionIds.includes(
                        be.institutionStammdaten.institution.id
                    )
                ) {
                    institutionIds.push(
                        be.institutionStammdaten.institution.id
                    );
                    html += this.institutionStammdatenToHtml(
                        be.institutionStammdaten
                    );
                }
            }
        }
        return html;
    }

    private sozialdienstStammdatenToHtml(
        stammdaten: TSSozialdienstStammdaten
    ): string {
        let html = `<span class="margin-top-20">${
            stammdaten.adresse.organisation
                ? stammdaten.adresse.organisation
                : ''
        }
                          ${stammdaten.sozialdienst.name}</span>
                    <span>${stammdaten.adresse.strasse} ${stammdaten.adresse.hausnummer}</span>
                    <span>${stammdaten.adresse.plz} ${stammdaten.adresse.ort}</span>
                    <a href="mailto:${stammdaten.mail}">${stammdaten.mail}</a>`;
        html += stammdaten.telefon
            ? `<a href="tel:${stammdaten.telefon}">${stammdaten.telefon}</a><br>`
            : '';
        return html;
    }

    /**
     * Sets the focus back to the Kontakt icon.
     */
    public setFocusBack(): void {
        element('#kontaktButton').first().focus();
    }

    public getGesuchName(): string {
        return this.gesuchModelManager.getGesuchName();
    }

    public showVerantwortlicher(): boolean {
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle()
        );
    }

    private isSZFallGesperrtFuerSZ(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getSozialdienstRolle()
            ) &&
            this.dossier.fall.isSozialdienstFall() &&
            this.dossier.fall.sozialdienstFall.status ===
                TSSozialdienstFallStatus.ENTZOGEN
        );
    }

    public isTagesschulangebotEnabled(): boolean {
        return this.angebotTS;
    }

    private setGemeindeInstitutionKontakteHtmlIfTraegerschaftInstitution(): void {
        if (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        ) {
            this.gemeindeRS
                .getGemeindeStammdaten(this.gemeindeId)
                .then(stammdaten => {
                    this.gemeindeInstitutionKontakteHtml =
                        this.gemeindeStammdatenToHtml(stammdaten);
                });
        }
    }

    private setGemeindeSozialdienstKontakteHtmIfSozialdienstFall(): void {
        if (this.dossier.fall.sozialdienstFall) {
            firstValueFrom(
                this.sozialdienstRS.getSozialdienstStammdaten(
                    this.dossier.fall.sozialdienstFall.sozialdienst.id
                )
            ).then(
                stammdaten =>
                    (this.gemeindeSozialdienstKontakteHtml =
                        this.sozialdienstStammdatenToHtml(stammdaten))
            );
        }
    }

    private getAllAntragDTOForDossierAndUpdateAndInitialize() {
        this.gesuchRS
            .getAllAntragDTOForDossier(this.dossier.id)
            .then(antraege => {
                this.antragList = copy(antraege);
                if (antraege && antraege.length > 0) {
                    const newest = this.getNewest(this.antragList);
                    this.gesuchRS.findGesuch(newest.antragId).then(gesuch => {
                        if (!gesuch) {
                            this.$log.warn(
                                `Could not find gesuch for id ${newest.antragId}`
                            );
                        }
                        this.gesuchModelManager.setGesuch(copy(gesuch));
                        this.updateListsAndProperties();
                    });
                } else if (
                    !this.gesuchModelManager.getGesuch() ||
                    !this.gesuchModelManager.getGesuch().isNew()
                ) {
                    // Wenn das Gesuch noch neu ist, sind wir noch ungespeichert auf der FallCreation-Seite
                    // In diesem Fall durfen wir das Gesuch nicht zuruecksetzen
                    const gesuch = new TSGesuch();
                    gesuch.dossier = copy(this.dossier);
                    this.gesuchModelManager.setGesuch(gesuch);
                    this.resetNavigationParameters();
                }
            });
    }

    private getAllAntragDTOAndUpdate() {
        this.gesuchRS
            .getAllAntragDTOForDossier(this.getGesuch().dossier.id)
            .then(antraege => {
                this.antragList = antraege;
                this.updateListsAndProperties();
            });
    }

    private updateListsAndProperties() {
        this.updateGesuchperiodeList();
        this.updateGesuchNavigationList();
        this.updateAntragTypList();
        this.updateAntragMutierenPossible();
        this.updateAntragErneuernPossible();
    }
}
