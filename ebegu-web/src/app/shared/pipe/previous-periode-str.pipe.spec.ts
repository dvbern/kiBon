import moment from 'moment';
import {TSDateRange} from '../../../models/entity/TSDateRange';
import {TSGesuchsperiode} from '../../../models/entity/TSGesuchsperiode';
import {PreviousPeriodeStrPipe} from './previous-periode-str.pipe';

describe('PreviousPeriodeStrPipe', () => {
    it('create an instance', () => {
        const pipe = new PreviousPeriodeStrPipe();
        expect(pipe).toBeTruthy();
    });

    it('should return "2018/19"', () => {
        const periode = new TSGesuchsperiode();
        periode.gueltigkeit = new TSDateRange(
            moment('2019-01-01'),
            moment('2020-01-01')
        );
        const pipe = new PreviousPeriodeStrPipe();
        expect(pipe.transform(periode)).toBe('2018/19');
    });
});
