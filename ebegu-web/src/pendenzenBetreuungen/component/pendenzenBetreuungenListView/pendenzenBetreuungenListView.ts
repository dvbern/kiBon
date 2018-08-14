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
import {IComponentOptions, IController} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import BerechnungsManager from '../../../gesuch/service/berechnungsManager';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import TSBetreuungsnummerParts from '../../../models/dto/TSBetreuungsnummerParts';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitution from '../../../models/TSInstitution';
import TSPendenzBetreuung from '../../../models/TSPendenzBetreuung';
import EbeguUtil from '../../../utils/EbeguUtil';
import PendenzBetreuungenRS from '../../service/PendenzBetreuungenRS.rest';

export class PendenzenBetreuungenListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = require('./pendenzenBetreuungenListView.html');
    controller = PendenzenBetreuungenListViewController;
    controllerAs = 'vm';
}

export class PendenzenBetreuungenListViewController implements IController {

    static $inject: string[] = ['PendenzBetreuungenRS', 'EbeguUtil', 'InstitutionRS', 'InstitutionStammdatenRS',
        'GesuchsperiodeRS', 'GesuchModelManager', 'BerechnungsManager', '$state', 'GemeindeRS', 'AuthServiceRS'];

    private pendenzenList: Array<TSPendenzBetreuung>;
    selectedBetreuungsangebotTyp: string;
    selectedInstitution: string;
    selectedGesuchsperiode: string;
    selectedGemeinde: TSGemeinde;
    institutionenList: Array<TSInstitution>;
    betreuungsangebotTypList: Array<TSBetreuungsangebotTyp>;
    activeGesuchsperiodenList: Array<string> = [];
    gemeindenList: Array<TSGemeinde>;
    itemsByPage: number = 20;
    numberOfPages: number = 1;

    constructor(public pendenzBetreuungenRS: PendenzBetreuungenRS,
                private readonly ebeguUtil: EbeguUtil,
                private readonly institutionRS: InstitutionRS,
                private readonly institutionStammdatenRS: InstitutionStammdatenRS,
                private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                private readonly gesuchModelManager: GesuchModelManager,
                private readonly berechnungsManager: BerechnungsManager,
                private readonly $state: StateService,
                private readonly gemeindeRS: GemeindeRS,
                private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public $onInit(): void {
        this.updatePendenzenList();
        this.updateInstitutionenList();
        this.updateBetreuungsangebotTypList();
        this.updateActiveGesuchsperiodenList();
        this.updateGemeindenList();
    }

    public getTotalResultCount(): number {
        if (this.pendenzenList && this.pendenzenList.length) {
            return this.pendenzenList.length;
        }
        return 0;
    }

    private updatePendenzenList() {
        this.pendenzBetreuungenRS.getPendenzenBetreuungenList().then(response => {
            this.pendenzenList = response;
            this.numberOfPages = this.pendenzenList.length / this.itemsByPage;
        });
    }

    public updateActiveGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllNichtAbgeschlosseneGesuchsperioden().then(response => {
            this.extractGesuchsperiodeStringList(response);
        });
    }

    private extractGesuchsperiodeStringList(allActiveGesuchsperioden: TSGesuchsperiode[]) {
        allActiveGesuchsperioden.forEach(gesuchsperiode => {
            this.activeGesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
        });
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenForCurrentBenutzer().then(response => {
            this.institutionenList = response;
        });
    }

    public updateBetreuungsangebotTypList(): void {
        this.institutionStammdatenRS.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer().then(response => {
            this.betreuungsangebotTypList = response;
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal(this.authServiceRS.getPrincipal())
            .then(gemeinden => {
                this.gemeindenList = gemeinden;
            });
    }

    public getPendenzenList(): Array<TSPendenzBetreuung> {
        return this.pendenzenList;
    }

    public editPendenzBetreuungen(pendenz: TSPendenzBetreuung, event: any): void {
        if (pendenz) {
            const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openBetreuung(pendenz, isCtrlKeyPressed);
        }
    }

    private openBetreuung(pendenz: TSPendenzBetreuung, isCtrlKeyPressed: boolean): void {
        const numberParts: TSBetreuungsnummerParts = this.ebeguUtil.splitBetreuungsnummer(pendenz.betreuungsNummer);
        if (numberParts && pendenz) {
            const kindNumber: number = parseInt(numberParts.kindnummer);
            const betreuungNumber: number = parseInt(numberParts.betreuungsnummer);
            if (betreuungNumber > 0) {
                this.berechnungsManager.clear(); // nur um sicher zu gehen, dass alle alte Werte geloescht sind

                // Reload Gesuch in gesuchModelManager on Init in fallCreationView because it has been changed since
                // last time
                this.gesuchModelManager.clearGesuch();
                const navObj: any = {
                    betreuungNumber: betreuungNumber,
                    kindNumber: kindNumber,
                    gesuchId: pendenz.gesuchId
                };
                if (isCtrlKeyPressed) {
                    const url = this.$state.href('gesuch.betreuung', navObj);
                    window.open(url, '_blank');
                } else {
                    this.$state.go('gesuch.betreuung', navObj);
                }
            }
        }
    }
}
