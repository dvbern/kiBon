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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {DvNgErrorMessages} from '../core/component/dv-error-messages/dv-ng-error-messages';
import {DvDebounceClickDirective} from '../debounceClick/dv-debounce-click.directive';

@NgModule({
    imports: [
        TranslateModule,
        CommonModule,
    ],
    declarations: [
        DvDebounceClickDirective,
        DvNgErrorMessages,
    ],
    entryComponents: [
        DvNgErrorMessages,
    ],
    exports: [
        DvDebounceClickDirective,
        DvNgErrorMessages,
    ],
})
export class NgSharedModule {
}


