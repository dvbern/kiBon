import {IComponentOptions, IFormController} from 'angular';
import AbstractGesuchViewController from '../abstractGesuchView';
import GesuchModelManager from '../../service/gesuchModelManager';
import {IStateService} from 'angular-ui-router';
import {IStammdatenStateParams} from '../../gesuch.route';
import TSFinanzielleSituationContainer from '../../../models/TSFinanzielleSituationContainer';
import BerechnungsManager from '../../service/berechnungsManager';
import TSFinanzielleSituationResultateDTO from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import ErrorService from '../../../core/errors/service/ErrorService';
let template = require('./finanzielleSituationView.html');
require('./finanzielleSituationView.less');


export class FinanzielleSituationViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = FinanzielleSituationViewController;
    controllerAs = 'vm';
}

export class FinanzielleSituationViewController extends AbstractGesuchViewController {

    public selbstaendig: boolean;
    static $inject: string[] = ['$stateParams', '$state', 'GesuchModelManager', 'BerechnungsManager', 'CONSTANTS', 'ErrorService'];
    /* @ngInject */
    constructor($stateParams: IStammdatenStateParams, $state: IStateService, gesuchModelManager: GesuchModelManager,
                berechnungsManager: BerechnungsManager, private CONSTANTS: any, private errorService: ErrorService) {
        super($state, gesuchModelManager, berechnungsManager);
        let parsedNum: number = parseInt($stateParams.gesuchstellerNumber, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedNum);
        this.initViewModel();
        this.calculate();
    }

    private initViewModel() {
        this.gesuchModelManager.initFinanzielleSituation();
        this.selbstaendig = this.gesuchModelManager.getStammdatenToWorkWith().finanzielleSituationContainer.finanzielleSituationSV.isSelbstaendig();
    }

    showSteuerveranlagung(): boolean {
        return !this.gesuchModelManager.getFamiliensituation().gemeinsameSteuererklaerung || this.gesuchModelManager.getFamiliensituation().gemeinsameSteuererklaerung === false;
    }

    showSteuererklaerung(): boolean {
        return this.gesuchModelManager.getStammdatenToWorkWith().finanzielleSituationContainer.finanzielleSituationSV.steuerveranlagungErhalten === false;
    }

    private steuerveranlagungClicked(): void {
        // Wenn Steuerveranlagung JA -> auch StekErhalten -> JA
        if (this.getModel().finanzielleSituationSV.steuerveranlagungErhalten === true) {
            this.getModel().finanzielleSituationSV.steuererklaerungAusgefuellt = true;
        } else if (this.getModel().finanzielleSituationSV.steuerveranlagungErhalten === false) {
            // Steuerveranlagung neu NEIN -> Fragen loeschen
            this.getModel().finanzielleSituationSV.steuererklaerungAusgefuellt = undefined;
        }
    }

    previousStep() {
        if ((this.gesuchModelManager.getGesuchstellerNumber() === 2)) {
            this.state.go('gesuch.finanzielleSituation', {gesuchstellerNumber: 1});
        } else if ((this.gesuchModelManager.gesuchstellerNumber === 1) && this.gesuchModelManager.isGesuchsteller2Required()) {
            this.state.go('gesuch.finanzielleSituationStart');
        } else {
            this.state.go('gesuch.kinder');
        }
    }

    nextStep() {
        if ((this.gesuchModelManager.getGesuchstellerNumber() === 1) && this.gesuchModelManager.isGesuchsteller2Required()) {
            this.state.go('gesuch.finanzielleSituation', {gesuchstellerNumber: '2'});
        } else {
            this.state.go('gesuch.finanzielleSituationResultate');
        }
    }

    submit(form: IFormController) {
        if (form.$valid) {
            // Speichern ausloesen
            this.errorService.clearAll();
            this.gesuchModelManager.saveFinanzielleSituation().then((finanzielleSituationResponse: any) => {
                this.nextStep();
            });
        }
    }

    calculate() {
        this.berechnungsManager.calculateFinanzielleSituation(this.gesuchModelManager.gesuch);
    }

    resetForm() {
        this.initViewModel();
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.gesuchModelManager.getStammdatenToWorkWith().finanzielleSituationContainer;
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.finanzielleSituationResultate;
    }
}
