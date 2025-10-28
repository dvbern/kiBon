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

import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {HybridFormBridgeService} from '@kibon/shared/util/hybrid-form-bridge';
import * as angular from 'angular';
import moment from 'moment';
import {CookieService} from 'ngx-cookie-service';
import {Observable, of} from 'rxjs';
import {KiBonMandant, MANDANTS} from '@kibon/shared-model-mandant';
import {MandantService} from '@kibon/shared-util-mandant-service';
import {TSInstitution} from '@kibon/shared/model/entity';
import {SharedUtilDvShowWarningAngabenVervollstaendingenService} from '@kibon/shared/util/dv-show-warning-angaben-vervollstaendingen';
import {EinstellungRS} from '../admin/service/einstellungRS.rest';
import {ErrorServiceX} from '../app/core/errors/service/ErrorServiceX';
import {BenutzerRSX} from '../app/core/service/benutzerRSX.rest';
import {DemoFeatureRS} from '../app/core/service/demoFeatureRS.rest';
import {EwkRS} from '../app/core/service/ewkRS.rest';
import {InstitutionRS} from '../app/core/service/institutionRS.rest';
import {VersionService} from '../app/core/service/version/version.service';
import {WindowRef} from '@kibon/shared-util-window-ref';
import {I18nServiceRSRest} from '../app/i18n/services/i18nServiceRS.rest';
import {GemeindeService} from '../app/shared/services/gemeinde.service';
import {AuthLifeCycleService} from '../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';
import {InternePendenzenRS} from '../gesuch/component/internePendenzenView/internePendenzenRS.rest';
import {KinderabzugExchangeService} from '../gesuch/component/kindView/service/kinderabzug-exchange.service';
import {FamiliensituationRS} from '../gesuch/service/familiensituationRS.service';
import {GesuchGenerator} from '../gesuch/service/gesuchGenerator';
import {SearchRS} from '../gesuch/service/searchRS.rest';
import {TSAuthEvent} from '../models/enums/TSAuthEvent';
import {TSBrowserLanguage} from '@kibon/shared/model/enums';
import {TSEingangsart} from '../models/enums/TSEingangsart';
import {TSBenutzer} from '../models/TSBenutzer';
import {TSDossier} from '../models/TSDossier';
import {TSEinstellung} from '../admin/einstellungen/TSEinstellung';
import {TSExceptionReport} from '../models/TSExceptionReport';
import {TSFall} from '../models/TSFall';
import {TSGesuch} from '../models/TSGesuch';
import {TSPublicAppConfig} from '@kibon/shared/model/einstellung';
import {EbeguRestUtil} from '../utils/EbeguRestUtil';

ngServicesMock.$inject = ['$provide'];

class GesuchGeneratorMock extends GesuchGenerator {
    public constructor() {
        super();
    }

    public initGesuch(eingangsart: TSEingangsart): angular.IPromise<TSGesuch> {
        const gesuch = new TSGesuch();
        gesuch.dossier = new TSDossier();
        gesuch.eingangsart = eingangsart;
        return Promise.resolve(gesuch);
    }

    public createNewGesuch(gesuch: TSGesuch): angular.IPromise<TSGesuch> {
        return Promise.resolve(gesuch);
    }

    public createNewDossier(dossier: TSDossier): angular.IPromise<TSDossier> {
        return Promise.resolve(dossier);
    }

    public createNewFall(fall: TSFall): angular.IPromise<TSFall> {
        return Promise.resolve(fall);
    }
}

class AuthLifeCycleServiceMock extends AuthLifeCycleService {
    public get$(event: TSAuthEvent): Observable<TSAuthEvent> {
        return of(event);
    }

    public changeAuthStatus(): void {
        return;
    }
}

class I18nServiceMock extends I18nServiceRSRest {
    public extractPreferredLanguage(): string {
        return TSBrowserLanguage.DE;
    }

    public currentLanguage(): TSBrowserLanguage {
        return TSBrowserLanguage.DE;
    }
}

class CookieServiceMock extends CookieService {
    public get(): string {
        return '';
    }
}

class ErrorServiceXMock extends ErrorServiceX {
    public getErrors(): ReadonlyArray<TSExceptionReport> {
        return [];
    }
}

class MandantServiceMock extends MandantService {
    public get mandant$(): Observable<KiBonMandant> {
        return of(MANDANTS.BERN);
    }
}

class EinstellungRSMock extends EinstellungRS {
    public findEinstellung(): Observable<TSEinstellung> {
        return of(new TSEinstellung());
    }

    public getAllEinstellungenBySystemCached(): Observable<TSEinstellung[]> {
        return of([]);
    }
}

class InstitutionRSMock extends InstitutionRS {
    public getInstitutionenEditableForCurrentBenutzer(): Observable<
        TSInstitution[]
    > {
        return of([]);
    }

    public getInstitutionenReadableForCurrentBenutzer(): Observable<
        TSInstitution[]
    > {
        return of([]);
    }
}

class SearchRSMock extends SearchRS {}

class BenutzerRSMock extends BenutzerRSX {}

class KinderabzugExchangeServiceMock extends KinderabzugExchangeService {}

class FamiliensituationRSMock extends FamiliensituationRS {}

class HybridFormBridgeServiceMock extends HybridFormBridgeService {}

class DemoFeatureRSMock extends DemoFeatureRS {
    public isDemoFeatureAllowed(): Observable<boolean> {
        return of(false);
    }
}

class AuthServiceRSMock extends AuthServiceRS {
    public initWithCookie(): angular.IPromise<TSBenutzer> {
        return Promise.resolve(new TSBenutzer());
    }

    public initLogin(): void {
        // NOP
    }

    public initLogout(): void {
        // NOP
    }
}

export function ngServicesMock($provide: angular.auto.IProvideService): void {
    $provide.service('I18nServiceRSRest', I18nServiceMock);
    $provide.service('AuthLifeCycleService', AuthLifeCycleServiceMock);
    $provide.factory('AuthServiceRS', AuthServiceRSMock);
    $provide.service('GesuchGenerator', GesuchGeneratorMock);
    $provide.service('InternePendenzenRS', InternePendenzenRS);
    $provide.service('BenutzerRS', BenutzerRSMock);
    $provide.service('VersionService', VersionService);
    $provide.service('MandantService', MandantServiceMock);
    $provide.service('EinstellungRS', EinstellungRSMock);
    $provide.service(EbeguRestUtil.name, EbeguRestUtil);
    $provide.factory(EwkRS.name, () =>
        jasmine.createSpyObj(EwkRS.name, ['sucheInEwk'])
    );
    $provide.factory(
        'SharedUtilDvShowWarningAngabenVervollstaendingenService',
        () => {
            const spy =
                jasmine.createSpyObj<SharedUtilDvShowWarningAngabenVervollstaendingenService>(
                    'SharedUtilDvShowWarningAngabenVervollstaendingenService',
                    ['showWarningAngabenVervollstaendigen']
                );
            spy.showWarningAngabenVervollstaendigen.and.returnValue(false);
            return spy;
        }
    );
    $provide.service('InstitutionRS', InstitutionRSMock);
    $provide.service('windowRef', WindowRef);
    $provide.service('cookieService', CookieServiceMock);
    $provide.service('ErrorServiceX', ErrorServiceXMock);
    $provide.service('SearchRS', SearchRSMock);
    $provide.service('FamiliensituationRS', FamiliensituationRSMock);
    $provide.service('DemoFeatureRS', DemoFeatureRSMock);
    $provide.service(
        'KinderabzugExchangeService',
        KinderabzugExchangeServiceMock
    );
    $provide.service('HybridFormBridgeService', HybridFormBridgeServiceMock);
    $provide.factory('FreigabeService', () =>
        jasmine.createSpyObj('FreigabeService', [
            'canBeFreigegeben',
            'getTextForFreigebenNotAllowed'
        ])
    );
    $provide.factory('SharedUtilApplicationPropertyRsService', () => {
        const spy =
            jasmine.createSpyObj<SharedUtilApplicationPropertyRsService>(
                'SharedUtilApplicationPropertyRsService',
                ['getPublicPropertiesCached', 'isDevMode']
            );
        spy.getPublicPropertiesCached.and.returnValue(
            of({
                steuerschnittstelleAktivAb: moment('2024-01-01'),
                angebotTSActivated: true
            } as TSPublicAppConfig)
        );
        spy.isDevMode.and.returnValue(of(true));
        return spy;
    });
    $provide.factory('BetreuungUtilAnmeldungRestService', () =>
        jasmine.createSpyObj('BetreuungUtilAnmeldungRestService', [
            'anmeldungSchulamtAblehnen'
        ])
    );
    $provide.service('GemeindeService', GemeindeService);
    $provide.value('LOCALE_ID', 'de-CH');
    $provide.value('platformId', 'de-CH');
}
