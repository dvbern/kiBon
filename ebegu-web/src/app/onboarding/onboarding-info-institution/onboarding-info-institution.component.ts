/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {MandantService} from '../../../utils/mandant-service/mandant.service';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

@Component({
    selector: 'dv-onboarding-info-institution',
    templateUrl: './onboarding-info-institution.component.html',
    styleUrls: [
        './onboarding-info-institution.component.less',
        '../onboarding.less'
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class OnboardingInfoInstitutionComponent implements OnInit {
    private readonly onboardingPlaceholderService = inject(
        OnboardingPlaceholderService
    );
    private readonly mandantService = inject(MandantService);
    private readonly translate = inject(TranslateService);

    private readonly description1: string = 'ONBOARDING_INSTITUTION_DESC1';
    private readonly description2: string = 'ONBOARDING_INSTITUTION_DESC2';
    private readonly description3: string = 'ONBOARDING_INSTITUTION_DESC3';
    private readonly description4: string = 'ONBOARDING_INSTITUTION_DESC4';
    private readonly subjectText: string = 'ONBOARDING_MAIL_SUBJECT';
    private readonly emailBody: string = 'ONBOARDING_MAIL_INSTITUTION_BODY';
    private readonly emailEnd: string = 'ONBOARDING_MAIL_BODY_END';

    public testZugangBeantragen: boolean;
    public institutionName: string;

    public ngOnInit(): void {
        this.onboardingPlaceholderService.setDescription1(
            this.translate.instant(this.description1)
        );
        this.onboardingPlaceholderService.setDescription2(
            this.translate.instant(this.description2)
        );
        this.onboardingPlaceholderService.setDescription3(
            this.translate.instant(this.description3)
        );
        this.onboardingPlaceholderService.setDescription4(
            this.translate.instant(this.description4)
        );
    }

    public onSubmit(form: NgForm): void {
        if (!form.valid) {
            return;
        }
        const supportMail = this.translate.instant('SUPPORT_MAIL');
        const mailto = `mailto:${supportMail}?subject=`;
        const emailBody = '&body=';
        const zeilenUmbruch = '%0D%0A%0D%0A';
        const body: string = this.translate.instant(this.emailBody, {
            institutionName: this.institutionName,
            mandantName: this.getMandantName()
        });
        const subject: string = this.translate.instant(this.subjectText);
        const endBody: string = this.translate.instant(this.emailEnd);
        window.location.href =
            mailto + subject + emailBody + body + zeilenUmbruch + endBody;
    }

    private getMandantName(): string {
        let mandantName = this.mandantService
            .parseHostnameForMandant()
            .fullName.replace(/^\w+/, 'Mandant');

        if (this.translate.currentLang === 'fr_be') {
            mandantName = mandantName.replace(/\bBern\b/, 'Berne');
        }

        return mandantName;
    }
}
