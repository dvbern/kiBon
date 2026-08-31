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

import {
    ChangeDetectionStrategy,
    Component,
    inject,
    computed,
    input
} from '@angular/core';
import {toSignal} from '@angular/core/rxjs-interop';
import {ControlContainer, NgForm, ValidationErrors} from '@angular/forms';
import {map} from 'rxjs/operators';
import {TranslateService} from '@ngx-translate/core';

@Component({
    selector: 'dv-error-messages',
    templateUrl: './error-messages.component.html',
    styleUrls: ['./dv-error-messages.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class ErrorMessagesComponent {
    readonly form = inject(NgForm);
    private readonly translate = inject(TranslateService);

    readonly errorObject = input<ValidationErrors | null>(null);
    readonly inputId = input<string>('');
    readonly errorMessageOverrides = input<Record<string, string>>({});

    private readonly currentLang = toSignal(
        this.translate.onLangChange.pipe(map(e => e.lang)),
        {initialValue: this.translate.getCurrentLang()}
    );

    readonly error = computed(() => {
        this.currentLang();
        const errors = this.errorObject();
        const errorKey = this.findFirstErrorKey(errors);
        return errorKey
            ? this.getErrorMessage(errorKey, errors?.[errorKey])
            : '';
    });

    private getErrorMessage(errorKey: string, errorValue?: any): string {
        if (this.errorMessageOverrides()?.[errorKey]) {
            return this.translate.instant(
                this.errorMessageOverrides()[errorKey]
            );
        }

        switch (errorKey) {
            case 'bicSwiftCode':
                return this.translate.instant('ERROR_BIC_SWIFT_CODE');
            case 'iban':
                return this.translate.instant('ERROR_IBAN');
            case 'qrIban':
                return this.translate.instant('ERROR_QR_IBAN');
            case 'required':
                return this.translate.instant('ERROR_REQUIRED');
            case 'minlength':
                return this.translate.instant('ERROR_MIN_LENG');
            case 'maxlength':
            case 'dvMaxLength':
                return this.translate.instant('ERROR_MAX_LENG');
            case 'number':
                return this.translate.instant('ERROR_NUMBER');
            case 'email':
                return this.translate.instant('ERROR_EMAIL');
            case 'pattern':
            case 'moment':
                return this.translate.instant('ERROR_FORMAT');
            case 'url':
                return this.translate.instant('ERROR_INVALID_URL');
            case 'date':
                return this.translate.instant('ERROR_DATE');
            case 'min':
                return this.translate.instant('ERROR_MIN', {
                    value: errorValue?.min
                });
            case 'max':
                return this.translate.instant('ERROR_MAX', {
                    value: errorValue?.max
                });
            case 'valueinput':
                return this.translate.instant('ERROR_VALUE');
            case 'dvMinDate':
            case 'matDatepickerMin':
                return this.translate.instant('ERROR_MIN_DATE');
            case 'matDatepickerMax':
            case 'dvMaxDate':
                return this.translate.instant('ERROR_MAX_DATE');
            case 'dateTime':
            case 'matDatepickerParse':
                return this.translate.instant('ERROR_DATETIME');
            case 'dvMinDateTime':
                return this.translate.instant('ERROR_MIN_DATETIME');
            case 'dvMaxDateTime':
                return this.translate.instant('ERROR_MAX_DATETIME');
            case 'dvNoFutureDate':
            case 'mat':
                return this.translate.instant('ERROR_NO_FUTURE_DATE');
            case 'dvNoFutureDateTime':
                return this.translate.instant('ERROR_NO_FUTURE_DATETIME');
            case 'dvOverlappingZeitraum':
                return this.translate.instant('ERROR_OVERLAPPING_ZEITRAUM');
            case 'parse':
                return this.translate.instant('ERROR_PARSE');
            case 'dvCheckboxRequiredAtLeastOne':
                return this.translate.instant('ERROR_REQUIRED_ONE_OF_THEM');
            case 'dvGesuchsperiodeIsInDateRange':
                return this.translate.instant('ERROR_GP_DATE', {
                    startgp: errorValue?.startGp,
                    endgp: errorValue?.endGp
                });
            case 'benutzerMailForStekIdentifierNotAllowed':
                return this.translate.instant(
                    'ERROR_STEK_IDENTIFIER_INIT_EMAIL_SAME_AS_BENUTER',
                    {email: errorValue?.email}
                );
            default:
                return this.translate.instant('ERROR_UNKNOWN');
        }
    }

    private findFirstErrorKey(errors?: ValidationErrors | null): string {
        if (!errors) {
            return '';
        }

        return (
            Object.keys(errors)
                .sort((a, b) => {
                    if (a === 'required') return 1;
                    if (b === 'required') return -1;
                    return a.localeCompare(b);
                })
                .find(key => !!errors[key]) ?? ''
        );
    }
}
