import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';

@Pipe({
    name: 'stringSqlDateToDisplayDate',
    standalone: true
})
export class StringSqlDateToDisplayDatePipe implements PipeTransform {
    transform(value: string, format: string = 'DD.MM.YYYY'): string {
        return moment(value).format(format);
    }
}
