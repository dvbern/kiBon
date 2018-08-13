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

import {Ng1StateDeclaration} from '@uirouter/angularjs';
import KindRS from '../app/core/service/kindRS.rest';
import AuthServiceRS from '../authentication/service/AuthServiceRS.rest';
import {RouterHelper} from '../dvbModules/router/route-helper-provider';
import {TSAntragTyp} from '../models/enums/TSAntragTyp';
import {TSEingangsart} from '../models/enums/TSEingangsart';
import TSGesuch from '../models/TSGesuch';
import TSKindDublette from '../models/TSKindDublette';
import TSMahnung from '../models/TSMahnung';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {GesuchRouteController} from './gesuch';
import BerechnungsManager from './service/berechnungsManager';
import GesuchModelManager from './service/gesuchModelManager';
import MahnungRS from './service/mahnungRS.rest';
import WizardStepManager from './service/wizardStepManager';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;

const gesuchTpl = require('./gesuch.html');

gesuchRun.$inject = ['RouterHelper'];

export function gesuchRun(routerHelper: RouterHelper) {
    routerHelper.configureStates(ng1States, []);
}

//STATES

export class EbeguGesuchState implements Ng1StateDeclaration {
    parent = 'app';
    abstract = true;
    name = 'gesuch';
    template = gesuchTpl;
    url = '/gesuch';
    controller = GesuchRouteController;
    controllerAs = 'vm';
}

export class EbeguNewFallState implements Ng1StateDeclaration {
    name = 'gesuch.fallcreation';
    url = '/fall/:createNewFall/:createNewDossier/:createNewGesuch/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId/:gemeindeId';
    params = {
        eingangsart: '',
        createNewDossier: 'false',
        createNewGesuch: 'false',
        gesuchsperiodeId: '',
        gesuchId: '',
        dossierId: '',
        gemeindeId: '',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<fall-creation-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: reloadGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };
}

export class EbeguMutationState implements Ng1StateDeclaration {
    name = 'gesuch.mutation';
    url = '/mutation/:createMutation/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<fall-creation-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: createEmptyMutation
    };

    data = {
        roles: TSRoleUtil.getAdministratorJugendamtSchulamtGesuchstellerRoles()
    };
}

export class EbeguErneuerungsgesuchState implements Ng1StateDeclaration {
    name = 'gesuch.erneuerung';
    url = '/erneuerung/:createErneuerung/:eingangsart/:gesuchsperiodeId/:gesuchId/:dossierId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<fall-creation-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: createEmptyErneuerungsgesuch
    };

    data = {
        roles: TSRoleUtil.getAdministratorJugendamtSchulamtGesuchstellerRoles()
    };
}

export class EbeguFamiliensituationState implements Ng1StateDeclaration {
    name = 'gesuch.familiensituation';
    url = '/familiensituation/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<familiensituation-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

export class EbeguStammdatenState implements Ng1StateDeclaration {
    name = 'gesuch.stammdaten';
    url = '/stammdaten/:gesuchId/:gesuchstellerNumber';
    params = {
        gesuchstellerNumber: '1',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<stammdaten-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

export class EbeguUmzugState implements Ng1StateDeclaration {
    name = 'gesuch.umzug';
    url = '/umzug/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<umzug-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

export class EbeguKinderListState implements Ng1StateDeclaration {
    name = 'gesuch.kinder';
    url = '/kinder/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<kinder-list-view kinder-dubletten="$resolve.kinderDubletten">'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager,
        kinderDubletten: getKinderDubletten
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguKindState implements Ng1StateDeclaration {
    name = 'gesuch.kind';
    url = '/kinder/kind/:gesuchId/:kindNumber';
    params = {
        kindNumber: '',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<kind-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguBetreuungListState implements Ng1StateDeclaration {
    name = 'gesuch.betreuungen';
    url = '/betreuungen/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<betreuung-list-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

export class EbeguBetreuungState implements Ng1StateDeclaration {
    name = 'gesuch.betreuung';
    url = '/betreuungen/betreuung/:gesuchId/:kindNumber/:betreuungNumber/:betreuungsangebotTyp';
    params = {
        betreuungsangebotTyp: '',
        betreuungNumber: '',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<betreuung-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

export class EbeguAbwesenheitState implements Ng1StateDeclaration {
    name = 'gesuch.abwesenheit';
    url = '/abwesenheit/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<abwesenheit-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButSteueramt()
    };
}

export class EbeguErwerbspensenListState implements Ng1StateDeclaration {
    name = 'gesuch.erwerbsPensen';
    url = '/erwerbspensen/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<erwerbspensum-list-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };
}

export class EbeguErwerbspensumState implements Ng1StateDeclaration {
    name = 'gesuch.erwerbsPensum';
    url = '/erwerbspensen/erwerbspensum/:gesuchId/:gesuchstellerNumber/:erwerbspensumNum';
    params = {
        erwerbspensumNum: '',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<erwerbspensum-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };
}

export class EbeguFinanzielleSituationState implements Ng1StateDeclaration {
    name = 'gesuch.finanzielleSituation';
    url = '/finanzielleSituation/:gesuchId/:gesuchstellerNumber';
    params = {
        gesuchstellerNumber: '1',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<finanzielle-situation-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguFinanzielleSituationStartState implements Ng1StateDeclaration {
    name = 'gesuch.finanzielleSituationStart';
    url = '/finanzielleSituationStart/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<finanzielle-situation-start-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguFinanzielleSituationResultateState implements Ng1StateDeclaration {
    name = 'gesuch.finanzielleSituationResultate';
    url = '/finanzielleSituationResultate/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<finanzielle-situation-resultate-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguVerfuegenListState implements Ng1StateDeclaration {
    name = 'gesuch.verfuegen';
    url = '/verfuegen/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<verfuegen-list-view mahnung-list="$resolve.mahnungList">'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager,
        mahnungList: getMahnungen
    };

    data = {
        roles: TSRoleUtil.getAllRolesButSteueramt()
    };
}

export class EbeguVerfuegenState implements Ng1StateDeclaration {
    name = 'gesuch.verfuegenView';
    url = '/verfuegenView/:gesuchId/:betreuungNumber/:kindNumber';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<verfuegen-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButSteueramt()
    };
}

export class EbeguEinkommensverschlechterungInfoState implements Ng1StateDeclaration {
    name = 'gesuch.einkommensverschlechterungInfo';
    url = '/einkommensverschlechterungInfo/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<einkommensverschlechterung-info-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguEinkommensverschlechterungSteuernState implements Ng1StateDeclaration {
    name = 'gesuch.einkommensverschlechterungSteuern';
    url = '/einkommensverschlechterungSteuern/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<einkommensverschlechterung-steuern-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguEinkommensverschlechterungState implements Ng1StateDeclaration {
    name = 'gesuch.einkommensverschlechterung';
    url = '/einkommensverschlechterung/:gesuchId/:gesuchstellerNumber/:basisjahrPlus';
    params = {
        gesuchstellerNumber: '1',
        basisjahrPlus: '1',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<einkommensverschlechterung-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguEinkommensverschlechterungResultateState implements Ng1StateDeclaration {
    name = 'gesuch.einkommensverschlechterungResultate';
    url = '/einkommensverschlechterungResultate/:gesuchId/:basisjahrPlus';
    params = {
        basisjahrPlus: '1',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<einkommensverschlechterung-resultate-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitution()
    };
}

export class EbeguDokumenteState implements Ng1StateDeclaration {
    name = 'gesuch.dokumente';
    url = '/dokumente/:gesuchId/:gesuchstellerNumber';
    params = {
        gesuchstellerNumber: '1',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<dokumente-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };
}

export class EbeguFreigabeState implements Ng1StateDeclaration {
    name = 'gesuch.freigabe';
    url = '/freigabe/:gesuchId';

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<freigabe-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRolesButTraegerschaftInstitutionSteueramt()
    };
}

export class EbeguBetreuungMitteilungState implements Ng1StateDeclaration {
    name = 'gesuch.mitteilung';
    url = '/mitteilung/:dossierId/:gesuchId/:betreuungId/:mitteilungId';
    params = {
        mitteilungId: '',
    };

    views: { [name: string]: Ng1StateDeclaration } = {
        'gesuchViewPort': {
            template: '<betreuung-mitteilung-view>'
        },
        'kommentarViewPort': {
            template: '<kommentar-view>'
        }
    };

    resolve = {
        gesuch: getGesuchModelManager
    };

    data = {
        roles: TSRoleUtil.getAllRoles()
    };
}

const ng1States: Ng1StateDeclaration[] = [
    new EbeguGesuchState(),
    new EbeguFamiliensituationState(),
    new EbeguStammdatenState(),
    new EbeguUmzugState(),
    new EbeguKinderListState(),
    new EbeguFinanzielleSituationStartState(),
    new EbeguFinanzielleSituationState(),
    new EbeguFinanzielleSituationResultateState(),
    new EbeguKindState(),
    new EbeguErwerbspensenListState(),
    new EbeguErwerbspensumState(),
    new EbeguBetreuungListState(),
    new EbeguBetreuungState(),
    new EbeguAbwesenheitState(),
    new EbeguNewFallState(),
    new EbeguMutationState(),
    new EbeguErneuerungsgesuchState(),
    new EbeguVerfuegenListState(),
    new EbeguVerfuegenState(),
    new EbeguEinkommensverschlechterungInfoState(),
    new EbeguEinkommensverschlechterungSteuernState(),
    new EbeguEinkommensverschlechterungState(),
    new EbeguEinkommensverschlechterungResultateState(),
    new EbeguDokumenteState(),
    new EbeguFreigabeState(),
    new EbeguBetreuungMitteilungState(),
    // new OnboardingTest()
];

//PARAMS

export class IGesuchStateParams {
    gesuchId: string;
}

export class IStammdatenStateParams {
    gesuchstellerNumber: string;
}

export class IKindStateParams {
    kindNumber: string;
}

export class IBetreuungStateParams {
    betreuungNumber: string;
    kindNumber: string;
    betreuungsangebotTyp: string;
}

export class INewFallStateParams {
    createNewFall: string;
    createNewDossier: string;
    createNewGesuch: string;
    createMutation: string;
    eingangsart: TSEingangsart;
    gesuchsperiodeId: string;
    gesuchId: string;
    dossierId: string;
    gemeindeId: string;
}

export class IErwerbspensumStateParams {
    gesuchstellerNumber: string;
    erwerbspensumNum: string;
}

export class IEinkommensverschlechterungStateParams {
    gesuchstellerNumber: string;
    basisjahrPlus: string;
}

export class IEinkommensverschlechterungResultateStateParams {
    basisjahrPlus: string;
}

// FIXME dieses $inject wird ignoriert, d.h, der Parameter der Funktion muss exact dem Namen des Services entsprechen (Grossbuchstaben am Anfang). Warum?
getMahnungen.$inject = ['MahnungRS', '$stateParams', '$q', '$log'];

export function getMahnungen(MahnungRS: MahnungRS, $stateParams: IGesuchStateParams, $q: IQService, $log: ILogService) {
    // return [];
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            return MahnungRS.findMahnungen(gesuchIdParam);
        }
    }
    $log.warn('keine stateParams oder keine gesuchId, gebe undefined zurueck');
    const deferred = $q.defer<TSMahnung[]>();
    deferred.resolve(undefined);
    return deferred.promise;
}

getGesuchModelManager.$inject = ['GesuchModelManager', 'BerechnungsManager', 'WizardStepManager', '$stateParams', '$q', '$log'];

export function getGesuchModelManager(gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                                      wizardStepManager: WizardStepManager, $stateParams: IGesuchStateParams, $q: IQService,
                                      $log: ILogService): IPromise<TSGesuch> {
    if ($stateParams) {
        const gesuchIdParam = $stateParams.gesuchId;
        if (gesuchIdParam) {
            if (!gesuchModelManager.getGesuch() || gesuchModelManager.getGesuch() && gesuchModelManager.getGesuch().id !== gesuchIdParam
                || gesuchModelManager.getGesuch().emptyCopy) {
                // Wenn die antrags id im GescuchModelManager nicht mit der GesuchId ueberreinstimmt wird das gesuch neu geladen
                // Ebenfalls soll das Gesuch immer neu geladen werden, wenn es sich beim Gesuch im Gesuchmodelmanager um eine leere Mutation handelt
                // oder um ein leeres Erneuerungsgesuch
                berechnungsManager.clear();
                return gesuchModelManager.openGesuch(gesuchIdParam);
            } else {
                const deferred = $q.defer<TSGesuch>();
                deferred.resolve(gesuchModelManager.getGesuch());
                return deferred.promise;
            }

        }
    }
    $log.warn('keine stateParams oder keine gesuchId, gebe undefined zurueck');
    const deferred = $q.defer<TSGesuch>();
    deferred.resolve(undefined);
    return deferred.promise;
}

reloadGesuchModelManager.$inject = ['GesuchModelManager', 'BerechnungsManager', 'WizardStepManager', '$stateParams', '$q', '$log'];

export function reloadGesuchModelManager(gesuchModelManager: GesuchModelManager, berechnungsManager: BerechnungsManager,
                                         wizardStepManager: WizardStepManager, $stateParams: INewFallStateParams, $q: any,
                                         $log: ILogService): IPromise<any> {
    if ($stateParams) {

        const eingangsart = $stateParams.eingangsart;
        const gemeindeId = $stateParams.gemeindeId;
        const gesuchsperiodeId = $stateParams.gesuchsperiodeId;

        if ($stateParams.createNewFall === 'true') {
            //initialize gesuch
            return gesuchModelManager.createNewFall(eingangsart, gemeindeId);
        }

        const createNewDossierParam = $stateParams.createNewDossier;
        if (createNewDossierParam === 'true') {
            return gesuchModelManager.createNewDossierForCurrentFall(eingangsart, gemeindeId);
        }

        const createNewGesuchParam = $stateParams.createNewGesuch;
        if (createNewGesuchParam === 'true') {
            return gesuchModelManager.initGesuch(eingangsart, false, false, gesuchsperiodeId);
        }

        const gesuchIdParam = $stateParams.gesuchId;
        if (!gesuchIdParam) {
            $log.error('opened fallCreation without gesuchId parameter in edit mode', $stateParams);
        }

        berechnungsManager.clear();
        return gesuchModelManager.openGesuch(gesuchIdParam);

    }
    $log.warn('no state params available fo page fallCreation, this is probably a bug');
    return $q.defer(gesuchModelManager.getGesuch());
}

getKinderDubletten.$inject = ['$stateParams', '$q', '$log', 'KindRS', 'AuthServiceRS'];

// Die Kinderdubletten werden nur für SCH-Mitarbeiter oder JA-Mitarbeiter (inkl. Revisor und Jurist) angezeigt
export function getKinderDubletten($stateParams: IGesuchStateParams, $q: IQService, $log: ILogService, KindRS: KindRS, authService: AuthServiceRS) {
    const isUserAllowed: boolean = authService.isOneOfRoles(TSRoleUtil.getJugendamtAndSchulamtRole());
    if (isUserAllowed && $stateParams && $stateParams.gesuchId) {
        const gesuchIdParam = $stateParams.gesuchId;
        return KindRS.getKindDubletten(gesuchIdParam);
    }
    const deferred = $q.defer<TSKindDublette[]>();
    deferred.resolve(undefined);
    return deferred.promise;
}

createEmptyMutation.$inject = ['GesuchModelManager', '$stateParams', '$q'];

export function createEmptyMutation(gesuchModelManager: GesuchModelManager, $stateParams: INewFallStateParams, $q: any): IPromise<TSGesuch> {
    return createEmptyGesuchFromGesuch($stateParams, gesuchModelManager, $q, TSAntragTyp.MUTATION);
}

createEmptyErneuerungsgesuch.$inject = ['GesuchModelManager', '$stateParams', '$q'];

export function createEmptyErneuerungsgesuch(gesuchModelManager: GesuchModelManager, $stateParams: INewFallStateParams, $q: any): IPromise<TSGesuch> {
    return createEmptyGesuchFromGesuch($stateParams, gesuchModelManager, $q, TSAntragTyp.ERNEUERUNGSGESUCH);
}

function createEmptyGesuchFromGesuch($stateParams: INewFallStateParams, gesuchModelManager: GesuchModelManager,
                                     $q: any, antragtyp: TSAntragTyp): IPromise<TSGesuch> {
    if ($stateParams) {
        const gesuchId = $stateParams.gesuchId;
        const eingangsart = $stateParams.eingangsart;
        const gesuchsperiodeId = $stateParams.gesuchsperiodeId;
        const dossierId = $stateParams.dossierId;

        if (gesuchId && eingangsart) {
            if (antragtyp === TSAntragTyp.ERNEUERUNGSGESUCH) {
                gesuchModelManager.initErneuerungsgesuch(gesuchId, eingangsart, gesuchsperiodeId, dossierId);
            } else if (antragtyp === TSAntragTyp.MUTATION) {
                gesuchModelManager.initMutation(gesuchId, eingangsart, gesuchsperiodeId, dossierId);
            }
        }
    }
    return $q.defer(gesuchModelManager.getGesuch());
}
