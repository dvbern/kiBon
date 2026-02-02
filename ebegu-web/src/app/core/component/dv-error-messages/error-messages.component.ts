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
    ChangeDetectorRef,
    Component,
    Input,
    OnChanges,
    OnDestroy,
    SimpleChanges,
    inject
} from '@angular/core';
import {ControlContainer, NgForm, ValidationErrors} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {TranslateService} from '@ngx-translate/core';

const LOG = LogFactory.createLog('ErrorMessagesComponent');

@Component({
    selector: 'dv-error-messages',
    templateUrl: './error-messages.component.html',
    styleUrls: ['./dv-error-messages.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    standalone: false
})
export class ErrorMessagesComponent implements OnChanges, OnDestroy {
    readonly form = inject(NgForm);
    readonly changeDetectorRef = inject(ChangeDetectorRef);
    private readonly translate = inject(TranslateService);

    @Input() public errorObject: ValidationErrors | null;
    @Input() public inputId: string;
    @Input() public errorMessageOverrides: Record<string, string> = {};

    public error: string = '';

    private readonly unsubscribe$ = new Subject<void>();

    public constructor() {
        this.form.ngSubmit.pipe(takeUntil(this.unsubscribe$)).subscribe({
            next: () => this.changeDetectorRef.markForCheck(),
            error: err => LOG.error(err)
        });
    }

    public ngOnChanges(changes: SimpleChanges): void {
        // when the errors change we need to update our error
        if (changes?.errorObject || changes?.errorMessageOverrides) {
            this.initError(changes.errorObject.currentValue);
        }
    }

    private getErrorMessage(errorKey: string, errorValue?: any): string {
        console.log('error cmp', this.errorMessageOverrides);
        console.log('errorkey', errorKey);

        if (this.errorMessageOverrides?.[errorKey]) {
            return this.translate.instant(this.errorMessageOverrides[errorKey]);
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
                    value: errorValue?.min?.min
                });
            case 'max':
                return this.translate.instant('ERROR_MAX', {
                    value: errorValue?.max?.max
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
                    startgp: errorValue?.dvGesuchsperiodeIsInDateRange?.startGp,
                    endgp: errorValue?.dvGesuchsperiodeIsInDateRange?.endGp
                });
            default:
                return this.translate.instant('ERROR_UNKNOWN');
        }
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public initError(errors: ValidationErrors | null): void {
        const errorKey = this.findFirstErrorKey(errors);
        const errorValue = errors?.[errorKey];
        this.error = errorKey ? this.getErrorMessage(errorKey, errorValue) : '';
    }

    private findFirstErrorKey(errors?: ValidationErrors | null): string {
        if (!errors) {
            return '';
        }

        const firstErroneousKey = Object.keys(errors)
            // sort required to the end so more precise errors precede
            .sort((a, b) => {
                if (a === 'required') {
                    return 1;
                }
                if (b === 'required') {
                    return -1;
                }
                return a.localeCompare(b);
            })
            .find(key => !!errors[key]);

        return firstErroneousKey || '';
    }
}
