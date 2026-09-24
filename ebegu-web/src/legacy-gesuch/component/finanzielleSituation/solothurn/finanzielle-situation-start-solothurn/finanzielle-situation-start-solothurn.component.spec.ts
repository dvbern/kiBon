import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {TSWizardStep} from '../../../../../models/entity/TSWizardStep';
import {TSFamilienstatus} from '../../../../../models/enums/TSFamilienstatus';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationSolothurnService} from '../finanzielle-situation-solothurn.service';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';
import {FinanzielleSituationStartSolothurnComponent} from './finanzielle-situation-start-solothurn.component';
import {By} from '@angular/platform-browser';
import {WizardStepManager} from '../../../../service/wizardStepManager';

describe('FinanzielleSituationStartSolothurnComponent', () => {
    let fixture: ComponentFixture<FinanzielleSituationStartSolothurnComponent>;
    const gesuchModelManagerSpy =
        SolothurnFinSitTestHelpers.createGesuchModelManagerMock();
    const finSitSolothurnServiceMock =
        SolothurnFinSitTestHelpers.createFinSitSolothurnServiceMock();
    const mockProvidersExceptGesuchModelManager =
        SolothurnFinSitTestHelpers.getMockProvidersExceptGesuchModelManager();

    const wizardStepManagerSpy = SolothurnFinSitTestHelpers.extractMockProvider(
        mockProvidersExceptGesuchModelManager,
        WizardStepManager
    ).useValue;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinanzielleSituationStartSolothurnComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                ...mockProvidersExceptGesuchModelManager,
                {
                    provide: FinanzielleSituationSolothurnService,
                    useValue: finSitSolothurnServiceMock
                },
                SolothurnFinSitTestHelpers.getMockProviderBerechnungsManager()
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(
            SolothurnFinSitTestHelpers.createGesuch()
        );
        const famSit = new TSFamiliensituation();
        famSit.familienstatus = TSFamilienstatus.VERHEIRATET;
        gesuchModelManagerSpy.getFamiliensituation.and.returnValue(famSit);
        fixture = TestBed.createComponent(
            FinanzielleSituationStartSolothurnComponent
        );
    });

    it('should show warning fill data 2nd gesuchsteller if wizardstep gesuchsteller is invalid', () => {
        const tsWizardStepCurrent = new TSWizardStep();
        const tsWizardStep = new TSWizardStep();
        tsWizardStepCurrent.wizardStepStatus = TSWizardStepStatus.NOK;
        tsWizardStep.wizardStepStatus = TSWizardStepStatus.NOK;
        wizardStepManagerSpy.getCurrentStep.and.returnValue(
            tsWizardStepCurrent
        );
        wizardStepManagerSpy.getStepByName.and.returnValue(tsWizardStep);
        fixture.detectChanges();
        const warningBanner = fixture.debugElement.query(
            By.css('.fa.fa-exclamation-triangle')
        );
        expect(warningBanner).toBeTruthy();
    });

    it('should not show warning fill data 2nd gesuchsteller if wizardstep gesuchsteller is valid', () => {
        const tsWizardStepCurrent = new TSWizardStep();
        const tsWizardStep = new TSWizardStep();
        tsWizardStepCurrent.wizardStepStatus = TSWizardStepStatus.NOK;
        tsWizardStep.wizardStepStatus = TSWizardStepStatus.OK;
        wizardStepManagerSpy.getCurrentStep.and.returnValue(
            tsWizardStepCurrent
        );
        wizardStepManagerSpy.getStepByName.and.returnValue(tsWizardStep);
        fixture.detectChanges();
        const warningBanner = fixture.debugElement.query(
            By.css('.fa.fa-exclamation-triangle')
        );
        expect(warningBanner).toBeFalsy();
    });
});
