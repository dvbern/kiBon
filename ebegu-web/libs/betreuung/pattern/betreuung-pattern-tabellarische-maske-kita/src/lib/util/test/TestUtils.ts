import {Moment} from 'moment/moment';
import {TSBetreuungspensumContainer} from '../../../../../../../../src/models/TSBetreuungspensumContainer';
import {TSBetreuungspensum} from '../../../../../../../../src/models/TSBetreuungspensum';
import {TSDateRange, TSGesuchsperiode} from '@kibon/shared/model/entity';
import {TSBetreuung} from '../../../../../../../../src/models/TSBetreuung';
import moment from 'moment';

export function createBetreuungspensumContainer(
    gueltigAb: Moment,
    gueltigBis: Moment
): TSBetreuungspensumContainer {
    const betreuungspensum = new TSBetreuungspensum();
    betreuungspensum.gueltigkeit = new TSDateRange(gueltigAb, gueltigBis);
    return new TSBetreuungspensumContainer(undefined, betreuungspensum);
}

export function createBetreuungWithPensen(
    gp: TSGesuchsperiode,
    tsBetreuungspensumContainers: TSBetreuungspensumContainer[]
): TSBetreuung {
    const betreuung = new TSBetreuung();
    betreuung.gesuchsperiode = gp;
    betreuung.betreuungspensumContainers = tsBetreuungspensumContainers;
    return betreuung;
}

export const TEST_DATES = {
    GP_START: moment('2024-08-01'),
    GP_END: moment('2025-07-31'),
    AUG_FIRST: moment('2024-08-01'),
    AUG_LAST: moment('2024-08-31'),
    SEP_FIRST: moment('2024-09-01'),
    SEP_FIFTEEN: moment('2024-09-15'),
    SEP_SIXTEEN: moment('2024-09-16'),
    SEP_LAST: moment('2024-09-30'),
    OCT_FIRST: moment('2024-10-01'),
    OCT_LAST: moment('2024-10-31'),
    JULY_LAST: moment('2025-07-31')
};
