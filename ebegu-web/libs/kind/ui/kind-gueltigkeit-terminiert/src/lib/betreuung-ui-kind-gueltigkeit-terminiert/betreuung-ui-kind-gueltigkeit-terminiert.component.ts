import {CommonModule} from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    inject,
    input,
    OnDestroy,
    viewChild
} from '@angular/core';

import {NgForm} from '@angular/forms';
import {PERMISSIONS_KIND} from '@kibon/kind/model/permissions';
import {HybridFormBridgeService} from '@kibon/shared/util/hybrid-form-bridge';
/* eslint-disable @nx/enforce-module-boundaries */
import {DvDatePickerXAngularjswrapperComponent} from '../../../../../../../src/app/shared/component/dv-date-picker/dv-date-picker-x.angularjswrapper.component';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';
import {AuthServiceRS} from '../../../../../../../src/authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../../../../../src/gesuch/service/gesuchModelManager';
/* eslint-enable @nx/enforce-module-boundaries */
import {TSKind} from '@kibon/kind/model/entity';

@Component({
    selector: 'lib-betreuung-ui-kind-gueltigkeit-terminiert',
    imports: [
        CommonModule,
        SharedModule,
        DvDatePickerXAngularjswrapperComponent
    ],
    templateUrl: './betreuung-ui-kind-gueltigkeit-terminiert.component.html',
    styleUrl: './betreuung-ui-kind-gueltigkeit-terminiert.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BetreuungUiKindGueltigkeitTerminiertComponent
    implements AfterViewInit, OnDestroy
{
    form = viewChild.required(NgForm);
    kind = input.required<TSKind>();

    bridgeService = inject(HybridFormBridgeService);
    gesuchModelManager = inject(GesuchModelManager);
    authService = inject(AuthServiceRS);

    ngAfterViewInit(): void {
        this.bridgeService.register(this.form());
    }

    ngOnDestroy(): void {
        this.bridgeService.unregister(this.form());
    }

    isReadonly(): boolean {
        return (
            this.gesuchModelManager.isGesuchReadonly() ||
            !this.authService.isOneOfRoles(
                PERMISSIONS_KIND.GUELTIGKEIT_TERMINIEREN_WRITE
            )
        );
    }

    resetData(): void {
        this.kind().gueltigkeitTerminiertPer = null;
        this.kind().gueltigkeitTerminiertKommentar = null;
    }
}
