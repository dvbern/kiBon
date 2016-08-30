import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {TSWizardStepName, getTSWizardStepNameValues} from '../../models/enums/TSWizardStepName';
import TSWizardStep from '../../models/TSWizardStep';
import WizardStepRS from './WizardStepRS.rest';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import IPromise = angular.IPromise;

export default class WizardStepManager {

    private allowedSteps: Array<TSWizardStepName> = [];
    private wizardSteps: Array<TSWizardStep> = [];
    private currentStepName: TSWizardStepName; // keeps track of the name of the current step


    static $inject = ['AuthServiceRS', 'WizardStepRS'];
    /* @ngInject */
    constructor(private authServiceRS: AuthServiceRS, private wizardStepRS: WizardStepRS) {
        this.setAllowedStepsForRole(authServiceRS.getPrincipalRole());
    }

    public getCurrentStep(): TSWizardStep {
        return this.getStepByName(this.currentStepName);
    }

    public setCurrentStep(stepName: TSWizardStepName): void {
        this.currentStepName = stepName;
    }

    /**
     * Initializes WizardSteps with one single Step GESUCH_ERSTELLEN which status is IN_BEARBEITUNG.
     * This method must be called only when the Gesuch doesn't exist yet.
     */
    public initWizardSteps() {
        this.wizardSteps = [new TSWizardStep(undefined, TSWizardStepName.GESUCH_ERSTELLEN, TSWizardStepStatus.IN_BEARBEITUNG, undefined)];
        this.currentStepName = TSWizardStepName.GESUCH_ERSTELLEN;
    }

    public getAllowedSteps(): Array<TSWizardStepName> {
        return this.allowedSteps;
    }

    public getWizardSteps(): Array<TSWizardStep> {
        return this.wizardSteps;
    }

    public setAllowedStepsForRole(role: TSRole): void {
        if (TSRole.SACHBEARBEITER_INSTITUTION === role || TSRole.SACHBEARBEITER_TRAEGERSCHAFT === role) {
            this.setAllowedStepsForInstitutionTraegerschaft();
        } else {
            this.setAllSteps();
        }
    }

    private setAllowedStepsForInstitutionTraegerschaft(): void {
        this.allowedSteps.push(TSWizardStepName.FAMILIENSITUATION);
        this.allowedSteps.push(TSWizardStepName.GESUCHSTELLER);
        this.allowedSteps.push(TSWizardStepName.BETREUUNG);
        this.allowedSteps.push(TSWizardStepName.VERFUEGEN);
    }

    private setAllSteps(): void {
        this.allowedSteps = getTSWizardStepNameValues();
    }

    public findStepsFromGesuch(gesuchId: string): IPromise<void> {
        return this.wizardStepRS.findWizardStepsFromGesuch(gesuchId).then((response) => {
            this.wizardSteps = response;
        });
    }

    public getStepByName(stepName: TSWizardStepName): TSWizardStep {
        return this.wizardSteps.filter((step: TSWizardStep) => {
            return step.wizardStepName === stepName;
        })[0];
    }

    /**
     * Der Step wird aktualisiert und die Liste von Steps wird nochmal aus dem Server geholt. Sollte der Status gleich sein,
     * nichts wird gemacht und undefined wird zurueckgegeben.
     * @param stepName
     * @param stepStatus
     * @returns {any}
     */
    private updateWizardStepStatus(stepName: TSWizardStepName, stepStatus: TSWizardStepStatus): IPromise<void> {
        let step: TSWizardStep = this.getStepByName(stepName);
        step.wizardStepStatus = this.getNewStatus(step.wizardStepStatus, stepStatus);
        if (step.wizardStepStatus === stepStatus) { // nur wenn der Status sich geaendert hat
            return this.wizardStepRS.updateWizardStep(step).then((response: TSWizardStep) => {
                return this.findStepsFromGesuch(response.gesuchId);
            });
        }
        return undefined;
    }

    /**
     * Der aktuelle Step wird aktualisiert und die Liste von Steps wird nochmal aus dem Server geholt. Sollte der Status gleich sein,
     * nichts wird gemacht und undefined wird zurueckgegeben.
     * @param stepStatus
     * @returns {IPromise<void>}
     */
    public updateCurrentWizardStepStatus(stepStatus: TSWizardStepStatus): IPromise<void> {
        return this.updateWizardStepStatus(this.currentStepName, stepStatus);
    }

    /**
     * Just updates the current step as is
     * @returns {IPromise<void>}
     */
    public updateCurrentWizardStep(): IPromise<void> {
        return this.wizardStepRS.updateWizardStep(this.getCurrentStep()).then((response: TSWizardStep) => {
            return this.findStepsFromGesuch(response.gesuchId);
        });
    }

    /**
     * Anhand des alten und neuen Status, wird es berechnet, welcher Status gesetzt werden muss. Im normalen Fall wird der neue Status
     * immer gesetzt aber in bestimmten Faellen kann auch ein anderer Status gebraucht werden.
     * @param oldStepStatus
     * @param newStepStatus
     * @returns {TSWizardStepStatus}
     */
    private getNewStatus(oldStepStatus: TSWizardStepStatus, newStepStatus: TSWizardStepStatus): TSWizardStepStatus {
        if (newStepStatus === TSWizardStepStatus.IN_BEARBEITUNG && oldStepStatus !== TSWizardStepStatus.UNBESUCHT) {
            return oldStepStatus;
        }
        return newStepStatus;
    }
}
