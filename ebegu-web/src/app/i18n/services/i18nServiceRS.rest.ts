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

import {Injectable, OnDestroy, inject} from '@angular/core';
import {DateAdapter} from '@angular/material/core';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, ReplaySubject, Subject} from 'rxjs';
import ITranslateService = angular.translate.ITranslateService;
import {takeUntil} from 'rxjs/operators';
import {CONSTANTS, LOCALSTORAGE_LANGUAGE_KEY} from '@models/constants';
import {KiBonMandant, MANDANTS} from '@models/mandant';
import {TSGemeinde} from '../../../models/entity/TSGemeinde';
import {
    TSBrowserLanguage,
    tsBrowserLanguageFromString
} from '../../../models/enums/TSBrowserLanguage';
import {MandantService} from '../../../utils/mandant-service/mandant.service';
import {WindowRef} from '../../../utils/window-ref/windowRef.service';
import {GemeindeService} from '../../shared/services/gemeinde.service';

const translationsDir = './assets/translations';
const translationFilePrefix = 'translations_';
const suffix = `.json?t=${Date.now()}`;

@Injectable({
    providedIn: 'root'
})
export class I18nServiceRSRest implements OnDestroy {
    private readonly translate = inject(TranslateService);
    private readonly $window = inject(WindowRef);
    private readonly dateAdapter = inject<DateAdapter<any>>(DateAdapter);
    private readonly gemeindeService = inject(GemeindeService);
    private readonly mandantService = inject(MandantService);

    public serviceURL: string;
    public static readonly LOCALE_SEPARATOR = '_';
    private $translate: ITranslateService; // will be removed in KIBON-2962
    private locale: string;
    private readonly unsubscribe$ = new Subject<void>();
    private readonly selectedLanguage$ = new ReplaySubject<string>(1);

    public readonly languageChanges$ = this.selectedLanguage$.asObservable();

    public constructor() {
        this.serviceURL = `${CONSTANTS.REST_API}i18n`;
        this.selectedLanguage$.next(this.extractPreferredLanguage());
    }

    public static getBaseTranslationsUrl(language: string): string {
        return `${translationsDir}/${translationFilePrefix}${language}${suffix}`;
    }

    public static getMandantTranslationsUrl(
        language: string,
        identifier: string
    ): string {
        return `${translationsDir}/mandant/${translationFilePrefix}${identifier}_${language}${suffix}`;
    }

    public static getGemeindeTranslationsUrl(
        language: string,
        gemeindeName: string
    ): string {
        return `${translationsDir}/gemeinde/${translationFilePrefix}${gemeindeName}_${language}${suffix}`;
    }

    public init(): void {
        combineLatest([
            this.mandantService.mandant$,
            this.gemeindeService.gemeinde$,
            this.selectedLanguage$
        ])
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(([mandant, gemeinde, selectedLanguage]) => {
                this.locale = this.constructLocale(
                    mandant,
                    gemeinde,
                    selectedLanguage
                );
                this.$window.nativeLocalStorage.setItem(
                    LOCALSTORAGE_LANGUAGE_KEY,
                    selectedLanguage
                );
                this.translate.use(this.locale.toString()); // angular
                this.dateAdapter.setLocale(selectedLanguage);
                if (this.$translate) {
                    this.$translate.use(this.locale.toString()); // will be removed in KIBON-2962
                }
            });
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    /**
     * This method will change the language that the plugin of angular5 uses. It will also use the given
     * angularJsTranslateService to set the language in the corresponding plugin of angularjs
     */
    public changeClientLanguage(selectedLanguage: TSBrowserLanguage): void {
        this.selectedLanguage$.next(selectedLanguage);
    }

    private constructLocale(
        mandant: KiBonMandant,
        gemeinde: TSGemeinde,
        selectedLanguage: string
    ): string {
        let locale =
            mandant === MANDANTS.NONE
                ? selectedLanguage
                : `${selectedLanguage}${I18nServiceRSRest.LOCALE_SEPARATOR}${mandant.hostname}`;
        if (gemeinde !== null) {
            locale = locale.concat(
                `${I18nServiceRSRest.LOCALE_SEPARATOR}${gemeinde.bfsNummer}`
            );
        }
        return locale;
    }

    /**
     * Because it still is an hybrid application we call this method from within angular, while the exported
     * function will be used con configuring the language from config.ts (angular.js) because there we don't have
     * any service registered yet
     */
    public extractPreferredLanguage(): string {
        const language = extractPreferredLanguage(this.$window.nativeWindow);
        this.dateAdapter.setLocale(language);
        return language;
    }

    public currentLanguage(): TSBrowserLanguage {
        return tsBrowserLanguageFromString(this.translate.currentLang);
    }

    // will be removed in KIBON-2962
    public setAngularJSTranslateService($translate: any): void {
        this.$translate = $translate;
        if (this.$translate) {
            this.$translate.use(this.locale.toString());
        }
    }
}

/**
 * This function will try to get the selected language out of the localStorage. If the kibonLanguage is not found
 * it will retrieve the language of the browser and stores it in the localStorage so it is stored for the next time
 */
export function extractPreferredLanguage($window: Window): string {
    const myStorage = $window.localStorage;
    const kibonLanguage = myStorage.getItem(LOCALSTORAGE_LANGUAGE_KEY);
    if (kibonLanguage) {
        return kibonLanguage;
    }

    const firstBrowserLanguage = getFirstBrowserLanguage($window);
    myStorage.setItem(LOCALSTORAGE_LANGUAGE_KEY, firstBrowserLanguage);
    return firstBrowserLanguage;
}

/**
 * This function gets the preferred language of the browser
 */
function getFirstBrowserLanguage($window: Window): TSBrowserLanguage {
    const navigator = $window.navigator;
    const browserLanguagePropertyKeys = [
        'language',
        'browserLanguage',
        'systemLanguage',
        'userLanguage'
    ];
    let foundLanguages: TSBrowserLanguage[];

    // support for HTML 5.1 "navigator.languages"
    if (Array.isArray(navigator.languages)) {
        foundLanguages = navigator.languages
            .filter(lang => lang && lang.length)
            .map(tsBrowserLanguageFromString);
        if (foundLanguages && foundLanguages.length > 0) {
            return foundLanguages[0];
        }
    }

    // support for other well known properties in browsers
    foundLanguages = browserLanguagePropertyKeys
        .map(key => (navigator as any)[key])
        .filter(lang => lang && lang.length)
        .map(tsBrowserLanguageFromString);

    return foundLanguages && foundLanguages.length > 0
        ? foundLanguages[0]
        : TSBrowserLanguage.DE;
}
