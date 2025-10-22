import {TSBetreuungspensum} from '../../../../../../../src/models/TSBetreuungspensum';
import {TSDateRange} from '@kibon/shared/model/entity';
import moment from 'moment/moment';
import {MathUtil} from '@kibon/shared/util-fn/math-util';

export abstract class BetreuungspensumMerger {
    static mergeMonthly(
        betreuungspensen: TSBetreuungspensum[],
        gpData: {firstYear: number; secondYear: number}
    ): TSBetreuungspensum[] {
        const months: TSBetreuungspensum[] = [];

        // iterate over months
        for (let i = 0; i <= 11; i++) {
            const betreuungsPensenOfMonth = this.getBetreuungspensenOfMonth(
                betreuungspensen,
                i
            );
            if (betreuungsPensenOfMonth.length === 0) {
                const tsBetreuungspensum = new TSBetreuungspensum();
                tsBetreuungspensum.gueltigkeit = new TSDateRange(
                    moment(
                        `01-${i}-${i >= 7 ? gpData.firstYear : gpData.secondYear}`
                    )
                );
                months.push(tsBetreuungspensum);
            } else {
                const merged = betreuungsPensenOfMonth
                    .map(pensum => this.calculateMonthly(pensum))
                    .reduce((previous, current) => {
                        return this.addData(previous, current);
                    });
                months.push(merged);
            }
        }

        return months;
    }

    private static getBetreuungspensenOfMonth(
        betreuungspensen: TSBetreuungspensum[],
        monthIndex: number
    ) {
        return betreuungspensen.filter(
            betreuungspensum =>
                betreuungspensum.gueltigkeit.gueltigAb.month() === monthIndex
        );
    }

    // Die Methode geht davon aus, dass die Pensen innerhalb eines Monats liegen
    static calculateMonthly(pensum: TSBetreuungspensum): TSBetreuungspensum {
        const monatsAnteil = this.getMonatsAnteil(pensum.gueltigkeit);
        pensum.pensum = monatsAnteil * pensum.pensum;
        pensum.monatlicheBetreuungskosten =
            monatsAnteil * pensum.monatlicheBetreuungskosten;
        pensum.monatlicheHauptmahlzeiten =
            monatsAnteil * pensum.monatlicheHauptmahlzeiten;
        pensum.betreuteTage = monatsAnteil * pensum.betreuteTage;

        pensum.gueltigkeit.gueltigAb.startOf('month').startOf('day');
        pensum.gueltigkeit.gueltigBis?.endOf('month').startOf('day');

        return pensum;
    }

    static addData(
        base: TSBetreuungspensum,
        toAdd: TSBetreuungspensum
    ): TSBetreuungspensum {
        base.pensum = MathUtil.addFloatPrecisionSafe(
            base.pensum,
            toAdd.pensum,
            2
        );
        base.betreuteTage = MathUtil.addFloatPrecisionSafe(
            base.betreuteTage,
            toAdd.betreuteTage,
            2
        );
        base.monatlicheBetreuungskosten = MathUtil.addFloatPrecisionSafe(
            toAdd.monatlicheBetreuungskosten,
            base.monatlicheBetreuungskosten,
            2
        );

        const addedMahlzeiten = this.addMahlzeiten(base, toAdd);

        base.monatlicheHauptmahlzeiten = addedMahlzeiten.anzahlMahlzeiten;
        base.tarifProHauptmahlzeit = addedMahlzeiten.kostenProMahlzeit;

        return base;
    }

    static getMonatsAnteil(gueltigkeit: TSDateRange): number {
        const daysInMonth = gueltigkeit.gueltigAb.daysInMonth();
        // add 1 to include start date
        const daysInPensum =
            Math.abs(
                gueltigkeit.gueltigAb.diff(gueltigkeit.gueltigBis, 'days')
            ) + 1;

        return daysInPensum / daysInMonth;
    }

    private static addMahlzeiten(
        base: TSBetreuungspensum,
        toAdd: TSBetreuungspensum
    ) {
        const costOfBase =
            base.monatlicheHauptmahlzeiten * base.tarifProHauptmahlzeit;

        const costOfToAdd =
            toAdd.monatlicheHauptmahlzeiten * toAdd.tarifProHauptmahlzeit;

        const costTotal = costOfToAdd + costOfBase;

        const anzahlMahlzeiten =
            base.monatlicheHauptmahlzeiten + toAdd.monatlicheHauptmahlzeiten;

        return {
            anzahlMahlzeiten: MathUtil.toFloatPrecision(anzahlMahlzeiten, 2),
            kostenProMahlzeit: MathUtil.divideFloatPrecisionSafe(
                costTotal,
                anzahlMahlzeiten
            )
        };
    }
}
