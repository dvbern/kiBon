import 'angular';
import './core.module.less';
import {appRun} from './core.route';
import {configure} from './config';
import router from '../dvbModules/router/router.module';
import AdresseRS from './service/adresseRS.rest';
import ListResourceRS from './service/listResourceRS.rest';
import EbeguRestUtil from '../utils/EbeguRestUtil';
import GesuchstellerRS from './service/gesuchstellerRS.rest.ts';
import {AdresseComponentConfig} from './component/dv-adresse/dv-adresse';
import {DvErrorMessagesComponentConfig} from './component/dv-error-messages/dv-error-messages';
import FallRS from '../gesuch/service/fallRS.rest';
import GesuchModelManager from '../gesuch/service/gesuchModelManager';
import GesuchRS from '../gesuch/service/gesuchRS.rest';
import FamiliensituationRS from '../gesuch/service/familiensituationRS.rest';
import DVMaxLength from './directive/dv-max-length';
import {DVDatepicker} from './directive/dv-datepicker/dv-datepicker';
import {FachstelleRS} from './service/fachstelleRS.rest';
import {DvInputContainerComponentConfig} from './component/dv-input-container/dv-input-container';
import {DvRadioContainerComponentConfig} from './component/dv-radio-container/dv-radio-container';

let dynamicDependencies = function (): string[] {

    let dynDep: string [] = ['unsavedChanges'];
    //deaktiviere unsavedChanges plugin fuer development
    if (ENV === 'development') {
        return [];
    }
    return dynDep;
};

const dependencies: string[] = [
    /* Angular modules */
    'ngAnimate',
    'ngSanitize',
    'ngMessages',
    'ngAria',
    'ngCookies',
    /* shared DVBern modules */
    router.name,
    /* 3rd-party modules */
    'ui.bootstrap',
    'smart-table',
    'ngMaterial',
    'ngMessages',
    'pascalprecht.translate',
    'angularMoment'
];


console.log(dependencies.concat(dynamicDependencies()));
export const EbeguWebCore: angular.IModule = angular
    .module('ebeguWeb.core', dependencies.concat(dynamicDependencies()))
    .run(appRun)
    .config(configure)
    .constant('REST_API', '/ebegu/api/v1/')
    .constant('MAX_LENGTH', 255)
    .constant('CONFIG', {
        name: 'EBEGU',
        REST_API: '/ebegu/api/v1/'
    })
    .service('EbeguRestUtil', EbeguRestUtil)
    .service('GesuchstellerRS', GesuchstellerRS)
    .service('AdresseRS', AdresseRS)
    .service('ListResourceRS', ListResourceRS)
    .service('FallRS', FallRS)
    .service('FamiliensituationRS', FamiliensituationRS)
    .service('GesuchModelManager', GesuchModelManager)
    .service('GesuchRS', GesuchRS)
    .directive('dvMaxLength', DVMaxLength.factory())
    .directive('dvDatepicker', DVDatepicker.factory())
    .service('FachstelleRS', FachstelleRS)
    .component('dvAdresse', new AdresseComponentConfig())
    .component('dvErrorMessages', new DvErrorMessagesComponentConfig())
    .component('dvInputContainer', new DvInputContainerComponentConfig())
    .component('dvRadioContainer', new DvRadioContainerComponentConfig());
