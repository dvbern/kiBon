import {IComponentOptions, IFormController} from 'angular';
import {IKindStateParams} from '../../gesuch.route';
import GesuchModelManager from '../../service/gesuchModelManager';
import TSKind from '../../../models/TSKind';
import {EnumEx} from '../../../utils/EnumEx';
import {TSGeschlecht} from '../../../models/enums/TSGeschlecht';
import AbstractGesuchViewController from '../abstractGesuchView';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import BerechnungsManager from '../../service/berechnungsManager';
import TSKindContainer from '../../../models/TSKindContainer';
import {TSKinderabzug, getTSKinderabzugValues} from '../../../models/enums/TSKinderabzug';
import ErrorService from '../../../core/errors/service/ErrorService';
import WizardStepManager from '../../service/wizardStepManager';
import {TSRole} from '../../../models/enums/TSRole';
import IPromise = angular.IPromise;
import IQService = angular.IQService;


let template = require('./kindView.html');
require('./kindView.less');

export class KindViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = KindViewController;
    controllerAs = 'vm';
}

export class KindViewController extends AbstractGesuchViewController<TSKindContainer> {
    geschlechter: Array<string>;
    kinderabzugValues: Array<TSKinderabzug>;
    showFachstelle: boolean;
    fachstelleId: string; //der ausgewaehlte fachstelleId wird hier gespeichert und dann in die entsprechende Fachstelle umgewandert
    allowedRoles: Array<TSRole>;

    static $inject: string[] = ['$stateParams', 'GesuchModelManager', 'BerechnungsManager', 'CONSTANTS', '$scope',
        'ErrorService', 'WizardStepManager', '$q'];
    /* @ngInject */
    constructor($stateParams: IKindStateParams, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private CONSTANTS: any, private $scope: any, private errorService: ErrorService,
                wizardStepManager: WizardStepManager, private $q: IQService) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager);
        this.gesuchModelManager.setKindNumber(parseInt($stateParams.kindNumber, 10));
        this.model = angular.copy(this.gesuchModelManager.getKindToWorkWith());
        this.initViewModel();
        this.allowedRoles = this.TSRoleUtil.getAllRolesButTraegerschaftInstitution();
    }

    private initViewModel(): void {
        this.geschlechter = EnumEx.getNames(TSGeschlecht);
        this.kinderabzugValues = getTSKinderabzugValues();
        this.showFachstelle = (this.model.kindJA.pensumFachstelle) ? true : false;
        if (this.getPensumFachstelle() && this.getPensumFachstelle().fachstelle) {
            this.fachstelleId = this.getPensumFachstelle().fachstelle.id;
        }
        if (this.gesuchModelManager.getFachstellenList() || this.gesuchModelManager.getFachstellenList().length <= 0) {
            this.gesuchModelManager.updateFachstellenList();
        }
    }

    save(form: IFormController): IPromise<TSKindContainer> {
        if (form.$valid) {
            this.gesuchModelManager.setKindToWorkWith(this.model);
            if (!form.$dirty) {
                // If there are no changes in form we don't need anything to update on Server and we could return the
                // promise immediately
                return this.$q.when(this.model);
            }

            this.errorService.clearAll();
            return this.gesuchModelManager.updateKind();
        }
        return undefined;
    }

    cancel(form: IFormController) {
        this.reset();
        form.$setPristine();
    }

    reset() {
        this.removeKindFromList();
    }

    private removeKindFromList() {
        if (!this.model.timestampErstellt) {
            //wenn das Kind noch nicht erstellt wurde, löschen wir das Kind vom Array
            this.gesuchModelManager.removeKindFromList();
        }
    }

    public setSelectedFachsstelle() {
        let fachstellenList = this.getFachstellenList();
        for (let i: number = 0; i < fachstellenList.length; i++) {
            if (fachstellenList[i].id === this.fachstelleId) {
                this.getModel().pensumFachstelle.fachstelle = fachstellenList[i];
            }
        }
    }

    public showFachstelleClicked() {
        if (!this.showFachstelle) {
            this.resetFachstelleFields();
        } else {
            this.getModel().pensumFachstelle = new TSPensumFachstelle();
        }
    }

    public familienErgaenzendeBetreuungClicked() {
        if (!this.getModel().familienErgaenzendeBetreuung) {
            this.showFachstelle = false;
            this.getModel().wohnhaftImGleichenHaushalt = undefined;
            this.resetFachstelleFields();
        }
    }

    private resetFachstelleFields() {
        this.fachstelleId = undefined;
        this.getModel().pensumFachstelle = undefined;
    }

    public getFachstellenList() {
        return this.gesuchModelManager.getFachstellenList();
    }

    public getModel(): TSKind {
        if (this.model) {
            return this.model.kindJA;
        }
        return undefined;
    }

    public getContainer(): TSKindContainer {
        if (this.model) {
            return this.model;
        }
        return undefined;
    }

    public getPensumFachstelle(): TSPensumFachstelle {
        if (this.getModel()) {
            return this.getModel().pensumFachstelle;
        }
        return undefined;
    }

    public isFachstelleRequired(): boolean {
        return this.getModel() && this.getModel().familienErgaenzendeBetreuung && this.showFachstelle;
    }

    public getDatumEinschulung(): moment.Moment {
        return this.gesuchModelManager.getGesuchsperiodeBegin();
    }
}

