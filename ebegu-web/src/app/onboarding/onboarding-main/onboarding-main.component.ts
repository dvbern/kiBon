/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, inject, OnInit} from '@angular/core';
import {map} from 'rxjs/operators';
import {MandantLogoWhiteNameVisitor} from '@kibon/shared-model-mandant';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

const LOG = LogFactory.createLog('OnboardingMainComponent');

@Component({
    selector: 'dv-onboarding-main',
    templateUrl: './onboarding-main.component.html',
    styleUrls: ['./onboarding-main.component.less', '../onboarding.less'],
    standalone: false
})
export class OnboardingMainComponent implements OnInit {
    public description1: string = 'ONBOARDING_MAIN_DESC1';
    public description2: string = 'ONBOARDING_MAIN_DESC2';
    public description3: string = 'ONBOARDING_MAIN_DESC3';
    public description4: string = 'ONBOARDING_MAIN_DESC4';
    public splittedScreen: boolean = true;

    mandantService = inject(MandantService);
    applicationPropertyRS = inject(SharedUtilApplicationPropertyRsService);
    onboardingPlaceholderService = inject(OnboardingPlaceholderService);

    logoFileNameWhite$ = this.mandantService.mandant$.pipe(
        map(mandant => {
            const fileName = new MandantLogoWhiteNameVisitor().process(mandant);
            return `url('assets/images/${fileName}')`;
        })
    );

    public ngOnInit() {
        this.onboardingPlaceholderService.description1$.subscribe({
            next: updatedDescription1 => {
                this.description1 = updatedDescription1;
            },
            error: err => LOG.error(err)
        });
        this.onboardingPlaceholderService.description2$.subscribe({
            next: updatedDescription2 => {
                this.description2 = updatedDescription2;
            },
            error: err => LOG.error(err)
        });
        this.onboardingPlaceholderService.description3$.subscribe({
            next: updatedDescription3 => {
                this.description3 = updatedDescription3;
            },
            error: err => LOG.error(err)
        });
        this.onboardingPlaceholderService.description4$.subscribe({
            next: updatedDescription4 => {
                this.description4 = updatedDescription4;
            },
            error: err => LOG.error(err)
        });
        this.onboardingPlaceholderService.splittedScreen$.subscribe({
            next: updatedSplittedScreen => {
                this.splittedScreen = updatedSplittedScreen;
            },
            error: err => LOG.error(err)
        });
    }
}
