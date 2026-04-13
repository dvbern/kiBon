import {TSDateRange} from '../../../../../models/entity/TSDateRange';
import {TSGesuchsperiode} from '../../../../../models/entity/TSGesuchsperiode';
import {TSBetreuungspensum} from '../../../../../models/TSBetreuungspensum';
import {Moment} from 'moment';

export abstract class GPPensumFiller {
    // we assume that the input is sorted and has no overlapping date ranges
    static fillGPGapsWithZeroPensen(
        betreuungspensen: TSBetreuungspensum[],
        gesuchsperiode: TSGesuchsperiode
    ) {
        if (betreuungspensen.length === 0) {
            return [
                this.createZeroPensum(
                    gesuchsperiode.gueltigkeit.gueltigAb,
                    gesuchsperiode.gueltigkeit.gueltigBis
                )
            ];
        }
        const filled = [];

        // fill perioden start to first pensum if necessary
        if (
            this.isFirstPensumStartingAfterGPStart(
                betreuungspensen,
                gesuchsperiode
            )
        ) {
            filled.push(
                this.createZeroPensum(
                    gesuchsperiode.gueltigkeit.gueltigAb,
                    betreuungspensen[0].gueltigkeit.gueltigAb
                )
            );
        }

        for (let i = 0; i < betreuungspensen.length; i++) {
            const current = betreuungspensen[i];
            const next = betreuungspensen[i + 1];

            filled.push(current);
            if (next !== undefined && !this.areConsecutive(current, next)) {
                const filler = this.createZeroPensum(
                    current.gueltigkeit.gueltigBis,
                    next.gueltigkeit.gueltigAb
                );
                filled.push(filler);
            }
        }

        if (
            this.isLastPensumEndingBeforeGPEnd(betreuungspensen, gesuchsperiode)
        ) {
            filled.push(
                this.createZeroPensum(
                    betreuungspensen[betreuungspensen.length - 1].gueltigkeit
                        .gueltigBis,
                    gesuchsperiode.gueltigkeit.gueltigBis
                )
            );
        }

        return filled;
    }

    private static isFirstPensumStartingAfterGPStart(
        betreuungspensen: TSBetreuungspensum[],
        gesuchsperiode: TSGesuchsperiode
    ) {
        return betreuungspensen[0].gueltigkeit.gueltigAb.isAfter(
            gesuchsperiode.gueltigkeit.gueltigAb
        );
    }

    private static createZeroPensum(previousEnd: Moment, nextStart: Moment) {
        const betreuungspensum = new TSBetreuungspensum();
        betreuungspensum.gueltigkeit = new TSDateRange(
            previousEnd.clone().add(1, 'day'),
            nextStart.clone().subtract(1, 'day')
        );
        betreuungspensum.pensum = 0;
        betreuungspensum.monatlicheBetreuungskosten = 0;
        betreuungspensum.tarifProHauptmahlzeit = 0;
        betreuungspensum.monatlicheHauptmahlzeiten = 0;
        betreuungspensum.betreuteTage = 0;

        return betreuungspensum;
    }

    private static areConsecutive(
        current: TSBetreuungspensum,
        next: TSBetreuungspensum
    ) {
        const currentEnd = current.gueltigkeit.gueltigBis;
        const nextStart = next.gueltigkeit.gueltigAb;

        return currentEnd.clone().add(1, 'day').isSame(nextStart);
    }

    private static isLastPensumEndingBeforeGPEnd(
        betreuungspensen: TSBetreuungspensum[],
        gesuchsperiode: TSGesuchsperiode
    ) {
        const last = betreuungspensen[betreuungspensen.length - 1];
        return last.gueltigkeit.gueltigBis
            .clone()
            .startOf('day')
            .isBefore(
                gesuchsperiode.gueltigkeit.gueltigBis.clone().startOf('day')
            );
    }
}
