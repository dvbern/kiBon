import {Directive, input} from '@angular/core';
import {
    AbstractControl,
    NG_VALIDATORS,
    ValidationErrors,
    Validator
} from '@angular/forms';
import {CONSTANTS} from '@models/constants';
import moment from 'moment';
import {TSGesuchsperiode} from '../../../models/entity/TSGesuchsperiode';

@Directive({
    selector: '[dateInPeriode]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: DateInPeriodeValidatorDirective,
            multi: true
        }
    ]
})
export class DateInPeriodeValidatorDirective implements Validator {
    gesuchsperiode = input<TSGesuchsperiode>();

    private getInputAsMoment(viewValue: any): moment.Moment {
        const value = this.stringToMoment(viewValue);

        return moment(value, CONSTANTS.ALLOWED_FORMATS_DATEPICKER, true);
    }

    private stringToMoment(date: string): any {
        if (
            moment(date, CONSTANTS.ALLOWED_FORMATS_DATEPICKER, true).isValid()
        ) {
            return moment(date, CONSTANTS.ALLOWED_FORMATS_DATEPICKER, true);
        }
        return null;
    }

    validate(control: AbstractControl): ValidationErrors | null {
        if (
            this.gesuchsperiode()?.gueltigkeit.gueltigAb &&
            this.gesuchsperiode()?.gueltigkeit.gueltigBis &&
            control.value
        ) {
            const maxDateAsMoment = moment(
                this.gesuchsperiode().gueltigkeit.gueltigBis,
                CONSTANTS.ALLOWED_FORMATS_DATEPICKER,
                true
            );
            const minDateAsMoment = moment(
                this.gesuchsperiode().gueltigkeit.gueltigAb,
                CONSTANTS.ALLOWED_FORMATS_DATEPICKER,
                true
            );
            if (maxDateAsMoment.isValid() && minDateAsMoment.isValid()) {
                const inputAsMoment = this.getInputAsMoment(control.value);
                if (
                    inputAsMoment &&
                    (inputAsMoment.isAfter(maxDateAsMoment) ||
                        inputAsMoment.isBefore(minDateAsMoment))
                ) {
                    return {
                        dvGesuchsperiodeIsInDateRange: {
                            startGp: this.gesuchsperiode()
                                .gueltigkeit.gueltigAb.toDate()
                                .getFullYear(),
                            endGp: this.gesuchsperiode()
                                .gueltigkeit.gueltigBis.toDate()
                                .getFullYear()
                        }
                    };
                }
            } else {
                throw new Error('min date and max date are Invalid');
            }
        }
        return null;
    }
}
