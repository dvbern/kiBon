/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

import {FocusOptions} from '@angular/cdk/a11y';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    input,
    model,
    output,
    viewChild
} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
    FormValueControl,
    transformedValue,
    ValidationError
} from '@angular/forms/signals';
import {ErrorStateMatcher} from '@angular/material/core';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MaskitoDirective} from '@maskito/angular';
import {MaskitoOptions} from '@maskito/core';
import {
    maskitoNumber,
    MaskitoNumberParams,
    maskitoParseNumber
} from '@maskito/kit';
import {GesuchSharedFormLabel} from '../style/gesuch-shared-form-label';
import {GesuchSharedFormStyleMaterialInputWrapper} from '../style/gesuch-shared-form-style-material-input-wrapper/gesuch-shared-form-style-material-input-wrapper';

@Component({
    selector: 'kib-gesuch-form-number-input',
    imports: [
        MaskitoDirective,
        FormsModule,
        GesuchSharedFormStyleMaterialInputWrapper,
        MatFormField,
        MatInput,
        MatError,
        GesuchSharedFormLabel
    ],
    templateUrl: './gesuch-form-number-input.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchFormNumberInput implements FormValueControl<number | null> {
    maximumDecimalDigits = input<number>(2);
    value = model<number | null>(null);
    errors = input<ValidationError[]>();
    disabled = input<boolean>();
    required = input<boolean>();
    invalid = input<boolean>();
    touched = input<boolean>(false);
    touch = output<void>();

    matcher: ErrorStateMatcher = {
        isErrorState: () => (this.errors()?.length ?? 0) > 0 && this.touched()
    };

    private readonly inputElement = viewChild.required<MatInput>(MatInput);

    public focus(options?: FocusOptions): void {
        this.inputElement().focus(options);
    }

    private readonly numberParamsSig = computed<MaskitoNumberParams>(() => ({
        thousandSeparator: "'",
        // Note: default maximumFractionDigits is 0 leading to no decimal allowed
        maximumFractionDigits: this.maximumDecimalDigits()
    }));

    readonly maskitoOptionsSig = computed<MaskitoOptions>(() =>
        maskitoNumber(this.numberParamsSig())
    );

    protected readonly rawValue = transformedValue(this.value, {
        parse: (raw: string) => {
            const parsed = maskitoParseNumber(raw, this.numberParamsSig());
            return {value: Number.isNaN(parsed) ? null : parsed};
        },
        // maskito formats the value itself when ngModel writes it (patched DefaultValueAccessor)
        format: value => value?.toString() ?? ''
    });
}
