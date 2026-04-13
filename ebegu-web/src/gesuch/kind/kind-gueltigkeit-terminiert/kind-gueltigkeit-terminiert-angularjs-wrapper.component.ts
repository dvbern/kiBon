import {Component, Input} from '@angular/core';
import {TSKind} from '../../../models/entity/TSKind';

import {KindGueltigkeitTerminiertComponent} from './kind-gueltigkeit-terminiert.component';

@Component({
    selector: 'kind-gueltigkeit-terminiert-angularjs-wrapper',
    imports: [KindGueltigkeitTerminiertComponent],
    template:
        '<kind-gueltigkeit-terminiert [kind]="kind"></kind-gueltigkeit-terminiert>'
})
export class KindGueltigkeitTerminiertAngularjsWrapperComponent {
    @Input({required: true})
    kind!: TSKind;
}
