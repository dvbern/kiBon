import {TSDateRange} from '../../../../../models/entity/TSDateRange';
import {TSGesuchsperiode} from '../../../../../models/entity/TSGesuchsperiode';
import {TSBetreuung} from '../../../../../models/TSBetreuung';
import {TSBetreuungspensum} from '../../../../../models/TSBetreuungspensum';
import {Moment} from 'moment';

export class BetreuungUtil {
    static getBetreuungspensenGPSafe(betreuung: TSBetreuung) {
        return betreuung.betreuungspensumContainers
            .map(betreuungspensumContainer =>
                betreuungspensumContainer.betreuungspensumJA.deepCopyTo(
                    new TSBetreuungspensum()
                )
            )
            .filter(
                betreuungspensum =>
                    this.isInGP(
                        betreuungspensum.gueltigkeit.gueltigAb,
                        betreuung.gesuchsperiode
                    ) ||
                    this.isInGP(
                        betreuungspensum.gueltigkeit.gueltigBis,
                        betreuung.gesuchsperiode
                    ) ||
                    this.containsGP(
                        betreuungspensum.gueltigkeit,
                        betreuung.gesuchsperiode
                    )
            )
            .map(betreuungspensum =>
                this.limitToGP(betreuung.gesuchsperiode, betreuungspensum)
            )
            .sort((a, b) => {
                return a.gueltigkeit.gueltigAb.isBefore(b.gueltigkeit.gueltigAb)
                    ? -1
                    : 1;
            });
    }

    private static limitToGP(
        gp: TSGesuchsperiode,
        betreuungspensum: TSBetreuungspensum
    ) {
        const gpGueltigkeit = gp.gueltigkeit;
        const pensumGueltigkeit = betreuungspensum.gueltigkeit;

        if (
            !pensumGueltigkeit.gueltigBis ||
            pensumGueltigkeit.gueltigBis.isAfter(gpGueltigkeit.gueltigBis)
        ) {
            pensumGueltigkeit.gueltigBis = gpGueltigkeit.gueltigBis;
        }

        if (
            !pensumGueltigkeit.gueltigAb ||
            pensumGueltigkeit.gueltigAb.isBefore(gpGueltigkeit.gueltigAb)
        ) {
            pensumGueltigkeit.gueltigAb = gpGueltigkeit.gueltigAb;
        }

        return betreuungspensum;
    }

    private static isInGP(
        date: Moment | undefined,
        gesuchsperiode: TSGesuchsperiode
    ) {
        if (date === undefined) {
            return false;
        }
        return (
            date.isSameOrAfter(gesuchsperiode.gueltigkeit.gueltigAb) &&
            date.isSameOrBefore(gesuchsperiode.gueltigkeit.gueltigBis)
        );
    }

    private static containsGP(
        gueltigkeit: TSDateRange,
        gesuchsperiode: TSGesuchsperiode
    ) {
        return (
            gueltigkeit.gueltigAb.isBefore(
                gesuchsperiode.gueltigkeit.gueltigAb
            ) &&
            (gueltigkeit.gueltigBis === undefined ||
                gesuchsperiode.gueltigkeit.gueltigBis.isAfter(
                    gesuchsperiode.gueltigkeit.gueltigBis
                ))
        );
    }
}
