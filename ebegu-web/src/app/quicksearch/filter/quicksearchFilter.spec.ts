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

import * as moment from 'moment';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import TSAntragDTO from '../../../models/TSAntragDTO';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import EbeguUtil from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {quicksearchFilter} from './quicksearchFilter';
import IProvideService = angular.auto.IProvideService;

describe('filter', () => {

    let quicksearchArray: Array<TSAntragDTO>;
    let antrag1: TSAntragDTO;
    let antrag2: TSAntragDTO;
    let antrag3: TSAntragDTO;
    let gesuchsperiode: TSGesuchsperiode;

    const abStr = '31.08.2016';
    let filter: any;

    beforeEach(angular.mock.module('pascalprecht.translate'));

    beforeEach(angular.mock.module(($provide: IProvideService) => {
        $provide.value('CONSTANTS', CONSTANTS);
        $provide.service('EbeguUtil', EbeguUtil);
        $provide.service('quicksearchFilterFilter', quicksearchFilter);
    }));

    beforeEach(angular.mock.inject($injector => {
        filter = $injector.get('$filter')('filter');

        const ab = moment(abStr, 'DD.MM.YYYY');
        const bis = moment('01.07.2017', 'DD.MM.YYYY');
        gesuchsperiode = new TSGesuchsperiode(TSGesuchsperiodeStatus.AKTIV, new TSDateRange(ab, bis));

        quicksearchArray = [];
        antrag1 = new TSAntragDTO('id1',
                                  1,
                                  'Hernandez',
                                  TSAntragTyp.ERSTGESUCH,
                                  ab,
                                  ab,
                                  undefined,
                                  [TSBetreuungsangebotTyp.KITA],
                                  ['Instit1'],
                                  'Juan Arbolado',
                                  'Juan Arbolado',
                                  TSAntragStatus.IN_BEARBEITUNG_JA,
                                  gesuchsperiode.gueltigkeit.gueltigAb,
                                  gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag1);

        antrag2 = new TSAntragDTO('id2',
                                  2,
                                  'Perez',
                                  TSAntragTyp.ERSTGESUCH,
                                  ab,
                                  ab,
                                  undefined,
                                  [TSBetreuungsangebotTyp.TAGESFAMILIEN],
                                  ['Instit2'],
                                  'Antonio Jimenez',
                                  'Antonio Jimenez',
                                  TSAntragStatus.IN_BEARBEITUNG_JA,
                                  gesuchsperiode.gueltigkeit.gueltigAb,
                                  gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag2);

        antrag3 = new TSAntragDTO('id3',
                                  3,
                                  'Dominguez',
                                  TSAntragTyp.MUTATION,
                                  ab,
                                  ab,
                                  undefined,
                                  [TSBetreuungsangebotTyp.KITA, TSBetreuungsangebotTyp.TAGESFAMILIEN],
                                  ['Instit1', 'Instit2'],
                                  'Eustaquio Romualdo',
                                  'Eustaquio Romualdo',
                                  TSAntragStatus.IN_BEARBEITUNG_JA,
                                  gesuchsperiode.gueltigkeit.gueltigAb,
                                  gesuchsperiode.gueltigkeit.gueltigBis);
        quicksearchArray.push(antrag3);

    }));

    describe('API usage', () => {
        it('should return an array with only the element with the given Fallnummer', () => {
            expect(filter(quicksearchArray, {fallNummer: '1'})).toEqual([antrag1]);
            expect(filter(quicksearchArray, {fallNummer: '01'})).toEqual([antrag1]);
            expect(filter(quicksearchArray, {fallNummer: '0002'})).toEqual([antrag2]);
            // the fallnummer doesn't exist
            expect(filter(quicksearchArray, {fallNummer: '4'})).toEqual([]);
        });
        it('should return an array with only the elements with the given Familienname or containing the given string',
           () => {
               expect(filter(quicksearchArray, {familienName: 'Hernandez'}))
                   .toEqual([antrag1]);
               expect(filter(quicksearchArray, {familienName: 'ez'}))
                   .toEqual([antrag1, antrag2, antrag3]);
               // empty string returns all elements
               expect(filter(quicksearchArray, {familienName: ''}))
                   .toEqual([antrag1, antrag2, antrag3]);
               // no familienname with this pattern
               expect(filter(quicksearchArray, {familienName: 'rrr'}))
                   .toEqual([]);
           });
        it('should return an array with only the elements of the given antragTyp', () => {
            expect(filter(quicksearchArray, {antragTyp: TSAntragTyp.ERSTGESUCH}))
                .toEqual([antrag1, antrag2]);
            expect(filter(quicksearchArray, {antragTyp: TSAntragTyp.MUTATION}))
                .toEqual([antrag3]);
            // empty string returns all elements
            expect(filter(quicksearchArray, {antragTyp: ''}))
                .toEqual([antrag1, antrag2, antrag3]);
            expect(filter(quicksearchArray, {antragTyp: 'error'}))
                .toEqual([]);
        });
        it('should return an array with only the elements of the given gesuchsperiodeGueltigAb', () => {
            expect(filter(quicksearchArray, {gesuchsperiodeGueltigAb: abStr}))
                .toEqual([antrag1, antrag2, antrag3]);
            expect(filter(quicksearchArray, {gesuchsperiodeGueltigAb: ''}))
                .toEqual([antrag1, antrag2, antrag3]);
            expect(filter(quicksearchArray, {gesuchsperiodeGueltigAb: '2020/2021'}))
                .toEqual([]);
        });
        it('should return an array with only the elements of the given eingangsdatum', () => {
            expect(filter(quicksearchArray, {eingangsdatum: abStr}))
                .toEqual([antrag1, antrag2, antrag3]);
            expect(filter(quicksearchArray, {eingangsdatum: ''}))
                .toEqual([antrag1, antrag2, antrag3]);
            expect(filter(quicksearchArray, {eingangsdatum: '31.08.2017'}))
                .toEqual([]);
        });
        it('should return an array with only the elements of the given angebotstyp', () => {
            expect(filter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.KITA}))
                .toEqual([antrag1, antrag3]);
            expect(filter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.TAGESFAMILIEN}))
                .toEqual([antrag2, antrag3]);
            expect(filter(quicksearchArray, {angebote: TSBetreuungsangebotTyp.TAGESSCHULE}))
                .toEqual([]);
            expect(filter(quicksearchArray, {angebote: ''}))
                .toEqual([antrag1, antrag2, antrag3]);
        });
        it('should return an array with only the elements of the given institutionen', () => {
            expect(filter(quicksearchArray, {institutionen: 'Instit1'}))
                .toEqual([antrag1, antrag3]);
            expect(filter(quicksearchArray, {institutionen: 'Instit2'}))
                .toEqual([antrag2, antrag3]);
            expect(filter(quicksearchArray, {institutionen: ''}))
                .toEqual([antrag1, antrag2, antrag3]);
        });
        it('should return the elements containing all given params, for a multiple filtering', () => {
            expect(filter(quicksearchArray, {familienName: 'Hernandez', institutionen: 'Instit1'}))
                .toEqual([antrag1]);
        });
    });

});
