import {Component, inject, ChangeDetectionStrategy} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {STEKUpdateResult} from './STEKUpdateResult';

@Component({
    selector: 'dv-zpv-nr-success',
    templateUrl: './zpv-nr-success.component.html',
    styleUrls: ['./zpv-nr-success.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class ZpvNrSuccessComponent {
    private readonly uiRouterGlobals = inject(UIRouterGlobals);

    protected readonly STEKUpdateResult = STEKUpdateResult;

    public result: STEKUpdateResult =
        this.uiRouterGlobals.params.zpvUpdateResult;
}
