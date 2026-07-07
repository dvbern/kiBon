import {Component, inject} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {ZPVUpdateResult} from './ZPVUpdateResult';

@Component({
    selector: 'dv-zpv-nr-success',
    templateUrl: './zpv-nr-success.component.html',
    styleUrls: ['./zpv-nr-success.component.less'],
    standalone: false
})
export class ZpvNrSuccessComponent {
    private readonly uiRouterGlobals = inject(UIRouterGlobals);

    protected readonly ZPVUpdateResult = ZPVUpdateResult;

    public result: ZPVUpdateResult =
        this.uiRouterGlobals.params.zpvUpdateResult;
}
