import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TSKind} from '@kibon/kind/model/entity';
import {BetreuungUiKindGueltigkeitTerminiertComponent} from './betreuung-ui-kind-gueltigkeit-terminiert.component';

@Component({
    selector: 'lib-betreuung-ui-kind-gueltigkeit-terminiert-angularjs-wrapper',
    imports: [CommonModule, BetreuungUiKindGueltigkeitTerminiertComponent],
    template:
        '<lib-betreuung-ui-kind-gueltigkeit-terminiert [kind]="kind"></lib-betreuung-ui-kind-gueltigkeit-terminiert>'
})
export class BetreuungUiKindGueltigkeitTerminiertAngularjsWrapperComponent {
    @Input({required: true})
    kind!: TSKind;
}
