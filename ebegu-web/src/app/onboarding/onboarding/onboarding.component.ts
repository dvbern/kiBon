/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {
    Component,
    Input,
    OnDestroy,
    OnInit,
    inject,
    ChangeDetectionStrategy
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DomSanitizer} from '@angular/platform-browser';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {MANDANTS} from '@models/mandant';
import {ApplicationPropertyRsService} from '../../../utils/application-property-rs/application-property-rs.service';
import {MandantService} from '../../../utils/mandant-service/mandant.service';
import {OnboardingHelpDialogComponent} from '../onboarding-help-dialog/onboarding-help-dialog.component';
import {OnboardingPlaceholderService} from '../service/onboarding-placeholder.service';

@Component({
    selector: 'dv-onboarding',
    templateUrl: './onboarding.component.html',
    styleUrls: ['./onboarding.component.less', '../onboarding.less'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class OnboardingComponent implements OnInit, OnDestroy {
    private readonly applicationPropertyRS = inject(
        ApplicationPropertyRsService
    );
    private readonly onboardingPlaceholderService = inject(
        OnboardingPlaceholderService
    );
    private readonly translate = inject(TranslateService);
    private readonly dialog = inject(MatDialog);
    private readonly mandantService = inject(MandantService);
    private readonly sanitizer = inject(DomSanitizer);

    @Input() public showLogin: boolean = true;

    private readonly description1: string = 'ONBOARDING_MAIN_DESC1';
    private readonly description2: string = 'ONBOARDING_MAIN_DESC2';
    private readonly description3: string = 'ONBOARDING_MAIN_DESC3';
    private readonly description4: string = 'ONBOARDING_MAIN_DESC4';
    public isDummyMode$: Observable<boolean>;
    public currentLangDe$: BehaviorSubject<boolean>;
    public isMultimandantEnabled$: Observable<boolean>;
    public isLuzern$: Observable<boolean>;
    private readonly unsubscribe$ = new Subject<void>();

    public constructor() {
        this.isDummyMode$ = this.applicationPropertyRS.isDummyMode();

        this.isMultimandantEnabled$ =
            this.applicationPropertyRS.isMultimandantEnabled();
    }

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

        this.currentLangDe$ = new BehaviorSubject(this.currLangIsGerman());
        this.translate.onLangChange.subscribe({
            next: () => {
                this.currentLangDe$.next(this.currLangIsGerman());
            },
            error: (err: any) => {
                console.error(err);
            }
        });
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private currLangIsGerman(): boolean {
        const splittedCurrentLang = this.translate.currentLang.split('_');
        return splittedCurrentLang[0] === 'de';
    }

    public isGerman$(): Observable<boolean> {
        return this.currentLangDe$.asObservable();
    }

    public switchToDifferentPortal(): void {
        Promise.all([
            this.mandantService.setMandantCookie(MANDANTS.NONE),
            this.mandantService.setMandantRedirectCookie(MANDANTS.NONE)
        ]).then(() => {
            this.mandantService.selectMandant(MANDANTS.NONE, '');
        });
    }

    public openHelp($event: MouseEvent): void {
        $event.preventDefault();
        const dialogConfig = new MatDialogConfig();
        this.dialog.open(OnboardingHelpDialogComponent, dialogConfig);
    }
}
