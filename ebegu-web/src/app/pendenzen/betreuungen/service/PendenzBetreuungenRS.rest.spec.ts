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

import angular, {IHttpBackendService} from 'angular';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../../hybridTools/translationsMock';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSPendenzBetreuung} from '../../../../models/TSPendenzBetreuung';
import {TestDataUtil} from '../../../../utils/TestDataUtil.spec';
import {PENDENZEN_BETREUUNGEN_JS_MODULE} from '../pendenzenBetreuungen.module';
import {PendenzBetreuungenRS} from './PendenzBetreuungenRS.rest';

xdescribe('pendenzBetreuungenRS', () => {
    let pendenzBetreuungenRS: PendenzBetreuungenRS;
    let $httpBackend: IHttpBackendService;
    let mockPendenzBetreuungen: TSPendenzBetreuung;
    let mockPendenzBetreuungenRest: any;

    beforeEach(angular.mock.module(PENDENZEN_BETREUUNGEN_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            pendenzBetreuungenRS = $injector.get('PendenzBetreuungenRS');
            $httpBackend = $injector.get('$httpBackend');
        })
    );

    beforeEach(() => {
        mockPendenzBetreuungen = new TSPendenzBetreuung(
            '123.12.12.12',
            'TestGemeinde',
            '123',
            '123',
            '123',
            'Kind',
            'Kilian',
            undefined,
            'Platzbestaetigung',
            '2024/2025',
            undefined,
            TSBetreuungsangebotTyp.KITA,
            undefined,
            undefined,
            undefined,
            undefined
        );
        mockPendenzBetreuungenRest = {
            betreuungsNummer: '123.12.12.12',
            gemeindeName: 'TestGemeinde',
            betreuungsId: '123',
            gesuchId: '123',
            kindId: '123',
            vorname: 'Kilian',
            name: 'Kind',
            typ: 'Platzbestaetigung',
            gesuchsperiodeString: '2024/2025',
            betreuungsangebotTyp: 'KITA'
        };
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('API Usage', () => {
        describe('findBetreuung', () => {
            it('should return all pending Betreuungen', () => {
                const arrayResult = [mockPendenzBetreuungenRest];
                $httpBackend
                    .expectGET(pendenzBetreuungenRS.serviceURL)
                    .respond(arrayResult);

                let foundPendenzen: Array<TSPendenzBetreuung>;
                pendenzBetreuungenRS
                    .getPendenzenBetreuungenList()
                    .then(result => {
                        foundPendenzen = result;
                    });
                $httpBackend.flush();
                expect(foundPendenzen).toBeDefined();
                expect(foundPendenzen.length).toBe(1);
                expect(foundPendenzen[0]).toEqual(mockPendenzBetreuungen);
            });
        });
    });
});
