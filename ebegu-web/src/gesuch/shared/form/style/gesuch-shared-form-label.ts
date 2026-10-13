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
import {Directive, input} from '@angular/core';

@Directive({
    selector: '[kibGesuchSharedFormLabel]',
    host: {
        class: "tw:block tw:m-0 tw:min-h-[3rem] tw:text-[1.4rem] tw:font-normal tw:tracking-[0.1rem] tw:leading-[normal] tw:text-black tw:font-['Open_Sans',Arial,Helvetica,sans-serif]",
        "[class.tw:after:content-['_*']]": 'required()',
        // this is a legacy styling issue where labels are per default in the error color. To avoid this, we apply this class
        '[class.ignore-danger-font-color]': 'true',
        // this is a legacy styling issue where the after display is set to inline-block, leading to a different box-size
        // when an after content is displayed
        '[class.tw:after:inline]': 'true'
    }
})
export class GesuchSharedFormLabel {
    required = input(false);
    invalid = input(false);
}
