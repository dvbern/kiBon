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

import {IAttributes, IAugmentedJQuery, IDirective, IDirectiveFactory, IDirectiveLinkFn, IScope} from 'angular';
import {ISubscription} from 'rxjs/Subscription';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DVAntragListController} from '../../component/dv-antrag-list/dv-antrag-list';
import {DVsTPersistService} from '../../service/dVsTPersistService';
import {InstitutionRS} from '../../service/institutionRS.rest';
import UserRS from '../../service/userRS.rest';

/**
 * This directive allows a filter and sorting configuration to be saved after leaving the table.
 * The information will be stored in an angular-service.
 */
export default class DVSTPersistAntraege implements IDirective {

    restrict = 'A';
    require = ['^stTable', '^dvAntragList'];
    link: IDirectiveLinkFn;
    obss: ISubscription;

    static $inject: string[] = ['UserRS', 'InstitutionRS', 'AuthServiceRS', 'DVsTPersistService', 'GemeindeRS',
        'AuthLifeCycleService'];

    /* @ngInject */
    constructor(private userRS: UserRS,
                private institutionRS: InstitutionRS,
                private authServiceRS: AuthServiceRS,
                private dVsTPersistService: DVsTPersistService,
                private gemeindeRS: GemeindeRS,
                private authLifeCycleService: AuthLifeCycleService) {

        this.link = (scope: IScope, element: IAugmentedJQuery, attrs: IAttributes, ctrlArray: any) => {
            this.obss = this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
                .subscribe(() => this.loadData(attrs, ctrlArray, scope, dVsTPersistService));

            scope.$on('$destroy', () => {
                this.destroy();
            });
        };
    }

    /**
     * Die Directive wird nicht destroyed, daher muss man beim destroyen vom Scope die observables unsubscriben. Sollte dies nicht
     * gemacht werden, bleibt die Directive aktiv und der Code wird immer wieder ausfgefuehrt
     *
     * INFO: wir speichern die Observables in eine ISubscription die beim destroyen von scope unsubscribed werden muss. Die Alternative
     * mit takeUntil ist in diesem Fall (fuer eine Directive) nicht so gut weil es nicht completen kann. Da die Directive nur einmal erstellt
     * wird, wird der Constructor nur einmal ausgefuehrt und die unsubscription$ object deshalb nur einmal erstellt.
     */
    private destroy() {
        this.obss.unsubscribe();
    }

    private loadData(attrs: angular.IAttributes, ctrlArray: any, scope: angular.IScope, dVsTPersistService: DVsTPersistService) {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllRolesButGesuchsteller())) { // just to be sure that the user has the required role
            let nameSpace: string = attrs.dvStPersistAntraege;
            let stTableCtrl: any = ctrlArray[0];
            let antragListController: DVAntragListController = ctrlArray[1];

            //save the table state every time it changes
            scope.$watch(function () {
                return stTableCtrl.tableState();
            }, function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    // sessionStorage.setItem(nameSpace, JSON.stringify(newValue));
                    dVsTPersistService.saveData(nameSpace, newValue);
                }
            }, true);

            // if (sessionStorage.getItem(nameSpace)) {
            // let savedState = JSON.parse(sessionStorage.getItem(nameSpace));

            //fetch the table state when the directive is loaded
            let savedState = dVsTPersistService.loadData(nameSpace);
            savedState = this.setCurrentUserAsVerantwortlicher(antragListController, savedState);
            if (savedState) {
                if (savedState.search && savedState.search.predicateObject) { //update all objects of the model for the filters
                    antragListController.selectedAntragTyp = savedState.search.predicateObject.antragTyp;
                    antragListController.selectedGesuchsperiode =
                        savedState.search.predicateObject.gesuchsperiodeString;
                    antragListController.selectedAntragStatus = savedState.search.predicateObject.status;
                    antragListController.selectedBetreuungsangebotTyp = savedState.search.predicateObject.angebote;
                    this.setInstitutionFromName(antragListController, savedState.search.predicateObject.institutionen);
                    antragListController.selectedFallNummer = savedState.search.predicateObject.fallNummer;
                    antragListController.selectedFamilienName = savedState.search.predicateObject.familienName;
                    antragListController.selectedKinder = savedState.search.predicateObject.kinder;
                    antragListController.selectedAenderungsdatum = savedState.search.predicateObject.aenderungsdatum;
                    antragListController.selectedEingangsdatum = savedState.search.predicateObject.eingangsdatum;
                    antragListController.selectedDokumenteHochgeladen =
                        savedState.search.predicateObject.dokumenteHochgeladen;
                    antragListController.selectedEingangsdatumSTV = savedState.search.predicateObject.eingangsdatumSTV;
                    this.setGemeindeFromName(antragListController, savedState.search.predicateObject.gemeinde);
                    this.setVerantwortlicherBGFromName(antragListController,
                        savedState.search.predicateObject.verantwortlicherBG);
                    this.setVerantwortlicherTSFromName(antragListController,
                        savedState.search.predicateObject.verantwortlicherTS);
                }
                let tableState = stTableCtrl.tableState();

                angular.extend(tableState, savedState);
                stTableCtrl.pipe();

            }
        }
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherBGFromName(antragListController: DVAntragListController,
                                          verantwortlicherBGFullname: string): void {
        if (verantwortlicherBGFullname && antragListController) {
            this.userRS.getBenutzerJAorAdmin().then(userList => {
                antragListController.selectedVerantwortlicherBG = userList.find(
                    user => user.getFullName() === verantwortlicherBGFullname);
            });
        }
    }

    /**
     * Extracts the user out of her name. This method is needed because the filter saves the user using its name
     * while the dropdownlist is constructed using the object TSUser. So in order to be able to select the right user
     * with need the complete object and not only its Fullname.
     */
    private setVerantwortlicherTSFromName(antragListController: DVAntragListController,
                                          verantwortlicherTSFullname: string): void {
        if (verantwortlicherTSFullname && antragListController) {
            this.userRS.getBenutzerSCHorAdminSCH().then(userList => {
                antragListController.selectedVerantwortlicherTS = userList.find(
                    user => user.getFullName() === verantwortlicherTSFullname);
            });
        }
    }

    /**
     * Extracts the Institution from the institutionList of the controller using the name that had been saved in the
     * filter. This is needed because the filter saves the name and not the object.
     */
    private setInstitutionFromName(antragListController: DVAntragListController, institution: string): void {
        if (institution && antragListController) {
            this.institutionRS.getInstitutionenForCurrentBenutzer().then(institutionList => {
                if (institutionList) {
                    for (let i = 0; i < institutionList.length; i++) {
                        if (institutionList[i].name === institution) {
                            antragListController.selectedInstitution = institutionList[i];
                            break;
                        }
                    }
                }
            });
        }
    }

    private setGemeindeFromName(antragListController: DVAntragListController, gemeinde: string): void {
        if (gemeinde && antragListController) {
            this.gemeindeRS.getAllGemeinden().then(gemeindeList => {
                antragListController.selectedGemeinde = gemeindeList.find(g => g.name === gemeinde);
            });
        }
    }

    static factory(): IDirectiveFactory {
        const directive = (userRS: any,
                           institutionRS: any,
                           authServiceRS: any,
                           dVsTPersistService: any,
                           gemeindeRS: any,
                           authLifeCycleService: any) =>
            new DVSTPersistAntraege(userRS, institutionRS, authServiceRS, dVsTPersistService, gemeindeRS, authLifeCycleService);

        directive.$inject = ['UserRS', 'InstitutionRS', 'AuthServiceRS', 'DVsTPersistService', 'GemeindeRS', 'AuthLifeCycleService'];
        return directive;
    }

    /**
     * Setzt den aktuellen Benutzer als selectedVerantwotlicher wenn:
     * - es eine pendenzenListe ist: ctrl.pendenz===true
     * - es noch nicht gesetzt wurde, d.h. nichts war ausgewaehlt
     */
    private setCurrentUserAsVerantwortlicher(antragListController: DVAntragListController, savedState: any): any {
        let savedStateToReturn: any = angular.copy(savedState);
        if (antragListController.pendenz) {
            if (!savedStateToReturn) {
                savedStateToReturn = {search: {predicateObject: this.extractVerantwortlicherFullName()}};
            }
            if (!savedStateToReturn.search.predicateObject) {
                savedStateToReturn.search.predicateObject = this.extractVerantwortlicherFullName();
            }
            if (!savedStateToReturn.search.predicateObject.verantwortlicher) {
                let berechtigung: TSBerechtigung = this.authServiceRS.getPrincipal().currentBerechtigung;
                if (berechtigung.role === TSRole.ADMINISTRATOR_SCHULAMT || berechtigung.role === TSRole.SCHULAMT) {
                    savedStateToReturn.search.predicateObject.verantwortlicherTS =
                        this.authServiceRS.getPrincipal().getFullName();
                } else { //JA
                    savedStateToReturn.search.predicateObject.verantwortlicherBG =
                        this.authServiceRS.getPrincipal().getFullName();
                }
            }
        }
        return savedStateToReturn;
    }

    private extractVerantwortlicherFullName() {
        if (this.authServiceRS.getPrincipal()) {
            let berechtigung: TSBerechtigung = this.authServiceRS.getPrincipal().currentBerechtigung;
            if (berechtigung.role === TSRole.ADMINISTRATOR_SCHULAMT || berechtigung.role === TSRole.SCHULAMT) {
                return {verantwortlicherTS: this.authServiceRS.getPrincipal().getFullName()};
            } else { //JA
                return {verantwortlicherBG: this.authServiceRS.getPrincipal().getFullName()};
            }
        }
        return '';
    }
}
