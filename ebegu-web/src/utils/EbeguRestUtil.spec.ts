import '../bootstrap.ts';
import 'angular-mocks';
import EbeguRestUtil from './EbeguRestUtil';
import TSAdresse from '../models/TSAdresse';
import {EbeguWebCore} from '../core/core.module';
import TSPerson from '../models/TSPerson';
import {TSGeschlecht} from '../models/enums/TSGeschlecht';
import {TSAdressetyp} from '../models/enums/TSAdressetyp';
import IInjectorService = angular.auto.IInjectorService;
import IHttpBackendService = angular.IHttpBackendService;
import {TSFachstelle} from '../models/TSFachstelle';
import TSAbstractEntity from '../models/TSAbstractEntity';
import {TSMandant} from '../models/TSMandant';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import {TSInstitution} from '../models/TSInstitution';
import {TSInstitutionStammdaten} from '../models/TSInstitutionStammdaten';
import {TSBetreuungsangebotTyp} from '../models/enums/TSBetreuungsangebotTyp';
import DateUtil from './DateUtil';

describe('EbeguRestUtil', function () {

    let ebeguRestUtil: EbeguRestUtil;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.inject(function ($injector: any) {
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    describe('publicAPI', () => {
        it('should include a parseAdresse() function', function () {
            expect(ebeguRestUtil.parseAdresse).toBeDefined();
        });
        it('should include a parsePerson() function', function () {
            expect(ebeguRestUtil.parsePerson).toBeDefined();
        });
        it('should include a fachstelleToRestObject() function', function () {
            expect(ebeguRestUtil.fachstelleToRestObject).toBeDefined();
        });
        it('should include a parseFachstelle() function', function () {
            expect(ebeguRestUtil.parseFachstelle).toBeDefined();
        });
        it('should include a mandantToRestObject() function', function () {
            expect(ebeguRestUtil.mandantToRestObject).toBeDefined();
        });
        it('should include a parseMandant() function', function () {
            expect(ebeguRestUtil.parseMandant).toBeDefined();
        });
        it('should include a traegerschaftToRestObject() function', function () {
            expect(ebeguRestUtil.traegerschaftToRestObject).toBeDefined();
        });
        it('should include a parseTraegerschaft() function', function () {
            expect(ebeguRestUtil.parseTraegerschaft).toBeDefined();
        });
        it('should include a institutionToRestObject() function', function () {
            expect(ebeguRestUtil.institutionToRestObject).toBeDefined();
        });
        it('should include a parseInstitution() function', function () {
            expect(ebeguRestUtil.parseInstitution).toBeDefined();
        });
        it('should include a institutionStammdatenToRestObject() function', function () {
            expect(ebeguRestUtil.institutionStammdatenToRestObject).toBeDefined();
        });
        it('should include a parseInstitutionStammdaten() function', function () {
            expect(ebeguRestUtil.parseInstitutionStammdaten).toBeDefined();
        });
    });

    describe('API Usage', function () {
        describe('parseAdresse()', () => {
            it('should transfrom Adresse Rest Objects', () => {
                let addresse = new TSAdresse();
                addresse.adresseTyp = TSAdressetyp.WOHNADRESSE;
                addresse.gemeinde = 'Testingen';
                addresse.land = 'CH';
                addresse.ort = 'Testort';
                addresse.strasse = 'Teststrasse';
                addresse.hausnummer = '1';
                addresse.zusatzzeile = 'co test';
                addresse.plz = '3014';
                addresse.id = '1234567';

                let restAdresse: any =  ebeguRestUtil.adresseToRestObject({}, addresse);
                expect(restAdresse).toBeDefined();
                let adr: TSAdresse = ebeguRestUtil.parseAdresse(new TSAdresse(), restAdresse);
                expect(adr).toBeDefined();
                expect(addresse.gemeinde).toEqual(adr.gemeinde);
                expect(addresse).toEqual(adr);

            });
        });
        describe('parsePerson()', () => {
            it('should transfrom TSPerson to REST Obj and back', () => {
                let myPerson =  new TSPerson();
                myPerson.vorname = 'Til';
                myPerson.nachname = 'Testperson';
                myPerson.id = 'mytestid';
                myPerson.geschlecht = TSGeschlecht.MAENNLICH;
                myPerson.telefon = '+41 76 300 12 34';
                myPerson.mobile = '+41 76 300 12 34';
                myPerson.umzug = false;
                myPerson.mail = 'Til.Testperson@example.com';
                myPerson.korrespondenzAdresse = undefined;
                myPerson.umzugAdresse = undefined;
                myPerson.adresse = undefined;
                myPerson.timestampErstellt = undefined;
                myPerson.timestampMutiert = undefined;
                let restPerson =  ebeguRestUtil.personToRestObject({}, myPerson);
                expect(restPerson).toBeDefined();
                let transformedPers: TSPerson = ebeguRestUtil.parsePerson(new TSPerson(), restPerson);
                expect(transformedPers).toBeDefined();
                expect(myPerson.nachname).toEqual(transformedPers.nachname);
                expect(myPerson).toEqual(transformedPers);

            });
        });
        describe('parseFachstelle()', () => {
           it('should transform TSFachstelle to REST object and back', () => {
               let myFachstelle = new TSFachstelle('Fachstelle_name', 'Beschreibung', true);
               setAbstractFieldsUndefined(myFachstelle);

               let restFachstelle = ebeguRestUtil.fachstelleToRestObject({}, myFachstelle);
               expect(restFachstelle).toBeDefined();
               expect(restFachstelle.name).toEqual(myFachstelle.name);
               expect(restFachstelle.beschreibung).toEqual(myFachstelle.beschreibung);
               expect(restFachstelle.behinderungsbestaetigung).toEqual(myFachstelle.behinderungsbestaetigung);

               let transformedFachstelle = ebeguRestUtil.parseFachstelle(new TSFachstelle(), restFachstelle);
               expect(transformedFachstelle).toBeDefined();
               expect(transformedFachstelle).toEqual(myFachstelle);
           });
        });
        describe('parseMandant()', () => {
            it('should transform TSMandant to REST object and back', () => {
                let myMandant = new TSMandant('myMandant');
                setAbstractFieldsUndefined(myMandant);

                let restMandant = ebeguRestUtil.mandantToRestObject({}, myMandant);
                expect(restMandant).toBeDefined();
                expect(restMandant.name).toEqual(myMandant.name);

                let transformedMandant = ebeguRestUtil.parseMandant(new TSMandant(), restMandant);
                expect(transformedMandant).toBeDefined();
                expect(transformedMandant).toEqual(myMandant);
            });
        });
        describe('parseTraegerschaft()', () => {
            it('should transform TSTraegerschaft to REST object and back', () => {
                let myTraegerschaft = new TSTraegerschaft('myTraegerschaft');
                setAbstractFieldsUndefined(myTraegerschaft);

                let restTraegerschaft = ebeguRestUtil.traegerschaftToRestObject({}, myTraegerschaft);
                expect(restTraegerschaft).toBeDefined();
                expect(restTraegerschaft.name).toEqual(myTraegerschaft.name);

                let transformedTraegerschaft = ebeguRestUtil.parseTraegerschaft(new TSTraegerschaft(), restTraegerschaft);
                expect(transformedTraegerschaft).toBeDefined();
                expect(transformedTraegerschaft).toEqual(myTraegerschaft);
            });
        });
        describe('parseInstitution()', () => {
            it('should transform TSInstitution to REST object and back', () => {
                var myInstitution = createInstitution();

                let restInstitution = ebeguRestUtil.institutionToRestObject({}, myInstitution);
                expect(restInstitution).toBeDefined();
                expect(restInstitution.name).toEqual(myInstitution.name);
                expect(restInstitution.traegerschaft.name).toEqual(myInstitution.traegerschaft.name);
                expect(restInstitution.mandant.name).toEqual(myInstitution.mandant.name);

                let transformedInstitution = ebeguRestUtil.parseInstitution(new TSInstitution(), restInstitution);
                expect(transformedInstitution).toBeDefined();
                expect(transformedInstitution).toEqual(myInstitution);
            });
        });
        describe('parseInstitutionStammdaten()', () => {
            it('should transform TSInstitutionStammdaten to REST object and back', () => {
                var myInstitution = createInstitution();
                let myInstitutionStammdaten = new TSInstitutionStammdaten('iban', 250, 12, TSBetreuungsangebotTyp.KITA, myInstitution,
                    DateUtil.today(), DateUtil.today());
                setAbstractFieldsUndefined(myInstitutionStammdaten);

                let restInstitutionStammdaten = ebeguRestUtil.institutionStammdatenToRestObject({}, myInstitutionStammdaten);
                expect(restInstitutionStammdaten).toBeDefined();
                expect(restInstitutionStammdaten.iban).toEqual(myInstitutionStammdaten.iban);
                expect(restInstitutionStammdaten.oeffnungsstunden).toEqual(myInstitutionStammdaten.oeffnungsstunden);
                expect(restInstitutionStammdaten.oeffnungstage).toEqual(myInstitutionStammdaten.oeffnungstage);
                expect(restInstitutionStammdaten.gueltigAb).toEqual(DateUtil.momentToLocalDate(myInstitutionStammdaten.gueltigAb));
                expect(restInstitutionStammdaten.gueltigBis).toEqual(DateUtil.momentToLocalDate(myInstitutionStammdaten.gueltigBis));
                expect(restInstitutionStammdaten.betreuungsangebotTyp).toEqual(myInstitutionStammdaten.betreuungsangebotTyp);
                expect(restInstitutionStammdaten.institution.name).toEqual(myInstitutionStammdaten.institution.name);

                let transformedInstitutionStammdaten = ebeguRestUtil.parseInstitutionStammdaten(new TSInstitutionStammdaten(), restInstitutionStammdaten);
                expect(transformedInstitutionStammdaten.gueltigAb).toBeDefined();

                // Dieses hack wird gebraucht weil um 2 Moment zu vergleichen kann man nicht einfach equal() benutzen sondern isSame
                expect(myInstitutionStammdaten.gueltigAb.isSame(transformedInstitutionStammdaten.gueltigAb)).toBe(true);
                expect(myInstitutionStammdaten.gueltigBis.isSame(transformedInstitutionStammdaten.gueltigBis)).toBe(true);
                myInstitutionStammdaten.gueltigAb = transformedInstitutionStammdaten.gueltigAb;
                myInstitutionStammdaten.gueltigBis = transformedInstitutionStammdaten.gueltigBis;
                expect(transformedInstitutionStammdaten).toEqual(myInstitutionStammdaten);
            });
        });
    });

    function setAbstractFieldsUndefined(abstractEntity: TSAbstractEntity) {
        abstractEntity.id = undefined;
        abstractEntity.timestampErstellt = undefined;
        abstractEntity.timestampMutiert = undefined;
    }

    function createInstitution(): TSInstitution {
        let traegerschaft = new TSTraegerschaft('myTraegerschaft');
        setAbstractFieldsUndefined(traegerschaft);
        let mandant = new TSMandant('myMandant');
        setAbstractFieldsUndefined(mandant);
        let myInstitution = new TSInstitution('myInstitution', traegerschaft, mandant);
        setAbstractFieldsUndefined(myInstitution);
        return myInstitution;
    }

});
