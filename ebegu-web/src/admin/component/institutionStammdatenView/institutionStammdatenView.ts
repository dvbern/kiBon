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

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import ListResourceRS from '../../../app/core/service/listResourceRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {getTSBetreuungsangebotTypValues, TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenFerieninsel from '../../../models/TSInstitutionStammdatenFerieninsel';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import TSLand from '../../../models/types/TSLand';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {IInstitutionStammdatenStateParams} from '../../admin.route';
import IFormController = angular.IFormController;

export class InstitutionStammdatenViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./institutionStammdatenView.html');
    public controller = InstitutionStammdatenViewController;
    public controllerAs = 'vm';
}

export class InstitutionStammdatenViewController extends AbstractAdminViewController {

    public static $inject = ['InstitutionRS', 'EbeguUtil', 'InstitutionStammdatenRS', '$state', 'ListResourceRS',
        'AuthServiceRS', '$stateParams'];

    public form: IFormController;

    public selectedInstitution: TSInstitution;
    public selectedInstitutionStammdaten: TSInstitutionStammdaten;
    public betreuungsangebotValues: Array<any>;
    public selectedInstitutionStammdatenBetreuungsangebot: any;
    public laenderList: TSLand[];
    public errormessage: string = undefined;
    public hasDifferentZahlungsadresse: boolean = false;
    public modulTageschuleMap: { [key: string]: TSModulTagesschule; } = {};

    public constructor(private readonly institutionRS: InstitutionRS,
                       private readonly ebeguUtil: EbeguUtil,
                       private readonly institutionStammdatenRS: InstitutionStammdatenRS,
                       private readonly $state: StateService,
                       private readonly listResourceRS: ListResourceRS,
                       authServiceRS: AuthServiceRS,
                       private readonly $stateParams: IInstitutionStammdatenStateParams) {
        super(authServiceRS);
    }

    public $onInit(): void {
        this.setBetreuungsangebotTypValues();
        this.listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
        if (this.$stateParams.institutionStammdatenId) {
            this.institutionStammdatenRS.findInstitutionStammdaten(this.$stateParams.institutionStammdatenId).then(
                institutionStammdaten => {
                    this.setSelectedInstitutionStammdaten(institutionStammdaten);
                    this.loadModuleTagesschule();
                    this.initStammdatenFerieninsel();
                });
        } else {
            this.institutionRS.findInstitution(this.$stateParams.institutionId).then(institution => {
                this.selectedInstitution = institution;
                this.createInstitutionStammdaten();
                this.loadModuleTagesschule();
                this.initStammdatenFerieninsel();
            });
        }
    }

    public isCreateStammdatenModel(): boolean {
        return this.selectedInstitutionStammdaten && this.selectedInstitutionStammdaten.isNew();
    }

    public setSelectedInstitutionStammdaten(institutionStammdaten: TSInstitutionStammdaten): void {
        this.selectedInstitutionStammdaten = institutionStammdaten;
        this.selectedInstitution = institutionStammdaten.institution;
        const typ = institutionStammdaten.betreuungsangebotTyp;
        this.selectedInstitutionStammdatenBetreuungsangebot = this.getBetreuungsangebotFromInstitutionList(typ);
        this.hasDifferentZahlungsadresse = !!this.selectedInstitutionStammdaten.adresseKontoinhaber;
    }

    public getSelectedInstitutionStammdaten(): TSInstitutionStammdaten {
        return this.selectedInstitutionStammdaten;
    }

    public createInstitutionStammdaten(): void {
        this.selectedInstitutionStammdaten = new TSInstitutionStammdaten();
        this.selectedInstitutionStammdaten.adresse = new TSAdresse();
        this.selectedInstitutionStammdaten.institution = this.selectedInstitution;
    }

    public differentZahlungsadresseClicked(): void {
        this.selectedInstitutionStammdaten.adresseKontoinhaber =
            this.hasDifferentZahlungsadresse ? new TSAdresse() : undefined;
    }

    public saveInstitutionStammdaten(form: IFormController): void {
        if (!form.$valid) {
            return;
        }

        this.selectedInstitutionStammdaten.betreuungsangebotTyp =
            this.selectedInstitutionStammdatenBetreuungsangebot.key;
        this.replaceTagesschulmoduleOnInstitutionStammdatenTagesschule();
        this.purgeInstitutionstammdaten();

        if (this.isCreateStammdatenModel()) {
            this.institutionStammdatenRS.createInstitutionStammdaten(this.selectedInstitutionStammdaten).then(() => {
                this.goBack();
            });
        } else {
            this.institutionStammdatenRS.updateInstitutionStammdaten(this.selectedInstitutionStammdaten).then(() => {
                this.goBack();
            });
        }
    }

    private goBack(): void {
        this.$state.go('admin.institution', {
            institutionId: this.selectedInstitution.id
        });
    }

    public getBetreuungsangebotFromInstitutionList(betreuungsangebotTyp: TSBetreuungsangebotTyp): any {
        return $.grep(this.betreuungsangebotValues, (value: any) => {
            return value.key === betreuungsangebotTyp;
        })[0];
    }

    public isKita(): boolean {
        return this.selectedInstitutionStammdatenBetreuungsangebot
            && this.selectedInstitutionStammdatenBetreuungsangebot.key === TSBetreuungsangebotTyp.KITA;
    }

    public isTagesschule(): boolean {
        return this.selectedInstitutionStammdatenBetreuungsangebot
            && this.selectedInstitutionStammdatenBetreuungsangebot.key === TSBetreuungsangebotTyp.TAGESSCHULE;
    }

    public isFerieninsel(): boolean {
        return this.selectedInstitutionStammdatenBetreuungsangebot
            && this.selectedInstitutionStammdatenBetreuungsangebot.key === TSBetreuungsangebotTyp.FERIENINSEL;
    }

    private setBetreuungsangebotTypValues(): void {
        this.betreuungsangebotValues = this.ebeguUtil.translateStringList(getTSBetreuungsangebotTypValues());
    }

    public getModulTagesschuleNamen(): TSModulTagesschuleName[] {
        return getTSModulTagesschuleNameValues();
    }

    public getModulTagesschule(modulname: TSModulTagesschuleName): TSModulTagesschule {
        let modul = this.modulTageschuleMap[modulname];
        if (!modul) {
            modul = new TSModulTagesschule();
            modul.wochentag = TSDayOfWeek.MONDAY; // als Vertreter der ganzen Woche
            modul.modulTagesschuleName = modulname;
            this.modulTageschuleMap[modulname] = modul;
        }
        return modul;
    }

    private loadModuleTagesschule(): void {
        this.modulTageschuleMap = {};
        if (this.selectedInstitutionStammdaten && this.selectedInstitutionStammdaten.id) {
            const stammdaten = this.selectedInstitutionStammdaten.institutionStammdatenTagesschule;
            if (stammdaten && stammdaten.moduleTagesschule) {
                this.fillModulTagesschuleMap(stammdaten.moduleTagesschule);
            }
            return;
        }
        this.fillModulTagesschuleMap([]);
    }

    private fillModulTagesschuleMap(modulListFromServer: TSModulTagesschule[]): void {
        getTSModulTagesschuleNameValues().forEach(modulname => {
            const foundmodul = modulListFromServer
                .find(modul => (modul.modulTagesschuleName === modulname && modul.wochentag === TSDayOfWeek.MONDAY));

            this.modulTageschuleMap[modulname] = foundmodul ? foundmodul : this.getModulTagesschule(modulname);
        });
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
        if (!this.isTagesschule()) {
            return;
        }

        const definedModulTagesschule = [];
        // tslint:disable-next-line:forin
        for (const modulname in this.modulTageschuleMap) {
            const tempModul = this.modulTageschuleMap[modulname];
            if (tempModul.zeitVon && tempModul.zeitBis) {
                definedModulTagesschule.push(tempModul);
            }
        }
        if (definedModulTagesschule.length <= 0) {
            return;
        }

        if (!this.selectedInstitutionStammdaten.institutionStammdatenTagesschule) {
            this.selectedInstitutionStammdaten.institutionStammdatenTagesschule =
                new TSInstitutionStammdatenTagesschule();
        }
        this.selectedInstitutionStammdaten.institutionStammdatenTagesschule.moduleTagesschule =
            definedModulTagesschule;
    }

    public getStammdatenFerieninsel(): TSInstitutionStammdatenFerieninsel {
        if (this.selectedInstitutionStammdaten) {
            return this.selectedInstitutionStammdaten.institutionStammdatenFerieninsel;
        }
        return undefined;
    }

    public showAusweichstandorte(): boolean {
        return this.getStammdatenFerieninsel() && this.isFerieninsel();
    }

    private initStammdatenFerieninsel(): void {
        if (this.isFerieninsel() && !this.selectedInstitutionStammdaten.institutionStammdatenFerieninsel) {
            this.selectedInstitutionStammdaten.institutionStammdatenFerieninsel =
                new TSInstitutionStammdatenFerieninsel();
        }
    }

    public betreuungsangebotChanged(): void {
        if (this.isFerieninsel()) {
            this.initStammdatenFerieninsel();
        }
    }

    /**
     * Removes all unused Stammdaten for the selected Betreuungsangebotstyp
     */
    private purgeInstitutionstammdaten(): void {
        if (!this.isFerieninsel()) {
            this.selectedInstitutionStammdaten.institutionStammdatenFerieninsel = undefined;
        }
        if (!this.isTagesschule()) {
            this.selectedInstitutionStammdaten.institutionStammdatenTagesschule = undefined;
        }
    }
}
