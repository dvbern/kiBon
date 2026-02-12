/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {IComponentOptions, IFormController} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {isAtLeastFreigegeben} from '../../../../models/enums/TSAntragStatus';
import {TSAdresseContainer} from '../../../../models/TSAdresseContainer';
import {TSGemeinde} from '@kibon/shared/model/entity';

import {TSLand} from '../../../../models/types/TSLand';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {AdresseRS} from '../../service/adresseRS.rest';
import {ListResourceRS} from '../../service/listResourceRS.rest';
import ITranslateService = angular.translate.ITranslateService;

export class AdresseComponentConfig implements IComponentOptions {
    public transclude = false;
    public bindings = {
        adresse: '<',
        gemeinde: '<?',
        prefix: '@',
        organisation: '<',
        showNichtInGemeinde: '<',
        showIfBisherNone: '<'
    };
    public template = require('./dv-adresse.html');
    public controller = DvAdresseController;
    public controllerAs = 'vm';
    public require: any = {parentForm: '?^form'};
}

export class DvAdresseController {
    public static $inject = [
        'AdresseRS',
        'ListResourceRS',
        'GesuchModelManager',
        '$translate',
        'AuthServiceRS'
    ];

    public adresse: TSAdresseContainer;
    public gemeinde: TSGemeinde;
    public prefix: string;
    public parentForm: IFormController;
    public laenderList: TSLand[];
    public organisation: boolean;
    public readonly TSRoleUtil = TSRoleUtil;
    public showNichtInGemeinde: boolean;
    public bisherLand: string;

    public constructor(
        public readonly adresseRS: AdresseRS,
        listResourceRS: ListResourceRS,
        public readonly gesuchModelManager: GesuchModelManager,
        public readonly $translate: ITranslateService,
        private readonly authServiceRS: AuthServiceRS
    ) {
        this.TSRoleUtil = TSRoleUtil;
        this.bisherLand = this.getBisherLand();
        listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
    }

    public submit(): void {
        this.adresseRS.create(this.adresse).then((response: any) => {
            const responseCode = 201;
            if (response.status === responseCode) {
                this.resetForm();
            }
        });
    }

    public resetForm(): void {
        this.adresse = undefined;
    }

    public updateOrganisation() {
        const org = this.getModel().adresseJA.organisation;
        // Convert the string to an array
        const resultArray = org.split('');

        // If the string is empty or just one character, handle this case
        if (resultArray.length <= 1) {
            const tmp = resultArray[0];

            // Ensure the first four elements are ["c", "/", "o", " "]
            if (resultArray[0] !== 'c') {
                resultArray.unshift('c');
            }
            if (resultArray[1] !== '/') {
                resultArray[1] = '/';
            }
            if (resultArray[2] !== 'o') {
                resultArray[2] = 'o';
            }
            if (resultArray[3] !== ' ') {
                resultArray[3] = ' ';
            }
            resultArray[4] = tmp; // Needed otherwise the first char doesn't appear
        } else {
            let firstTypedChar = '';
            if (
                resultArray.length > 0 &&
                resultArray[0] !== 'c' &&
                resultArray[1] !== '/' &&
                resultArray[2] !== 'o' &&
                resultArray[3] !== ' '
            ) {
                firstTypedChar = resultArray.shift(); // Remove and preserve the first character
            }

            // Step 2: Ensure the first four elements are ["c", "/", "o", " "]
            if (resultArray[0] !== 'c') {
                resultArray[0] = 'c'; // Correct the first character
            }
            if (resultArray[1] !== '/') {
                resultArray[1] = '/'; // Correct the second character
            }
            if (resultArray[2] !== 'o') {
                resultArray[2] = 'o'; // Correct the third character
            }
            if (resultArray[3] !== ' ') {
                resultArray[3] = ' '; // Correct the fourth character
            }

            // Step 3: Reinsert the preserved typed character (if it exists)
            if (firstTypedChar) {
                resultArray.push(firstTypedChar); // Place the first typed character at the end
            }
        }
        // Convert the array back to string
        const updatedString = resultArray.join('');

        // Only update the model if the string has changed
        if (org !== updatedString) {
            this.getModel().adresseJA.organisation = updatedString;
        }
    }

    public restorePlaceholder() {
        if (this.getModel().adresseJA.organisation === 'c/o ') {
            this.getModel().adresseJA.organisation = ''; // Remove "c/o " if nothing else is typed
        }
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public showDatumVon(): boolean {
        return this.adresse && this.adresse.showDatumVon;
    }

    public getModel(): TSAdresseContainer {
        return this.adresse;
    }

    private getBisherLand(): string {
        if (
            this.getModel() &&
            this.getModel().adresseGS &&
            this.getModel().adresseGS.land
        ) {
            return this.$translate.instant(
                `Land_${this.getModel().adresseGS.land}`
            );
        }
        return '';
    }

    public enableNichtInGemeinde(): boolean {
        return (
            !this.isGesuchReadonly() &&
            this.gesuchModelManager.getGesuch() &&
            isAtLeastFreigegeben(this.gesuchModelManager.getGesuch().status) &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        );
    }

    public onDateChanged() {
        this.parentForm.$setDirty();
    }
}
