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

import {ChangeDetectionStrategy, Component, model, signal} from '@angular/core';
import {
    applyWhen,
    disabled,
    form,
    FormField,
    max,
    min,
    required,
    validate
} from '@angular/forms/signals';
import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {TranslateModule} from '@ngx-translate/core';
import {SharedModule} from '../../../app/shared/shared.module';
import {GesuchFormError} from '../../shared/form/gesuch-form-error/gesuch-form-error';
import {GesuchFormNumberInput} from '../../shared/form/gesuch-form-number-input/gesuch-form-number-input';
import {GesuchSharedFormLabel} from '../../shared/form/style/gesuch-shared-form-label';
import {GesuchSharedFormLabelContent} from '../../shared/form/style/gesuch-shared-form-label-content';
import {GesuchSharedFormStyleMaterialInputWrapper} from '../../shared/form/style/gesuch-shared-form-style-material-input-wrapper/gesuch-shared-form-style-material-input-wrapper';
import {strictEmail} from '../../shared/form/validators/gesuch-form-validators';
import {GesuchPageHeadingComponent} from '../../shared/heading';
import {GesuchPageLayoutComponent} from '../../shared/page-layout';
import {KibTooltip} from '../../shared/kib-tooltip/kib-tooltip';
import {KibBisherComponent} from '../../shared/kib-bisher/kib-bisher.component';
import {BisherLabel} from '../../shared/kib-bisher/bisher-label';
import {BisherValue} from '../../shared/kib-bisher/bisher-value';

@Component({
    selector: 'kib-gesuch-erwerbspensum-view',
    imports: [
        TranslateModule,
        SharedModule,
        GesuchPageHeadingComponent,
        GesuchPageLayoutComponent,
        GesuchFormNumberInput,
        FormField,
        GesuchSharedFormLabelContent,
        GesuchSharedFormLabel,
        GesuchSharedFormStyleMaterialInputWrapper,
        GesuchFormError,
        MatFormField,
        MatInput,
        MatError,
        KibTooltip,
        KibBisherComponent,
        BisherLabel,
        BisherValue
    ],
    templateUrl: './erwerbspensum-view.component.html',
    styleUrl: './erwerbspensum-view.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErwerbspensumViewComponent {
    hasBisher = model(true);
    bisherValue: number = 90;
    isInvalid = model(false);
    isRequired = model(false);
    isDisabled = model(false);

    formModel = signal<PoCForm>({
        pensum: 0,
        zweitesPensum: 0,
        fehlerDemoPensum: null,
        fehlerDemoEmail: ''
    });
    form = form(this.formModel, p => {
        applyWhen(
            p.pensum,
            () => this.isRequired(),
            p => required(p)
        );
        applyWhen(
            p.pensum,
            () => this.isDisabled(),
            p => disabled(p)
        );
        validate(p.pensum, () =>
            this.isInvalid() ? {kind: 'error', message: 'Some error'} : null
        );
        required(p.zweitesPensum);
        required(p.fehlerDemoPensum);
        min(p.fehlerDemoPensum, 10);
        max(p.fehlerDemoPensum, 100);
        required(p.fehlerDemoEmail);
        strictEmail(p.fehlerDemoEmail);
    });
}

interface PoCForm {
    pensum: number | null;
    zweitesPensum: number | null;
    fehlerDemoPensum: number | null;
    fehlerDemoEmail: string;
}
