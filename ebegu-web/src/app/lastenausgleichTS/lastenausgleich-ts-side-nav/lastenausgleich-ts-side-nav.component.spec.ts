/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatToolbarModule} from '@angular/material/toolbar';
import {TranslateModule} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {WindowRef} from '../../core/service/windowRef.service';
import {SharedModule} from '../../shared/shared.module';

import {LastenausgleichTsSideNavComponent} from './lastenausgleich-ts-side-nav.component';

describe('LastenausgleichTsSideNavComponent', () => {
    let component: LastenausgleichTsSideNavComponent;
    let fixture: ComponentFixture<LastenausgleichTsSideNavComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            providers: [
                WindowRef
            ],
            declarations: [LastenausgleichTsSideNavComponent],
            imports: [
                UIRouterModule.forRoot({useHash: true}),
                TranslateModule,
                MatToolbarModule,
                SharedModule
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(LastenausgleichTsSideNavComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
