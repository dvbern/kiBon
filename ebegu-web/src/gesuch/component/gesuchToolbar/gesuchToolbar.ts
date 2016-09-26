import {IComponentOptions} from 'angular';
import UserRS from '../../../core/service/userRS.rest';
import GesuchModelManager from '../../service/gesuchModelManager';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSGesuch from '../../../models/TSGesuch';
import GesuchRS from '../../service/gesuchRS.rest';
import BerechnungsManager from '../../service/berechnungsManager';
import {IStateService} from 'angular-ui-router';
import TSAntragDTO from '../../../models/TSAntragDTO';
import Moment = moment.Moment;
import ITranslateService = angular.translate.ITranslateService;
let template = require('./gesuchToolbar.html');
require('./gesuchToolbar.less');

export class GesuchToolbarComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = GesuchToolbarController;
    controllerAs = 'vm';
}

export class GesuchToolbarController {

    userList: Array<TSUser>;
    antragList: Array<TSAntragDTO>;

    gesuchsperiodeList: { [key: string]: Array<TSAntragDTO> } = {};
    antragTypList: { [key: string]: TSAntragDTO } = {};

    static $inject = ['UserRS', 'GesuchModelManager', 'EbeguUtil', 'CONSTANTS', 'GesuchRS',
        'BerechnungsManager', '$state'];

    constructor(private userRS: UserRS, private gesuchModelManager: GesuchModelManager, private ebeguUtil: EbeguUtil,
                private CONSTANTS: any, private gesuchRS: GesuchRS,
                private berechnungsManager: BerechnungsManager, private $state: IStateService) {
        this.updateUserList();
        this.updateAntragDTOList();
    }

    public getVerantwortlicherFullName(): string {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().fall && this.gesuchModelManager.getGesuch().fall.verantwortlicher) {
            return this.gesuchModelManager.getGesuch().fall.verantwortlicher.getFullName();
        }
        return '';
    }

    public updateUserList(): void {
        this.userRS.getAllUsers().then((response) => {
            this.userList = angular.copy(response);
        });
    }

    public updateAntragDTOList(): void {
        let gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch && gesuch.id) {
            this.gesuchRS.getAllAntragDTOForFall(gesuch.fall.id).then((response) => {
                this.antragList = angular.copy(response);
                this.updateGesuchperiodeList();
                this.updateAntragTypList();
            });
        }
    }

    private updateGesuchperiodeList() {

        for (var i = 0; i < this.antragList.length; i++) {
            let gs = this.antragList[i].gesuchsperiodeString;

            if (!this.gesuchsperiodeList[gs]) {
                this.gesuchsperiodeList[gs] = [];
            }
            this.gesuchsperiodeList[gs].push(this.antragList[i]);
        }
    }

    private updateAntragTypList() {
        let gesuch = this.gesuchModelManager.getGesuch();
        for (var i = 0; i < this.antragList.length; i++) {
            let antrag = this.antragList[i];
            if (gesuch.gesuchsperiode.gueltigkeit.gueltigAb.isSame(antrag.gesuchsperiodeGueltigAb)) {
                let txt = this.ebeguUtil.getAntragTextDateAsString(antrag.antragTyp, antrag.eingangsdatum);

                this.antragTypList[txt] = antrag;
            }
        }
    }

    getKeys(map: { [key: string]: Array<TSAntragDTO> }): Array<String> {
        var keys: Array<String> = [];
        for (var key in map) {
            if (map.hasOwnProperty(key)) {
                keys.push(key);
            }
        }
        return keys;
    }

    /**
     * Sets the given user as the verantworlicher fuer den aktuellen Fall
     * @param verantwortlicher
     */
    public setVerantwortlicher(verantwortlicher: TSUser): void {
        if (verantwortlicher) {
            this.gesuchModelManager.setUserAsFallVerantwortlicher(verantwortlicher);
            this.gesuchModelManager.updateFall();
        }
    }

    /**
     *
     * @param user
     * @returns {boolean} true if the given user is already the verantwortlicher of the current fall
     */
    public isCurrentVerantwortlicher(user: TSUser): boolean {
        return (user && this.gesuchModelManager.getFallVerantwortlicher() && this.gesuchModelManager.getFallVerantwortlicher().username === user.username);
    }

    public getGesuchName(): string {
        let gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch && gesuch.gesuchsteller1) {
            return this.ebeguUtil.addZerosToNumber(gesuch.fall.fallNummer, this.CONSTANTS.FALLNUMMER_LENGTH) +
                ' ' + gesuch.gesuchsteller1.nachname;
        } else {
            return '--';
        }
    }

    public getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getCurrentGesuchsperiode(): string {
        let gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch && gesuch.gesuchsperiode) {
            return this.getGesuchsperiodeAsString(gesuch.gesuchsperiode);
        } else {
            return '--';
        }
    }

    public getAntragTyp(): string {
        let gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch) {
            return this.ebeguUtil.getAntragTextDateAsString(gesuch.typ, gesuch.eingangsdatum);
        } else {
            return '';
        }
    }

    public getAntragDatum(): Moment {
        let gesuch = this.gesuchModelManager.getGesuch();
        if (gesuch && gesuch.eingangsdatum) {
            return gesuch.eingangsdatum;
        } else {
            return moment();
        }
    }

    public getGesuchsperiodeAsString(tsGesuchsperiode: TSGesuchsperiode) {
        return tsGesuchsperiode.gesuchsperiodeString;
    }

    public setGesuchsperiode(gesuchsperiodeKey: string) {
        let selectedGesuche = this.gesuchsperiodeList[gesuchsperiodeKey];
        let selectedGesuch: TSAntragDTO = this.getNewest(selectedGesuche);
        this.gesuchRS.findGesuch(selectedGesuch.antragId)
            .then((response) => {
                if (response) {
                    this.openGesuch(response);
                    this.updateAntragTypList();
                }
            });
    }

    private getNewest(arrayTSAntragDTO: Array<TSAntragDTO>): TSAntragDTO {
        let newest: TSAntragDTO = arrayTSAntragDTO[0];
        for (var i = 0; i < arrayTSAntragDTO.length; i++) {
            if (arrayTSAntragDTO[i].eingangsdatum.isAfter(newest.eingangsdatum)) {
                newest = arrayTSAntragDTO[i];
            }
        }
        return newest;
    }

    private openGesuch(gesuch: TSGesuch): void {
        if (gesuch) {
            this.berechnungsManager.clear();
            this.gesuchModelManager.setGesuch(gesuch);
            this.$state.go('gesuch.fallcreation');
        }
    }

    public setAntragTypDatum(antragTypDatumKey: string) {
        let selectedAntragTypGesuch = this.antragTypList[antragTypDatumKey];

        this.gesuchRS.findGesuch(selectedAntragTypGesuch.antragId)
            .then((response) => {
                if (response) {
                    this.openGesuch(response);
                }
            });
    }

}
