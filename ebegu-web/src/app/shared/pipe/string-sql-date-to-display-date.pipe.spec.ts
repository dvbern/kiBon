import {StringSqlDateToDisplayDatePipe} from './string-sql-date-to-display-date.pipe';

describe('StringSqlDateToDisplayDatePipe', () => {
    it('create an instance', () => {
        const pipe = new StringSqlDateToDisplayDatePipe();
        expect(pipe).toBeTruthy();
    });
});
