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

import {HttpClient, HttpClientModule} from '@angular/common/http';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {MatSortModule} from '@angular/material';
import {MatTableModule} from '@angular/material/table';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import ErrorService from '../../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../../core/service/traegerschaftRS.rest';
import {createTranslateLoader} from '../../../ngApp/ng-app.module';
import {NgSharedModule} from '../../../shared/ng-shared.module';

import {TraegerschaftViewComponent} from './traegerschaftView';

describe('traegerschaftView', function () {

    let component: TraegerschaftViewComponent;
    let fixture: ComponentFixture<TraegerschaftViewComponent>;

    beforeEach(async(() => {
        const traegerschaftServiceSpy = jasmine.createSpyObj('TraegerschaftRS', ['createTraegerschaft']);
        const errorServiceSpy = jasmine.createSpyObj('ErrorService', ['getErrors']);
        const dvDialogServiceSpy = jasmine.createSpyObj('DvDialog', ['showDialog']);
        const authServiceSpy = jasmine.createSpyObj('AuthServiceRS', ['isOneOfRoles']);

        TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useFactory: (createTranslateLoader),
                        deps: [HttpClient]
                    }
                }),
                FormsModule,
                MatTableModule,
                MatSortModule,
                NoopAnimationsModule, // we don't want material animations in the project yet
                NgSharedModule,
            ],
            providers: [
                {provide: TraegerschaftRS, useValue: traegerschaftServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: DvDialog, useValue: dvDialogServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [TraegerschaftViewComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TraegerschaftViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
