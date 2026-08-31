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
import {DvDatePickerXAngularjswrapperComponent} from '../../../app/shared/component/dv-date-picker/dv-date-picker-x.angularjswrapper.component';
import {SharedModule} from '../../../app/shared/shared.module';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSKind} from '../../../models/entity/TSKind';
import {HybridFormBridgeService} from '../../../utils/hybrid-form-bridge/hybrid-form-bridge.service';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {PERMISSIONS_KIND} from '../../../models/permissions/PermissionsKind';

@Component({
    selector: 'kind-gueltigkeit-terminiert',
    imports: [
        CommonModule,
        SharedModule,
        DvDatePickerXAngularjswrapperComponent
    ],
    templateUrl: './kind-gueltigkeit-terminiert.component.html',
    styleUrl: './kind-gueltigkeit-terminiert.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class KindGueltigkeitTerminiertComponent
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
