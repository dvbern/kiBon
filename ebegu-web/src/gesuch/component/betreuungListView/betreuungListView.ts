import {IComponentOptions} from 'angular';
import {IStateService} from 'angular-ui-router';
import AbstractGesuchViewController from '../abstractGesuchView';
import GesuchModelManager from '../../service/gesuchModelManager';
import TSKindContainer from '../../../models/TSKindContainer';
import TSBetreuung from '../../../models/TSBetreuung';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import BerechnungsManager from '../../service/berechnungsManager';
import {RemoveDialogController} from '../../dialog/RemoveDialogController';
import EbeguUtil from '../../../utils/EbeguUtil';
import ErrorService from '../../../core/errors/service/ErrorService';
import WizardStepManager from '../../service/wizardStepManager';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import IDialogService = angular.material.IDialogService;
import ITranslateService = angular.translate.ITranslateService;
import IScope = angular.IScope;
import TSMitteilung from '../../../models/TSMitteilung';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
let template = require('./betreuungListView.html');
require('./betreuungListView.less');
let removeDialogTemplate = require('../../dialog/removeDialogTemplate.html');


export class BetreuungListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = BetreuungListViewController;
    controllerAs = 'vm';
}

/**
 * View fuer die Liste der Betreeungen der eingegebenen Kinder
 */
export class BetreuungListViewController extends AbstractGesuchViewController<any> {

    TSRoleUtil = TSRoleUtil;

    static $inject: string[] = ['$state', 'GesuchModelManager', '$translate', 'DvDialog', 'EbeguUtil', 'BerechnungsManager',
        'ErrorService', 'WizardStepManager', 'AuthServiceRS', '$scope'];
    /* @ngInject */
    constructor(private $state: IStateService, gesuchModelManager: GesuchModelManager, private $translate: ITranslateService,
                private DvDialog: DvDialog, private ebeguUtil: EbeguUtil, berechnungsManager: BerechnungsManager,
                private errorService: ErrorService, wizardStepManager: WizardStepManager, private authServiceRS: AuthServiceRS, $scope: IScope) {
        super(gesuchModelManager, berechnungsManager, wizardStepManager, $scope, TSWizardStepName.BETREUUNG);
        this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.IN_BEARBEITUNG);

    }

    public editBetreuung(kind: TSKindContainer, betreuung: any): void {
        let kindNummer: number = this.gesuchModelManager.findKind(kind);
        let betreuungNumber: number = this.gesuchModelManager.findBetreuung(betreuung);
        if (betreuungNumber > 0) {
            betreuung.isSelected = false; // damit die row in der Tabelle nicht mehr als "selected" markiert ist
            this.openBetreuungView(betreuungNumber, kindNummer);
        }
    }

    public getKinderWithBetreuungList(): Array<TSKindContainer> {
        return this.gesuchModelManager.getKinderWithBetreuungList();
    }

    public hasBetreuungInStatusWarten(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().hasBetreuungInStatusWarten();
        }
        return false;
    }


    public createBetreuung(kind: TSKindContainer): void {
        let kindNumber: number = this.gesuchModelManager.findKind(kind);
        if (kindNumber > 0) {
            this.gesuchModelManager.setKindNumber(kindNumber);
            this.openBetreuungView(undefined, kindNumber);
        }
    }

    public removeBetreuung(kind: TSKindContainer, betreuung: TSBetreuung): void {
        this.gesuchModelManager.findKind(kind);
        let remTitleText: any = this.$translate.instant('BETREUUNG_LOESCHEN', {
            kindname: this.gesuchModelManager.getKindToWorkWith().kindJA.getFullName(),
            betreuungsangebottyp: this.ebeguUtil.translateString(TSBetreuungsangebotTyp[betreuung.institutionStammdaten.betreuungsangebotTyp])
        });
        this.DvDialog.showDialog(removeDialogTemplate, RemoveDialogController, {
            title: remTitleText,
            deleteText: 'BETREUUNG_LOESCHEN_BESCHREIBUNG'
        }).then(() => {   //User confirmed removal
            this.errorService.clearAll();
            let betreuungNumber: number = this.gesuchModelManager.findBetreuung(betreuung);
            if (betreuungNumber > 0) {
                this.gesuchModelManager.setBetreuungNumber(betreuungNumber);
                this.gesuchModelManager.removeBetreuung();
            }
        });
    }

    private openBetreuungView(betreuungNumber: number, kindNumber: number): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: betreuungNumber,
            kindNumber: kindNumber,
            gesuchId: this.getGesuchId()
        });
    }

    /**
     * Gibt den Betreuungsangebottyp der Institution, die mit der gegebenen Betreuung verknuepft ist zurueck.
     * By default wird ein Leerzeichen zurueckgeliefert.
     * @param betreuung
     * @returns {string}
     */
    public getBetreuungsangebotTyp(betreuung: TSBetreuung): string {
        if (betreuung && betreuung.institutionStammdaten) {
            return TSBetreuungsangebotTyp[betreuung.institutionStammdaten.betreuungsangebotTyp];
        }
        return '';
    }

    public canRemoveBetreuung(betreuung: TSBetreuung): boolean {
        return !this.isGesuchReadonly() && !betreuung.vorgaengerId;
    }

    private showMitteilung() : boolean {
        return this.authServiceRS.isOneOfRoles(this.TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
    }

    private gotoMitteilung(betreuung : TSBetreuung) {
        let entwurf : TSMitteilung = new TSMitteilung();
        this.$state.go('gesuch.mitteilung', {
            fallId: this.gesuchModelManager.getGesuch().fall.id,
            gesuchId: this.gesuchModelManager.getGesuch().id,
            betreuungId: betreuung.id
        });
    }
}
