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

import {Component, Input} from '@angular/core';
import {StateService, TargetState} from '@uirouter/core';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import GemeindeRS from '../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import TSBenutzer from '../../models/TSBenutzer';
import TSGemeinde from '../../models/TSGemeinde';
import TSInstitution from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import AuthServiceRS from '../service/AuthServiceRS.rest';

// tslint:disable:no-duplicate-string no-identical-functions
@Component({
    selector: 'dv-local-login',
    templateUrl: './local-login.component.html',
    styleUrls: ['./local-login.component.less'],
})
export class LocalLoginComponent {

    private static readonly ID_BERN = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
    private static readonly ID_OSTERMUNDIGEN = '80a8e496-b73c-4a4a-a163-a0b2caf76487';

    @Input() public returnTo: TargetState;

    // Allgemeine User
    public superadmin: TSBenutzer;

    public administratorKantonBern: TSBenutzer;
    public sachbearbeiterKantonBern: TSBenutzer;

    public administratorInstitutionKitaBruennen: TSBenutzer;
    public sachbearbeiterInstitutionKitaBruennen: TSBenutzer;
    public sachbearbeiterTraegerschaftStadtBern: TSBenutzer;
    public administratorTraegerschaftStadtBern: TSBenutzer;

    public gesuchstellerEmmaGerber: TSBenutzer;
    public gesuchstellerHeinrichMueller: TSBenutzer;
    public gesuchstellerMichaelBerger: TSBenutzer;
    public gesuchstellerHansZimmermann: TSBenutzer;

    // Gemeindeabhängige User
    public administratorBGBern: TSBenutzer;
    public sachbearbeiterBGBern: TSBenutzer;
    public administratorTSBern: TSBenutzer;
    public sachbearbeiterTSBern: TSBenutzer;
    public administratorGemeindeBern: TSBenutzer;
    public sachbearbeiterGemeindeBern: TSBenutzer;

    public administratorBGOstermundigen: TSBenutzer;
    public sachbearbeiterBGOstermundigen: TSBenutzer;
    public administratorTSOstermundigen: TSBenutzer;
    public sachbearbeiterTSOstermundigen: TSBenutzer;
    public administratorGemeindeOstermundigen: TSBenutzer;
    public sachbearbeiterGemeindeOstermundigen: TSBenutzer;

    public administratorBGBernOstermundigen: TSBenutzer;
    public sachbearbeiterBGBernOstermundigen: TSBenutzer;
    public administratorTSBernOstermundigen: TSBenutzer;
    public sachbearbeiterTSBernOstermundigen: TSBenutzer;
    public administratorGemeindeBernOstermundigen: TSBenutzer;
    public sachbearbeiterGemeindeBernOstermundigen: TSBenutzer;

    public steueramtBern: TSBenutzer;
    public revisorBern: TSBenutzer;
    public juristBern: TSBenutzer;

    public steueramtOstermundigen: TSBenutzer;
    public revisorOstermundigen: TSBenutzer;
    public juristOstermundigen: TSBenutzer;

    public steueramtBernOstermundigen: TSBenutzer;
    public revisorBernOstermundigen: TSBenutzer;
    public juristBernOstermundigen: TSBenutzer;

    public devMode: boolean;

    private readonly mandant: TSMandant;
    private gemeindeBern: TSGemeinde;
    private gemeindeOstermundigen: TSGemeinde;
    private readonly institution: TSInstitution;
    private readonly traegerschaftStadtBern: TSTraegerschaft;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS,
    ) {

        this.mandant = LocalLoginComponent.getMandant();
        this.traegerschaftStadtBern = LocalLoginComponent.getTraegerschaftStadtBern();
        this.institution = this.getInsitution();
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });

        // getAktiveGemeinden() can be called by anonymous.
        this.gemeindeRS.getAktiveGemeinden().then(aktiveGemeinden => {
            this.gemeindeBern = aktiveGemeinden
                .find(gemeinde => gemeinde.id === LocalLoginComponent.ID_BERN);
            this.gemeindeOstermundigen = aktiveGemeinden
                .find(gemeinde => gemeinde.id === LocalLoginComponent.ID_OSTERMUNDIGEN);

            this.initUsers();
        });
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getMandant(): TSMandant {
        const mandant = new TSMandant();
        mandant.name = 'TestMandant';
        mandant.id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
        return mandant;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftStadtBern(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Kitas Stadt Bern';
        traegerschaft.id = 'f9ddee82-81a1-4cda-b273-fb24e9299308';
        return traegerschaft;
    }

    private initUsers(): void {
        this.createGeneralUsers();
        this.createUsersOfBern();
        this.createUsersOfOstermundigen();
        this.createUsersOfBothBernAndOstermundigen();
    }

    private createGeneralUsers(): void {
        this.superadmin = new TSBenutzer('E-BEGU',
            'Superuser',
            'ebegu',
            'password10',
            'superuser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SUPER_ADMIN);
        this.administratorKantonBern = new TSBenutzer('Bernhard',
            'Röthlisberger',
            'robe',
            'password1',
            'bernhard.roethlisberger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_MANDANT);
        this.sachbearbeiterKantonBern = new TSBenutzer('Benno',
            'Röthlisberger',
            'brbe',
            'password1',
            'benno.roethlisberger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_MANDANT);
        this.administratorInstitutionKitaBruennen = new TSBenutzer('Silvia',
            'Bergmann',
            'besi',
            'password1',
            'silvia.bergmann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterInstitutionKitaBruennen = new TSBenutzer('Sophie',
            'Bergmann',
            'beso',
            'password3',
            'sophie.bergmann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterTraegerschaftStadtBern = new TSBenutzer('Agnes',
            'Krause',
            'krad',
            'password4',
            'agnes.krause@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.administratorTraegerschaftStadtBern = new TSBenutzer('Bernhard',
            'Bern',
            'lelo',
            'password1',
            'bernhard.bern@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.gesuchstellerEmmaGerber = new TSBenutzer('Emma',
            'Gerber',
            'geem',
            'password6',
            'emma.gerber@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHeinrichMueller = new TSBenutzer('Heinrich',
            'Mueller',
            'muhe',
            'password6',
            'heinrich.mueller@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerMichaelBerger = new TSBenutzer('Michael',
            'Berger',
            'bemi',
            'password6',
            'michael.berger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHansZimmermann = new TSBenutzer('Hans',
            'Zimmermann',
            'ziha',
            'password6',
            'hans.zimmermann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
    }

    private createUsersOfBern(): void {
        this.administratorBGBern = new TSBenutzer('Kurt',
            'Blaser',
            'blku',
            'password5',
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterBGBern = new TSBenutzer('Jörg',
            'Becker',
            'jobe',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.administratorTSBern = new TSBenutzer('Adrian',
            'Schuler',
            'scad',
            'password9',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterTSBern = new TSBenutzer('Julien',
            'Schuler',
            'scju',
            'password9',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.administratorGemeindeBern = new TSBenutzer('Gerlinde',
            'Hofstetter',
            'hoge',
            'password1',
            'gerlinde.hofstetter@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterGemeindeBern = new TSBenutzer('Stefan',
            'Wirth',
            'wist',
            'password1',
            'stefan.wirth@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.steueramtBern = new TSBenutzer('Rodolfo',
            'Geldmacher',
            'gero',
            'password11',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.revisorBern = new TSBenutzer('Reto',
            'Revisor',
            'rere',
            'password9',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.juristBern = new TSBenutzer('Julia',
            'Jurist',
            'juju',
            'password9',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeBern]);
    }

    private createUsersOfOstermundigen(): void {
        this.administratorBGOstermundigen = new TSBenutzer('Kurt',
            'Schmid',
            'scku',
            'password1',
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterBGOstermundigen = new TSBenutzer('Jörg',
            'Keller',
            'kejo',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.administratorTSOstermundigen = new TSBenutzer('Adrian',
            'Huber',
            'huad',
            'password1',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterTSOstermundigen = new TSBenutzer('Julien',
            'Odermatt',
            'odju',
            'password1',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.administratorGemeindeOstermundigen = new TSBenutzer('Gerlinde',
            'Bader',
            'bage',
            'password1',
            'gerlinde.bader@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterGemeindeOstermundigen = new TSBenutzer('Stefan',
            'Weibel',
            'west',
            'password1',
            'stefan.weibel@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.steueramtOstermundigen = new TSBenutzer('Rodolfo',
            'Iten',
            'itro',
            'password1',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.revisorOstermundigen = new TSBenutzer('Reto',
            'Werlen',
            'were',
            'password1',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.juristOstermundigen = new TSBenutzer('Julia',
            'Adler',
            'adju',
            'password1',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
    }

    private createUsersOfBothBernAndOstermundigen(): void {
        this.administratorBGBernOstermundigen = new TSBenutzer('Kurt',
            'Kälin',
            'kaku',
            'password1',
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterBGBernOstermundigen = new TSBenutzer('Jörg',
            'Aebischer',
            'aejo',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.administratorTSBernOstermundigen = new TSBenutzer('Adrian',
            'Bernasconi',
            'bead',
            'password1',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterTSBernOstermundigen = new TSBenutzer('Julien',
            'Bucheli',
            'buju',
            'password1',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.administratorGemeindeBernOstermundigen = new TSBenutzer('Gerlinde',
            'Mayer',
            'mage',
            'password1',
            'gerlinde.mayer@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterGemeindeBernOstermundigen = new TSBenutzer('Stefan',
            'Marti',
            'mast',
            'password1',
            'stefan.marti@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.steueramtBernOstermundigen = new TSBenutzer('Rodolfo',
            'Hermann',
            'hero',
            'password1',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.revisorBernOstermundigen = new TSBenutzer('Reto',
            'Hug',
            'hure',
            'password1',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.juristBernOstermundigen = new TSBenutzer('Julia',
            'Lory',
            'luju',
            'password1',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Kita Brünnen';
        institution.id = '1b6f476f-e0f5-4380-9ef6-836d688853a3';
        institution.traegerschaft = this.traegerschaftStadtBern;
        institution.mandant = this.mandant;
        return institution;
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }
}