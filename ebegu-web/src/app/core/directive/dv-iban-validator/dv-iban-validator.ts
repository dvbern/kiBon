/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
    IAugmentedJQuery,
    IDirective,
    IDirectiveFactory,
    IDirectiveLinkFn,
    IScope
} from 'angular';
import {isValidIBAN, electronicFormatIBAN} from 'ibantools';
import {EbeguUtil} from '../../../../utils/EbeguUtil';

export class DvIbanValidator implements IDirective {
    public restrict = 'A';
    public require = 'ngModel';
    public link: IDirectiveLinkFn;

    public constructor() {
        this.link = (
            _scope: IScope,
            _element: IAugmentedJQuery,
            _attrs,
            ctrl: any
        ) => {
            if (!ctrl) {
                return;
            }

            ctrl.$validators.iban = (_modelValue: any, viewValue: any) =>
                EbeguUtil.isEmptyStringNullOrUndefined(viewValue) ||
                isValidIBAN(electronicFormatIBAN(viewValue));
        };
    }

    public static factory(): IDirectiveFactory {
        const directive = () => new DvIbanValidator();
        return directive;
    }
}
