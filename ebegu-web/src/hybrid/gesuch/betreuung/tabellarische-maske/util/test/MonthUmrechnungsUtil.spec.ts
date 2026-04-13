/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import moment from 'moment';
import {TSDateRange} from '../../../../../../models/entity/TSDateRange';
import {TSGesuchsperiode} from '../../../../../../models/entity/TSGesuchsperiode';
import {TSGesuchsperiodeStatus} from '../../../../../../models/enums/TSGesuchsperiodeStatus';
import {TSBetreuung} from '../../../../../../models/TSBetreuung';
import {TSBetreuungspensum} from '../../../../../../models/TSBetreuungspensum';
import {TSBetreuungspensumContainer} from '../../../../../../models/TSBetreuungspensumContainer';
import {MonthUmrechnungsUtil} from '../MonthUmrechnungsUtil';
import {
    createBetreuungWithPensen,
    createBetreuungspensumContainer,
    TEST_DATES
} from './TestUtils';

describe('MonthUmrechnungsUtil', () => {
    describe('kitaBetreuungToMonthly', () => {
        const gpStart = TEST_DATES.AUG_FIRST;
        const gpEnd = TEST_DATES.JULY_LAST;
        const gp = new TSGesuchsperiode(
            TSGesuchsperiodeStatus.AKTIV,
            new TSDateRange(gpStart, gpEnd)
        );
        it('should set gueltigBis to the right date when splitted', () => {
            const betreuungspensumFirstMonatHalf = new TSBetreuungspensum();
            betreuungspensumFirstMonatHalf.gueltigkeit = new TSDateRange(
                TEST_DATES.OCT_FIRST,
                moment('2024-10-17')
            );
            betreuungspensumFirstMonatHalf.pensum = 80;
            betreuungspensumFirstMonatHalf.monatlicheBetreuungskosten = 2000;

            const betreuungspensumSecondMonatHalf = new TSBetreuungspensum();
            betreuungspensumSecondMonatHalf.gueltigkeit = new TSDateRange(
                moment('2024-10-25'),
                TEST_DATES.OCT_LAST
            );
            betreuungspensumSecondMonatHalf.pensum = 50;
            betreuungspensumSecondMonatHalf.monatlicheBetreuungskosten = 1500;

            const betreuung = new TSBetreuung();
            betreuung.gesuchsperiode = gp;
            betreuung.betreuungspensumContainers = [
                new TSBetreuungspensumContainer(
                    undefined,
                    betreuungspensumFirstMonatHalf
                ),
                new TSBetreuungspensumContainer(
                    undefined,
                    betreuungspensumSecondMonatHalf
                )
            ];

            const gpData = {firstYear: 2024, secondYear: 2025}; // mock year data
            const result = MonthUmrechnungsUtil.toMonthlyBetreuungspensen(
                betreuung,
                gpData
            );

            expect(result[9].pensum).toEqual(55.16);
        });

        describe('pensen outside periode', () => {
            it('should have 0 for pensum in same month but ending before periode start', () => {
                const container = createBetreuungspensumContainer(
                    moment('2023-08-15'),
                    moment('2023-08-31')
                );
                container.betreuungspensumJA.pensum = 80;
                container.betreuungspensumJA.monatlicheBetreuungskosten = 2000;
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[7].pensum).toEqual(0);
                expect(transformed[7].monatlicheBetreuungskosten).toEqual(0);
            });

            it('should have 0 for pensum in same month but starting after periode end', () => {
                const container = createBetreuungspensumContainer(
                    moment('2025-08-10'),
                    moment('2025-08-31')
                );
                container.betreuungspensumJA.pensum = 80;
                container.betreuungspensumJA.monatlicheBetreuungskosten = 2000;
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[7].pensum).toEqual(0);
                expect(transformed[7].monatlicheBetreuungskosten).toEqual(0);
            });
        });

        describe('untermonatige pensen', () => {
            describe('first pensum starting after first month', () => {
                it('should create first month with gueltigkeit of first month when pensum is starting after first month', () => {
                    const container = createBetreuungspensumContainer(
                        moment('2024-09-15'),
                        TEST_DATES.JULY_LAST
                    );
                    const betreuung = createBetreuungWithPensen(gp, [
                        container
                    ]);
                    const transformed =
                        MonthUmrechnungsUtil.toMonthlyBetreuungspensen(
                            betreuung,
                            {
                                firstYear: 2024,
                                secondYear: 2025
                            }
                        );
                    expect(
                        transformed[7].gueltigkeit.gueltigAb.toDate()
                    ).toEqual(gpStart.toDate());
                    expect(
                        transformed[7].gueltigkeit.gueltigBis.toDate()
                    ).toEqual(TEST_DATES.AUG_LAST.toDate());
                });
            });

            it('should create full september abschnitt gueltigkeiten when two untermonatige august pensen are provided', () => {
                const containerTo15 = createBetreuungspensumContainer(
                    TEST_DATES.AUG_FIRST,
                    moment('2024-08-15')
                );
                const containerTo30 = createBetreuungspensumContainer(
                    TEST_DATES.AUG_FIRST,
                    moment('2024-08-30')
                );
                const betreuung = createBetreuungWithPensen(gp, [
                    containerTo15,
                    containerTo30
                ]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[7].gueltigkeit.gueltigAb.toDate()).toEqual(
                    gpStart.toDate()
                );
                expect(transformed[7].gueltigkeit.gueltigBis.toDate()).toEqual(
                    TEST_DATES.AUG_LAST.toDate()
                );

                expect(transformed[8].gueltigkeit.gueltigAb.toDate()).toEqual(
                    TEST_DATES.SEP_FIRST.toDate()
                );
                expect(transformed[8].gueltigkeit.gueltigBis.toDate()).toEqual(
                    TEST_DATES.SEP_LAST.toDate()
                );
            });
        });

        describe('Mittagstisch', () => {
            it('should have 0 hauptmahlzeit werte in October if full august and sep are provided', () => {
                const augCont = createBetreuungspensumContainer(
                    TEST_DATES.AUG_FIRST,
                    TEST_DATES.AUG_LAST
                );
                augCont.betreuungspensumJA.monatlicheHauptmahlzeiten = 10;
                augCont.betreuungspensumJA.tarifProHauptmahlzeit = 10;

                const sepCont = createBetreuungspensumContainer(
                    TEST_DATES.SEP_FIRST,
                    TEST_DATES.SEP_LAST
                );
                sepCont.betreuungspensumJA.monatlicheHauptmahlzeiten = 10;
                sepCont.betreuungspensumJA.tarifProHauptmahlzeit = 10;
                const betreuung = createBetreuungWithPensen(gp, [
                    augCont,
                    sepCont
                ]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[9].tarifProHauptmahlzeit).toEqual(0);
                expect(transformed[9].monatlicheHauptmahlzeiten).toEqual(0);
            });

            it('should have 30 tarif in Sep if one day with 30 tarif pensum is provided', () => {
                const augCont = createBetreuungspensumContainer(
                    TEST_DATES.AUG_FIRST,
                    TEST_DATES.SEP_FIRST
                );
                augCont.betreuungspensumJA.monatlicheHauptmahlzeiten = 10;
                augCont.betreuungspensumJA.tarifProHauptmahlzeit = 30;

                const betreuung = createBetreuungWithPensen(gp, [augCont]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[8].tarifProHauptmahlzeit).toEqual(30);
            });

            it('should have 20 tarif in Sep if half month has 30 tarif and other half has 10 tarif', () => {
                const firstHalf = createBetreuungspensumContainer(
                    TEST_DATES.SEP_FIRST,
                    TEST_DATES.SEP_FIFTEEN
                );
                firstHalf.betreuungspensumJA.monatlicheHauptmahlzeiten = 10;
                firstHalf.betreuungspensumJA.tarifProHauptmahlzeit = 30;
                const secondHalf = createBetreuungspensumContainer(
                    TEST_DATES.SEP_SIXTEEN,
                    TEST_DATES.SEP_LAST
                );
                secondHalf.betreuungspensumJA.monatlicheHauptmahlzeiten = 10;
                secondHalf.betreuungspensumJA.tarifProHauptmahlzeit = 10;

                const betreuung = createBetreuungWithPensen(gp, [
                    firstHalf,
                    secondHalf
                ]);
                const transformed =
                    MonthUmrechnungsUtil.toMonthlyBetreuungspensen(betreuung, {
                        firstYear: 2024,
                        secondYear: 2025
                    });

                expect(transformed[8].tarifProHauptmahlzeit).toEqual(20);
            });
        });
    });
});
