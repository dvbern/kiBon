import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';
import {CONSTANTS} from '@models/constants';
@Pipe({
    name: 'ebeguDate',
    standalone: false
})
export class EbeguDatePipe implements PipeTransform {
    public transform(date: moment.Moment): string {
        if (date?.isValid()) {
            return date.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }
}
