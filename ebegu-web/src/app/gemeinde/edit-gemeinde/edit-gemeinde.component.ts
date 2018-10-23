/*
 * AGPL File-Header
 *
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import TSAdresse from '../../../models/TSAdresse';
import TSBenutzer from '../../../models/TSBenutzer';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    styleUrls: ['../gemeinde-module.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    public beguStart: string;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    public logoImageUrl: string = '#';
    private fileToUpload!: File;
    private navigationSource: StateDeclaration;
    private gemeindeId: string;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.navigationSource = this.$transition$.from();
        this.gemeindeId = this.$transition$.params().gemeindeId;
        if (!this.gemeindeId) {
            return;
        }
        // TODO: Task KIBON-217: Load from DB
        this.logoImageUrl = 'https://upload.wikimedia.org/wikipedia/commons/e/e8/Ostermundigen-coat_of_arms.svg';
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.initProperties(stammdaten);
                if (stammdaten.adresse === undefined) {
                    stammdaten.adresse = new TSAdresse();
                }
                if (stammdaten.beschwerdeAdresse === undefined) {
                    stammdaten.beschwerdeAdresse = new TSAdresse();
                }
                this.beguStart = stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');
                return stammdaten;
            }));
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(stammdaten: TSGemeindeStammdaten): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.saveProperties(stammdaten);
        if (this.keineBeschwerdeAdresse) {
            // Reset Beschwerdeadresse if not used
            stammdaten.beschwerdeAdresse = undefined;
        }
        if (this.fileToUpload && this.fileToUpload.type.includes('image/')) {
            this.gemeindeRS.postLogoImage(stammdaten.gemeinde.id, this.fileToUpload);
        }
        this.gemeindeRS.saveGemeindeStammdaten(stammdaten).then(() => this.navigateBack());
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public handleLogoUpload(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.onload = (e: any): void => {
            this.logoImageUrl = e.target.result;
        };
        tmpFileReader.readAsDataURL(this.fileToUpload);
    }

    public getKonfigKontingentierungString(gk: TSGemeindeKonfiguration): string {
        const kontStr = gk.konfigKontingentierung ? this.translate.instant('KONTINGENTIERUNG') :
            'Keine ' + this.translate.instant('KONTINGENTIERUNG');
        return kontStr;
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public isKonfigurationEditable(stammdaten: TSGemeindeStammdaten, gk: TSGemeindeKonfiguration): boolean {
        return TSGemeindeStatus.EINGELADEN === stammdaten.gemeinde.status
            || TSGesuchsperiodeStatus.ENTWURF === gk.gesuchsperiodeStatus;
    }

    private initProperties(stammdaten: TSGemeindeStammdaten): void {
        this.keineBeschwerdeAdresse = stammdaten.beschwerdeAdresse ? false : true;
        stammdaten.konfigurationsListe.forEach(config => {
            config.konfigBeguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
            config.konfigKontingentierung = false;
            config.konfigurationen.forEach(property => {
                if (TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key) {
                    config.konfigBeguBisUndMitSchulstufe = (TSEinschulungTyp as any)[property.value];
                }
                if (TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key) {
                    config.konfigKontingentierung = (property.value === 'true');
                }
            });
        });
    }

    private saveProperties(stammdaten: TSGemeindeStammdaten): void {
        stammdaten.konfigurationsListe.forEach(config => {
            config.konfigurationen.forEach(property => {
                if (TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key) {
                    property.value = config.konfigBeguBisUndMitSchulstufe;
                }
                if (TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key) {
                    property.value = config.konfigKontingentierung ? 'true' : 'false';
                }
            });
        });
    }

    private navigateBack(): void {
        if (this.navigationSource.name) {
            this.$state.go(this.navigationSource, {gemeindeId: this.gemeindeId});
            return;
        }
        this.$state.go('gemeinde.list');
    }
}
