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

import {
    ChangeDetectionStrategy,
    Component,
    computed,
    input
} from '@angular/core';
import {ValidationError} from '@angular/forms/signals';
import {TranslatePipe} from '@ngx-translate/core';
import {
    GesuchFormErrorMessageOverrides,
    prioritizeError,
    resolveErrorMessage
} from './gesuch-form-error.util';

/**
 * Renders the prioritized, translated message of a field's validation errors.
 *
 * Must be placed inside a `<mat-error>` which itself is a direct child of `<mat-form-field>`,
 * so Angular Material keeps handling visibility and accessibility:
 *
 * ```html
 * <mat-error><kib-gesuch-form-error [errors]="form.pensum().errors()" /></mat-error>
 * ```
 */
@Component({
    selector: 'kib-gesuch-form-error',
    imports: [TranslatePipe],
    templateUrl: './gesuch-form-error.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchFormError {
    errors = input.required<readonly ValidationError[]>();

    /**
     * Allows overriding the translation keys per error kind, replacing the message of that kind
     *
     * <b>Example:</b>
     * ```html
     * <input [formField]="form.pensum()" />
     *
     * <mat-error>
     *     <kib-gesuch-form-error
     *      [errors]="form.pensum().errors()"
     *      [messageOverrides]="{required: 'CUSTOM_REQUIRED_MESSAGE'}" />
     * </mat-error>
     * ```
     * */
    messageOverrides = input<GesuchFormErrorMessageOverrides>({});

    protected readonly message = computed(() => {
        const error = prioritizeError(this.errors());
        return error
            ? resolveErrorMessage(error, this.messageOverrides())
            : null;
    });
}
