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
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';
import {AuthServiceRS} from '../../../../../../../src/authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../../../../../src/gesuch/service/gesuchModelManager';
import {TSKind} from '@kibon/kind/model/entity';

@Component({
    selector: 'lib-betreuung-ui-kind-gueltigkeit-terminiert',
    imports: [SharedModule],
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
