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
import angular, {IHttpBackendService, IQService, IScope} from 'angular';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSFinanzielleSituationResultateDTO} from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSBetreuungsstatus} from '@kibon/shared/model/enums';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSDossier} from '../../../models/TSDossier';
import {TSEinstellung} from '../../../admin/einstellungen/TSEinstellung';
import {TSGemeindeStammdatenLite} from '../../../models/TSGemeindeStammdatenLite';
import {TSGemeinde} from '@kibon/shared/model/entity';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSGesuchsperiode} from '@kibon/shared/model/entity';
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {VerfuegenListViewController} from './verfuegenListView';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';

describe('verfuegenListViewTest', () => {
    const verfuegenView = 'gesuch.verfuegenView';

    let verfuegenListView: VerfuegenListViewController;
    let gesuchModelManager: GesuchModelManager;
    let $state: StateService;
    let tsKindContainer: TSKindContainer;
    let berechnungsManager: BerechnungsManager;
    let $q: IQService;
    let $rootScope: IScope;
    let $httpBackend: IHttpBackendService;
    let einstellungRS: EinstellungRS;
    let gesuchsperiodeRS: GesuchsperiodeRS;
    let appPropRs: SharedUtilApplicationPropertyRsService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));
    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            $state = $injector.get('$state');
            $q = $injector.get('$q');
            $rootScope = $injector.get('$rootScope');
            $httpBackend = $injector.get('$httpBackend');
            einstellungRS = $injector.get('EinstellungRS');
            gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
            appPropRs = $injector.get('SharedUtilApplicationPropertyRsService');
            tsKindContainer = new TSKindContainer();
            tsKindContainer.kindNummer = 1;
            const wizardStepManager: WizardStepManager =
                $injector.get('WizardStepManager');
            spyOn(wizardStepManager, 'updateWizardStepStatus').and.returnValue(
                $q.resolve()
            );
            spyOn(
                gesuchModelManager,
                'getKinderWithBetreuungList'
            ).and.returnValue([tsKindContainer]);
            spyOn(gesuchsperiodeRS, 'existDokument').and.returnValue(
                $q.resolve(true)
            );
            spyOn(gesuchModelManager, 'calculateVerfuegungen').and.returnValue(
                $q.resolve()
            );

            const gesuchMock = new TSGesuch();
            gesuchMock.dossier = new TSDossier();
            gesuchMock.dossier.gemeinde = new TSGemeinde();
            const gemeindeId = 'mock-gemeinde-id';
            gesuchMock.dossier.gemeinde.id = gemeindeId;
            gesuchMock.gesuchsteller1 = new TSGesuchstellerContainer();
            gesuchMock.gesuchsteller1.gesuchstellerJA = new TSGesuchsteller();
            gesuchModelManager.gemeindeStammdaten =
                new TSGemeindeStammdatenLite();
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuchMock);
            spyOn(gesuchModelManager, 'getDossier').and.returnValue(
                gesuchMock.dossier
            );
            const gesuchsperiode = new TSGesuchsperiode();
            gesuchsperiode.id = 'mock-gesuchsperiode-id';
            spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                gesuchsperiode
            );

            berechnungsManager = $injector.get('BerechnungsManager');
            spyOn(
                berechnungsManager,
                'calculateFinanzielleSituation'
            ).and.returnValue(
                $q.resolve(new TSFinanzielleSituationResultateDTO())
            );
            spyOn(
                berechnungsManager,
                'calculateEinkommensverschlechterung'
            ).and.returnValue(
                $q.resolve(new TSFinanzielleSituationResultateDTO())
            );
            spyOn(einstellungRS, 'findEinstellung').and.returnValue(
                of(new TSEinstellung(null, null, 'true'))
            );

            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            verfuegenListView = new VerfuegenListViewController(
                $state,
                gesuchModelManager,
                berechnungsManager,
                wizardStepManager,
                null,
                $injector.get('DownloadRS'),
                $injector.get('MahnungRS'),
                $injector.get('AuthServiceRS'),
                $rootScope,
                $injector.get('GesuchRS'),
                $injector.get('$timeout'),
                $injector.get('$translate'),
                einstellungRS,
                gesuchsperiodeRS,
                null,
                appPropRs
            );
            $rootScope.$apply();
        })
    );

    describe('Usage API', () => {
        it('should call gesuchModelManager.getBetreuungenList() and return it back', () => {
            const kinderWithetreuungList =
                verfuegenListView.getKinderWithBetreuungList();
            expect(kinderWithetreuungList).toBeDefined();
            expect(kinderWithetreuungList.length).toBe(1);
            expect(kinderWithetreuungList[0]).toBe(tsKindContainer);
        });
        describe('openVerfuegen', () => {
            it('does not open the betreuung because it is not BESTAETIGT', () => {
                spyOn(gesuchModelManager, 'findKind').and.returnValue(1);
                spyOn(
                    gesuchModelManager,
                    'convertKindNumberToKindIndex'
                ).and.returnValue(1);
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.ABGEWIESEN;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                expect(gesuchModelManager.findKind).not.toHaveBeenCalled();
            });
            it('does not find the Kind, so it stops loading and does not move to the next page', () => {
                spyOn(
                    gesuchModelManager,
                    'convertKindNumberToKindIndex'
                ).and.returnValue(-1);
                spyOn(gesuchModelManager, 'setKindIndex');
                spyOn($state, 'go');
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                expect(
                    gesuchModelManager.convertKindNumberToKindIndex
                ).toHaveBeenCalledWith(tsKindContainer.kindNummer);

                expect(gesuchModelManager.setKindIndex).not.toHaveBeenCalled();

                expect($state.go).not.toHaveBeenCalledWith(verfuegenView, {
                    gesuchId: ''
                });
            });
            it('does find the Kind but does not find the Betreuung, so it stops loading and does not move to the next page', () => {
                spyOn(
                    gesuchModelManager,
                    'convertKindNumberToKindIndex'
                ).and.returnValue(0);
                spyOn(
                    gesuchModelManager,
                    'convertBetreuungNumberToBetreuungIndex'
                ).and.returnValue(-1);
                spyOn(gesuchModelManager, 'setKindIndex');
                spyOn(gesuchModelManager, 'setBetreuungIndex');
                spyOn($state, 'go');
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                expect(
                    gesuchModelManager.convertKindNumberToKindIndex
                ).toHaveBeenCalledWith(tsKindContainer.kindNummer);

                expect(gesuchModelManager.setKindIndex).toHaveBeenCalledWith(0);

                expect(
                    gesuchModelManager.setBetreuungIndex
                ).not.toHaveBeenCalled();

                expect($state.go).not.toHaveBeenCalledWith(verfuegenView, {
                    gesuchId: ''
                });
            });
            it('does find the Kind but does not find the Betreuung, so it stops loading and does not move to the next page', () => {
                spyOn(
                    gesuchModelManager,
                    'convertKindNumberToKindIndex'
                ).and.returnValue(0);
                spyOn(
                    gesuchModelManager,
                    'convertBetreuungNumberToBetreuungIndex'
                ).and.returnValue(1);
                spyOn(gesuchModelManager, 'setKindIndex');
                spyOn(gesuchModelManager, 'setBetreuungIndex');
                spyOn($state, 'go');
                const betreuung = new TSBetreuung();
                betreuung.betreuungsstatus = TSBetreuungsstatus.BESTAETIGT;
                betreuung.betreuungNummer = 2;

                verfuegenListView.openVerfuegung(tsKindContainer, betreuung);

                expect(
                    gesuchModelManager.convertKindNumberToKindIndex
                ).toHaveBeenCalledWith(tsKindContainer.kindNummer);

                expect(gesuchModelManager.setKindIndex).toHaveBeenCalledWith(0);

                expect($state.go).toHaveBeenCalledWith(verfuegenView, {
                    betreuungNumber: 2,
                    kindNumber: 1,
                    gesuchId: undefined
                });
            });
        });
    });
});
