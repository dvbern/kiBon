/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {async, TestBed} from '@angular/core/testing';
import {Observable} from 'rxjs';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {AuthLifeCycleService} from './authLifeCycle.service';

describe('authLifeCycleService', () => {

    let authLifeCycleService: AuthLifeCycleService;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                AuthLifeCycleService,
            ],
        });

        authLifeCycleService = TestBed.get(AuthLifeCycleService);
    }));

    describe('changeAuthStatus', () => {
        it('changes the status to undefined', () => {
            authLifeCycleService.changeAuthStatus(undefined, 'undefined values');
            const all$: Observable<TSAuthEvent> = authLifeCycleService.getAll$();
            all$.subscribe(() => {
                expect(true).toBe(false); // no value should come
            });
        });

        it('changes the status to a given value', () => {
            authLifeCycleService.changeAuthStatus(TSAuthEvent.CHANGE_USER, 'user has changed');

            const all$: Observable<TSAuthEvent> = authLifeCycleService.getAll$();
            all$.subscribe(value => expect(value).toBe(TSAuthEvent.CHANGE_USER));
        });
    });
    describe('get$', () => {
        it('get$ should just return a value for the thrown event', () => {
            authLifeCycleService.changeAuthStatus(TSAuthEvent.CHANGE_USER, 'user has changed');

            const loginFailed$: Observable<TSAuthEvent> = authLifeCycleService.get$(TSAuthEvent.LOGIN_FAILED);
            loginFailed$.subscribe(() => {
                expect(true).toBe(false); // no value should come
            });

            const changeUser$: Observable<TSAuthEvent> = authLifeCycleService.get$(TSAuthEvent.CHANGE_USER);
            changeUser$.subscribe(value => {
                expect(value).toBe((TSAuthEvent.CHANGE_USER));
            });
        });
    });
});
