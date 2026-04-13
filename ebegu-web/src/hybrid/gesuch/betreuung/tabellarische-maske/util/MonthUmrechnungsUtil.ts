import {TSBetreuungspensum} from '../../../../../models/TSBetreuungspensum';
import {TSBetreuung} from '../../../../../models/TSBetreuung';
import {MathUtil} from '../../../../../utils/math-util/MathUtil';
import {
    BetreuungspensumWithStunden,
    BetreuungspensumWithTage,
    MonthAbschnitte
} from '../types/types';
import {BetreuungspensumSplitter} from './BetreuungspensumSplitter';
import {BetreuungspensumMerger} from './BetreuungspensumMerger';
import {GPPensumFiller} from './GPPensumFiller';
import {BetreuungUtil} from './BetreuungUtil';

export abstract class MonthUmrechnungsUtil {
    static toMonthlyKitaBetreuungInTage(
        betreuung: TSBetreuung,
        gpData: {firstYear: number; secondYear: number},
        multiplier: number
    ) {
        const months = this.toMonthlyBetreuungspensen(betreuung, gpData);

        const monthsWithTage: BetreuungspensumWithTage[] = months.map(
            betreuungspensum =>
                Object.assign(betreuungspensum, {
                    pensumInTage: this.toPensumUnit(
                        betreuungspensum.pensum,
                        multiplier
                    )
                })
        );

        return this.pensenToMonthAbschnitte<BetreuungspensumWithTage>(
            monthsWithTage
        );
    }

    static toMonthlyMittagstischBetreuung(
        betreuung: TSBetreuung,
        gpData: {firstYear: number; secondYear: number}
    ) {
        const months = this.toMonthlyBetreuungspensen(betreuung, gpData);

        return this.pensenToMonthAbschnitte<TSBetreuungspensum>(months);
    }

    static toMonthlyTfoBetreuungInTage(
        betreuung: TSBetreuung,
        gpData: {firstYear: number; secondYear: number},
        multiplier: number
    ) {
        const months = this.toMonthlyBetreuungspensen(betreuung, gpData);

        const monthsWithStunden: BetreuungspensumWithStunden[] = months.map(
            betreuungspensum =>
                Object.assign(betreuungspensum, {
                    pensumInStunden: this.toPensumUnit(
                        betreuungspensum.pensum,
                        multiplier
                    )
                })
        );

        return this.pensenToMonthAbschnitte<BetreuungspensumWithStunden>(
            monthsWithStunden
        );
    }

    static toMonthlyBetreuungspensen(
        betreuung: TSBetreuung,
        gpData: {firstYear: number; secondYear: number}
    ): TSBetreuungspensum[] {
        const betreuungspensenGPSafe =
            BetreuungUtil.getBetreuungspensenGPSafe(betreuung);
        const fullGP = GPPensumFiller.fillGPGapsWithZeroPensen(
            betreuungspensenGPSafe,
            betreuung.gesuchsperiode
        );
        const split = fullGP.flatMap(betreuungspensum => {
            return BetreuungspensumSplitter.splitOnMonthEnds(betreuungspensum);
        });

        return BetreuungspensumMerger.mergeMonthly(split, gpData);
    }

    static toPensumUnit(pensumInMonth: number, multiplier: number): number {
        return MathUtil.multiplyFloatPrecisionSafe(
            pensumInMonth,
            multiplier,
            2
        );
    }

    /**
     * Returns the percentage value for a given pensum.
     * @param pensumInPensumUnit The pensum in hours per month.
     * @param maxPensumPerMonth This value is based to the setting: maximum pensum in hours per month. In most cases this value is
     * 205 hours.
     * @param decimals The maximum allowed decimal places for the result to have. If the actual result exceeds this number,
     * excessing decimal places will be rounded.
     * @return The pensum as a percentage value. E.g. if given pensum is 100 hours and maximum pensum per month is 200 hours,
     * then the result of this function is 100 / 200 = 0.5 -> 0.5 * 100 = 50, so 50 is returned.
     */
    static toPercentage(
        pensumInPensumUnit: number,
        maxPensumPerMonth: number,
        decimals: number
    ): number {
        const decimalsInt = Math.round(decimals); // no decimal places allowed here
        const pensumInPercentage =
            100 * (pensumInPensumUnit / maxPensumPerMonth);
        const roundingFactor = Math.pow(10, decimalsInt);
        // all numbers must be rounded to have no more than the given decimal places.
        const pensumRounded =
            Math.round(pensumInPercentage * roundingFactor) / roundingFactor;
        return pensumRounded;
    }

    private static pensenToMonthAbschnitte<T>(months: T[]) {
        return {
            AUGUST: months[7],
            SEPTEMBER: months[8],
            OCTOBER: months[9],
            NOVEMBER: months[10],
            DECEMBER: months[11],
            JANUARY: months[0],
            FEBRUARY: months[1],
            MARCH: months[2],
            APRIL: months[3],
            MAY: months[4],
            JUNE: months[5],
            JULY: months[6]
        } as MonthAbschnitte<T>;
    }
}
