import {
    createBetreuungspensumContainer,
    createBetreuungWithPensen,
    TEST_DATES
} from './TestUtils';
import moment from 'moment/moment';
import {TSDateRange, TSGesuchsperiode} from '@kibon/shared/model/entity';
import {TSGesuchsperiodeStatus} from '@kibon/shared/model/enums';
import {BetreuungUtil} from '../BetreuungUtil';

describe('BetreuungUtil', () => {
    const gpStart = TEST_DATES.GP_START;
    const gpEnd = TEST_DATES.GP_END;
    const gp = new TSGesuchsperiode(
        TSGesuchsperiodeStatus.AKTIV,
        new TSDateRange(gpStart, gpEnd)
    );
    describe('betreuungspensum retrieval', () => {
        describe('pensen starting before periode', () => {
            it('should discard pensum ending before periode start', () => {
                const container = createBetreuungspensumContainer(
                    moment('2023-08-15'),
                    moment('2023-08-31')
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(0);
            });

            it('should discard pensum starting after periode end', () => {
                const container = createBetreuungspensumContainer(
                    moment('2025-08-15'),
                    moment('2025-08-31')
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(0);
            });

            it('should discard pensum starting after periode end without end date', () => {
                const container = createBetreuungspensumContainer(
                    moment('2025-08-15'),
                    undefined
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(0);
            });

            it('should not discard pensum starting before periode start without end date', () => {
                const container = createBetreuungspensumContainer(
                    moment('2023-08-15'),
                    undefined
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(1);
            });

            it('should not discard pensum starting before periode end without end date', () => {
                const container = createBetreuungspensumContainer(
                    moment('2024-07-15'),
                    undefined
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(1);
            });

            it('should not discard pensum starting before periode end ending after periode end', () => {
                const container = createBetreuungspensumContainer(
                    moment('2025-07-15'),
                    moment('2025-08-15')
                );
                const betreuung = createBetreuungWithPensen(gp, [container]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved.length).toBe(1);
            });
        });

        describe('sorting', () => {
            it('should sort sep pensum after aug pensum', () => {
                const augCont = createBetreuungspensumContainer(
                    TEST_DATES.AUG_FIRST,
                    TEST_DATES.AUG_LAST
                );
                const sepCont = createBetreuungspensumContainer(
                    TEST_DATES.SEP_FIRST,
                    TEST_DATES.SEP_LAST
                );

                const betreuung = createBetreuungWithPensen(gp, [
                    augCont,
                    sepCont
                ]);
                const retrieved =
                    BetreuungUtil.getBetreuungspensenGPSafe(betreuung);

                expect(retrieved[0].gueltigkeit.gueltigAb.toDate()).toEqual(
                    TEST_DATES.AUG_FIRST.toDate()
                );
                expect(retrieved[1].gueltigkeit.gueltigAb.toDate()).toEqual(
                    TEST_DATES.SEP_FIRST.toDate()
                );
            });
        });
    });
});
