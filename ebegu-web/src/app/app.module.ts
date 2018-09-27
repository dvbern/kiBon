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

import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {NgAdminModule} from '../admin/ng-admin.module';
import {NgAuthenticationModule} from '../authentication/ng-authentication.module';
import {NgGesuchModule} from '../gesuch/ng-gesuch.module';
import {NgPosteingangModule} from '../posteingang/ng-posteingang.module';
import {AppRoutingModule} from './app-routing.module';
import {appModuleAngularJS} from './app.angularjs.module';
import {BenutzerModule} from './benutzer/benutzer.module';
import {CoreModule} from './core/core.module';
import {GemeindeModule} from './gemeinde/gemeinde.module';
import {OnboardingModule} from './onboarding/onboarding.module';
import {SharedModule} from './shared/shared.module';

@NgModule({
    imports: [
        BrowserModule,
        NoopAnimationsModule, // we don't want material animations in the project yet
        UpgradeModule,

        // Core & Shared
        CoreModule.forRoot(),
        SharedModule,

        AppRoutingModule,
        NgAdminModule,
        GemeindeModule,
        NgAuthenticationModule,
        NgGesuchModule,
        NgPosteingangModule,
        OnboardingModule,
        BenutzerModule,
    ],
})

export class AppModule {

    constructor(private readonly upgrade: UpgradeModule) {
    }

    // noinspection JSUnusedGlobalSymbols
    ngDoBootstrap() {
        // noinspection XHTMLIncompatabilitiesJS
        this.upgrade.bootstrap(document.body, [appModuleAngularJS.name], {strictDi: true});
    }
}
