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

import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import TSApplicationProperty from '../../models/TSApplicationProperty';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {EbeguWebAdmin} from '../admin.module';
import {ApplicationPropertyRS} from './applicationPropertyRS.rest';

describe('ApplicationPropertyRS', () => {

    let applicationPropertyRS: ApplicationPropertyRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let REST_API: string;
    const testName: string = 'myTestName';

    const mockApplicationProp = new TSApplicationProperty(testName, 'myTestValue');

    const mockApplicationPropertyRest = {
        name: testName,
        value: 'myTestValue'
    };

    beforeEach(angular.mock.module(EbeguWebAdmin.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        applicationPropertyRS = $injector.get('ApplicationPropertyRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        REST_API = $injector.get('REST_API');
    }));

    // set the mock response
    beforeEach(() => {
        $httpBackend.when('GET', REST_API + 'application-properties/key/' + testName).respond(mockApplicationPropertyRest);
        $httpBackend.when('GET', REST_API + 'application-properties/').respond([mockApplicationPropertyRest]);
        $httpBackend.when('DELETE', REST_API + 'application-properties/' + testName).respond(200, '');
        $httpBackend.when('POST', REST_API + 'application-properties/' + testName)
            .respond(201, mockApplicationPropertyRest, {Location: 'http://localhost:8080/ebegu/api/v1/application-properties/key/test2'});

    });

    describe('Public API', () => {
        it('should include a getByName() function', () => {
            expect(applicationPropertyRS.getByName).toBeDefined();
        });
        it('should include a create() function', () => {
            expect(applicationPropertyRS.create).toBeDefined();
        });

        it('should include a remove() function', () => {
            expect(applicationPropertyRS.remove).toBeDefined();
        });

        it('should include a getAllApplicationProperties() function', () => {
            expect(applicationPropertyRS.getAllApplicationProperties).toBeDefined();
        });

    });

    describe('API Usage', () => {
        describe('getByName', () => {

            it('should fetch property with given name', () => {
                $httpBackend.expectGET(REST_API + 'application-properties/key/' + testName);
                const promise: angular.IPromise<TSApplicationProperty> = applicationPropertyRS.getByName(testName);
                let property: TSApplicationProperty = undefined;

                promise.then(data => {
                    property = data;
                });
                $httpBackend.flush();
                expect(property.name).toEqual(mockApplicationProp.name);
                expect(property.value).toEqual(mockApplicationProp.value);

            });

        });

        describe('create', () => {

            it('should create property with name and value', () => {
                $httpBackend.expectPOST(REST_API + 'application-properties/' + testName, mockApplicationPropertyRest.value);
                const promise: angular.IHttpPromise<any> = applicationPropertyRS.create(mockApplicationPropertyRest.name, mockApplicationPropertyRest.value);
                let property: TSApplicationProperty = undefined;

                promise.then(response => {
                    property = response.data;
                });
                $httpBackend.flush();
                expect(property.name).toEqual(mockApplicationProp.name);
                expect(property.value).toEqual(mockApplicationProp.value);

            });
        });

        describe('getAllApplicationProperties', () => {

            it('should fetch a list of all properties', () => {
                $httpBackend.expectGET(REST_API + 'application-properties/');
                const promise: angular.IPromise<TSApplicationProperty[]> = applicationPropertyRS.getAllApplicationProperties();
                let list: TSApplicationProperty[] = undefined;

                promise.then(data => {
                    list = data;
                });
                $httpBackend.flush();

                for (let i = 0; i < list.length; i++) {
                    const mockArray = [mockApplicationPropertyRest];
                    expect(list[i].name).toEqual(mockArray[i].name);
                    expect(list[i].value).toEqual(mockArray[i].value);
                }
            });
        });

        describe('remove', () => {

            it('should remove a property', () => {
                $httpBackend.expectDELETE(REST_API + 'application-properties/' + testName);
                const promise = applicationPropertyRS.remove(testName);
                let status: number = undefined;

                promise.then(response => {
                    status = response.status;

                });
                $httpBackend.flush();
                expect(200).toEqual(status);

            });
        });

    });

    afterEach(() => {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });
});
