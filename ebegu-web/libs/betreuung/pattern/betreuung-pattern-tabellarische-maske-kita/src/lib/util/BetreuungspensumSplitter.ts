import {TSBetreuungspensum} from '../../../../../../../src/models/TSBetreuungspensum';
import moment from 'moment/moment';
import {TSDateRange} from '@kibon/shared/model/entity';

export abstract class BetreuungspensumSplitter {
    static splitOnMonthEnds(betreuungspensum: TSBetreuungspensum) {
        const split: TSBetreuungspensum[] = [];
        let nextMonthEnd = this.getMonthEnd(
            betreuungspensum.gueltigkeit.gueltigAb
        );
        let toBeSplit: TSBetreuungspensum = betreuungspensum;

        while (this.needsToBeSplit(betreuungspensum, nextMonthEnd)) {
            const pensenAfterSplit = this.splitOn(toBeSplit, nextMonthEnd);
            split.push(pensenAfterSplit.beforeSplitDate);

            toBeSplit = pensenAfterSplit.afterSplitDate;
            nextMonthEnd = this.getMonthEnd(toBeSplit.gueltigkeit.gueltigAb);
        }

        // make sure we don't manipulate the original
        split.push(toBeSplit.deepCopyTo(new TSBetreuungspensum()));

        return split;
    }

    private static needsToBeSplit(
        betreuungspensum: TSBetreuungspensum,
        nextMonthEnd: moment.Moment
    ) {
        return (
            betreuungspensum.gueltigkeit.isInDateRange(nextMonthEnd) &&
            !nextMonthEnd.isSame(betreuungspensum.gueltigkeit.gueltigBis)
        );
    }

    private static getMonthEnd(gueltigAb: moment.Moment): moment.Moment {
        return gueltigAb.clone().endOf('month');
    }

    private static splitOn(
        toBeSplit: TSBetreuungspensum,
        splitDate: moment.Moment
    ): {
        beforeSplitDate: TSBetreuungspensum;
        afterSplitDate: TSBetreuungspensum;
    } {
        const beforeMonthEnd = toBeSplit.deepCopyTo(new TSBetreuungspensum());
        beforeMonthEnd.id = undefined;
        beforeMonthEnd.gueltigkeit = new TSDateRange(
            toBeSplit.gueltigkeit.gueltigAb.clone(),
            splitDate.clone()
        );

        const afterSplitDate = toBeSplit.deepCopyTo(new TSBetreuungspensum());
        afterSplitDate.id = undefined;
        afterSplitDate.gueltigkeit = new TSDateRange(
            splitDate.clone().add(1, 'day'),
            toBeSplit.gueltigkeit.gueltigBis?.endOf('day').clone()
        );

        return {
            beforeSplitDate: beforeMonthEnd,
            afterSplitDate
        };
    }
}
