import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {FinanzielleSituationStartSchwyzComponent} from './finanzielle-situation-start-schwyz.component';
import {TSGesuchsperiode, TSWizardStep} from '@kibon/shared/model/entity';
import {TSWizardStepStatus} from '@kibon/shared/model/enums';
import {By} from '@angular/platform-browser';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {ListResourceRS} from '../../../../../app/core/service/listResourceRS.rest';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {StateService} from '@uirouter/angular';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSLand} from '../../../../../models/types/TSLand';
import {Observable} from 'rxjs';
import {TSFinanzielleSituationTyp} from '@kibon/shared/model/enums';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';

const gesuchsperiodeSpy = jasmine.createSpyObj<TSGesuchsperiode>(
    TSGesuchsperiode.name,
    ['gueltigkeit']
);

const finanzModelSpy = jasmine.createSpyObj<TSFinanzModel>(TSFinanzModel.name, [
    'copyFinSitDataFromGesuch'
]);
finanzModelSpy.familienSituation = {
    set sozialhilfeBezueger(value: boolean) {},
    get sozialhilfeBezueger() {
        return false;
    }
} as TSFamiliensituation;

const wizardStepManagerSpy = jasmine.createSpyObj<WizardStepManager>(
    WizardStepManager.name,
    [
        'getCurrentStep',
        'setCurrentStep',
        'isNextStepBesucht',
        'isNextStepEnabled',
        'getCurrentStepName',
        'updateCurrentWizardStepStatusSafe',
        'getStepByName'
    ]
);

const familiensituationSpy = jasmine.createSpyObj<TSFamiliensituation>(
    TSFamiliensituation.name,
    ['hasSecondGesuchsteller']
);
familiensituationSpy.hasSecondGesuchsteller.and.returnValue(true);

const gesuchSpy = jasmine.createSpyObj<TSGesuch>(TSGesuch.name, [
    'extractFamiliensituation',
    'extractFamiliensituationGS',
    'gesuchsteller1',
    'gesuchsteller2'
]);
gesuchSpy.extractFamiliensituation.and.returnValue(familiensituationSpy);

const landSpy = jasmine.createSpyObj<TSLand>(TSLand.name, ['code']);

const listResourceRSSpy = jasmine.createSpyObj<ListResourceRS>(
    ListResourceRS.name,
    ['getLaenderList']
);
listResourceRSSpy.getLaenderList.and.returnValue(Promise.resolve([landSpy]));

const finanzielleSituationRSSpy = jasmine.createSpyObj<FinanzielleSituationRS>(
    FinanzielleSituationRS.name,
    ['getFinanzielleSituationTyp']
);
finanzielleSituationRSSpy.getFinanzielleSituationTyp.and.returnValue(
    new Observable<TSFinanzielleSituationTyp>()
);

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, [
    'current'
]);

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, [
    'errors'
]);

const authServiceRSSpy = jasmine.createSpyObj<AuthServiceRS>(
    AuthServiceRS.name,
    ['isOneOfRoles']
);

const gesuchModelManagerSpy = jasmine.createSpyObj<GesuchModelManager>(
    GesuchModelManager.name,
    [
        'areThereOnlyFerieninsel',
        'getBasisjahr',
        'getBasisjahrPlus',
        'getGesuch',
        'isGesuchsteller2Required',
        'isGesuchReadonly',
        'getGesuchsperiode',
        'getGemeinde',
        'setGesuchstellerNumber',
        'getFamiliensituation',
        'isKorrekturModusJugendamt'
    ]
);
gesuchModelManagerSpy.getFamiliensituation.and.returnValue(
    familiensituationSpy
);
gesuchModelManagerSpy.getGesuchsperiode.and.returnValue(gesuchsperiodeSpy);
gesuchModelManagerSpy.getGesuch.and.returnValue(gesuchSpy);

const berechnungsManagerSpy = jasmine.createSpyObj<BerechnungsManager>(
    BerechnungsManager.name,
    ['calculateFinanzielleSituation', 'calculateFinanzielleSituationTemp']
);
berechnungsManagerSpy.calculateFinanzielleSituationTemp.and.returnValue(
    Promise.resolve(new TSFinanzielleSituationResultateDTO())
);

describe('FinanzielleSituationStartSchwyzComponent', () => {
    let fixture: ComponentFixture<FinanzielleSituationStartSchwyzComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinanzielleSituationStartSchwyzComponent],
            providers: [
                {provide: WizardStepManager, useValue: wizardStepManagerSpy},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: BerechnungsManager, useValue: berechnungsManagerSpy},
                {provide: ListResourceRS, useValue: listResourceRSSpy},
                {
                    provide: FinanzielleSituationRS,
                    useValue: finanzielleSituationRSSpy
                },
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceRSSpy}
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
        fixture = TestBed.createComponent(
            FinanzielleSituationStartSchwyzComponent
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
