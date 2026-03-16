import {inject, Injectable} from '@angular/core';
import {MatDatepickerIntl} from '@angular/material/datepicker';
import {TranslateService} from '@ngx-translate/core';

@Injectable()
export class DvDatePickerI18nOverwrite extends MatDatepickerIntl {
    translate = inject(TranslateService);

    nextMonthLabel = this.translate.instant('DATEPICKER_NEXT_MONTH_LABEL');
    prevMonthLabel = this.translate.instant('DATEPICKER_PREV_MONTH_LABEL');

    nextMultiYearLabel = this.translate.instant(
        'DATEPICKER_NEXT_MULTIYEAR_LABEL'
    );
    prevMultiYearLabel = this.translate.instant(
        'DATEPICKER_PREV_MULTIYEAR_LABEL'
    );
}
