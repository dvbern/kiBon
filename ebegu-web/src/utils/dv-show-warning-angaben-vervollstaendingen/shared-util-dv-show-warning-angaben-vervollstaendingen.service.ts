import {Injectable, inject} from '@angular/core';
import {WizardStepManager} from '../../gesuch/service/wizardStepManager';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';

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
