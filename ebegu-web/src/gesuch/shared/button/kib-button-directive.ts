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
 *
 */

import {Directive, ElementRef, inject} from '@angular/core';

/**
 * Adds the standard kiBon button styling and disabled-state to an element.
 *
 * <p>Apply this directive to a button or another clickable element using the
 * {@code kibButton} attribute.</p>
 *
 * <p>When the host element is disabled, the directive applies the disabled
 * styling. Otherwise, the element receives the pointer cursor styling.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * <button kibButton [disabled]="isDisabled">
 *     Save
 * </button>
 * }
 * </pre>
 */
@Directive({
    selector: '[kibButton]',
    host: {
        class: 'tw:h-16 tw:border-0 tw:px-6 tw:text-[1.6rem] tw:font-normal tw:tracking-[0.2rem] tw:text-white tw:uppercase tw:bg-primary-color tw:hover:bg-primary-color-dark tw:rounded-none tw:focus-visible:outline tw:focus-visible:outline-2 tw:outline-contrast-darkest tw:disabled:bg-contrast-default tw:disabled:text-black',
        '[class.opacity-40]': 'isDisabled',
        '[class.cursor-not-allowed]': 'isDisabled',
        '[class.cursor-pointer]': '!isDisabled'
    },
    standalone: true
})
export class KibButtonDirective {
    private el = inject(ElementRef);

    get isDisabled(): boolean {
        return (
            this.el.nativeElement.disabled ||
            this.el.nativeElement.hasAttribute('disabled')
        );
    }
}
