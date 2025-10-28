import {Injectable, inject} from '@angular/core';
import {TSWizardStepName, TSWizardStepStatus} from '@kibon/shared/model/enums';
import {WizardStepManager} from '../../../../../../src/gesuch/service/wizardStepManager';

@Injectable({
    providedIn: 'root'
})
export class SharedUtilDvShowWarningAngabenVervollstaendingenService {
    private wizardStepManager = inject(WizardStepManager);

    public showWarningAngabenVervollstaendigen(): boolean {
        if (
            this.wizardStepManager.getCurrentStep().wizardStepStatus ===
            TSWizardStepStatus.OK
        ) {
            return false;
        }
        return (
            this.wizardStepManager.getStepByName(TSWizardStepName.GESUCHSTELLER)
                .wizardStepStatus === TSWizardStepStatus.NOK
        );
    }
}
