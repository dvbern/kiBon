import {
    ChangeDetectionStrategy,
    Component,
    computed,
    input,
    model,
    Signal
} from '@angular/core';
import {FormsModule} from '@angular/forms';
import * as Moment from 'moment';
import {MomentUtil} from '@utils/moment';
import {SharedModule} from '../../../app/shared/shared.module';
import {
    ConfigurableEinstellung,
    EinstellungType,
    getEinstellungConfig,
    isEnumEinstellung
} from './EinstellungConfigurations';

@Component({
    selector: 'lib-dv-edit-einstellung',
    imports: [FormsModule, SharedModule],
    templateUrl: './admin-edit-einstellung.component.html',
    styleUrl: './admin-edit-einstellung.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditEinstellungComponent {
    einstellung = model.required<ConfigurableEinstellung>();
    readOnly = input.required<boolean>();

    enumValues = computed(() => {
        const item = this.einstellung();
        if (!isEnumEinstellung(item.key)) {
            return [];
        }
        return Object.values(getEinstellungConfig(item.key).type);
    });
    einstellungAsDate: Signal<Moment.Moment> = computed(() =>
        MomentUtil.localDateToMoment(this.einstellung().value)
    );
    enumAsArray = computed(() =>
        this.einstellung()
            .value.split(',')
            .map(item => item.trim())
    );
    type = computed(() => getEinstellungConfig(this.einstellung().key).type);
    options = computed(
        () => getEinstellungConfig(this.einstellung().key).options
    );

    protected readonly String = String;
    protected readonly Boolean = Boolean;
    protected readonly Date = Date;
    protected readonly Number = Number;
    protected readonly isEnumEinstellung = isEnumEinstellung;

    public changeDate(date: Moment.Moment): void {
        this.update({value: MomentUtil.momentToLocalDate(date)});
    }

    public throwNotImplementedError(einstellungType: EinstellungType): string {
        throw new Error(
            `Annotation type ${einstellungType.constructor.name} is not implemented`
        );
    }

    public blockInvalidChars(event: any) {
        const invalidChars = ['-', '+', 'e'];
        if (invalidChars.includes(event.key)) {
            event.preventDefault();
        }
    }

    public update(change: {value: string}): void {
        this.einstellung.update(val => ({
            ...val,
            value: change.value
        }));
    }

    public updateMultiValue($event: string[]): void {
        this.einstellung.update(val => ({
            ...val,
            value: $event.join(',')
        }));
    }
}
