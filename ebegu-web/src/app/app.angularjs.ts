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

import {ApplicationPropertyRsService} from '@utils/application-property-rs';
import {IAugmentedJQuery, IComponentOptions, IController} from 'angular';
import {MandantLogoNameVisitor, MANDANTS} from '@models/mandant';
import {ColorService} from './shared/services/color.service';
import {MandantService} from '@utils/mandant';

export class AppAngularjsComponent implements IController {
    public static $inject: string[] = [
        '$element',
        'ApplicationPropertyRsService',
        'MandantService'
    ];

    public constructor(
        private readonly $element: IAugmentedJQuery,
        private readonly applicationPropertyRS: ApplicationPropertyRsService,
        private readonly mandantService: MandantService
    ) {}

    public $postLink(): void {
        this.mandantService.mandant$.subscribe(mandant => {
            if (mandant === MANDANTS.NONE) {
                this.$element.find('.logo-bern').css('background-image', '');
                this.$element.find('#meldungsfenster').css('display', 'none');
                return;
            }
            const filename = new MandantLogoNameVisitor().process(mandant);
            this.$element
                .find('.logo-bern')
                .css('background-image', this.getBackgroundImage(filename));
        });
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(response => {
                this.$element
                    .find('#Intro')
                    .css('background-color', response.backgroundColor);
                this.$element
                    .find('.environment')
                    .css('display', response.devmode ? 'inline' : 'none');
                // Beim Ablösen bitte mit ngIf ersetzen, das hier ist ein Workaround, weil das Template hier nicht
                // auf den Controller zugreifen konnte
                if (!response.frenchEnabled) {
                    this.$element
                        .find('#language-selector')
                        .css('display', 'none');
                }
                if (response.devmode) {
                    document
                        .querySelector('meta[name="robots"]')
                        .setAttribute('content', 'noindex,nofollow');
                }
                ColorService.changeColors(response);
            });
    }

    public getBackgroundImage(filename: string): string {
        return `url("assets/images/${filename}")`;
    }
}

export const APP_ANGULARJS_COMPONENT: IComponentOptions = {
    template: require('./app.angularjs.html'),
    controller: AppAngularjsComponent
};
