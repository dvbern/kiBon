import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    ViewChild,
    inject
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

const LOG = LogFactory.createLog('HoehereBetraegeBeeintraechtigungComponent');

@Component({
    selector: 'dv-hoehere-betraege-beeintraechtigung',
    templateUrl: './hoehere-betraege-beeintraechtigung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class HoehereBetraegeBeeintraechtigungComponent
    implements AfterViewInit
{
    private readonly formExchangeService = inject(KinderabzugExchangeService);
    private readonly cd = inject(ChangeDetectorRef);

    @ViewChild(NgForm) public form!: NgForm;

    @Input()
    public kindContainer!: TSKindContainer;

    @Input()
    public readOnly = false;

    public ngAfterViewInit(): void {
        this.formExchangeService.addForm(this.form);
        this.formExchangeService.getFormValidationTriggered$().subscribe(
            () => this.cd.markForCheck(),
            error => LOG.error(error)
        );
    }

    public handleBeantragungChange(): void {
        if (
            this.kindContainer.kindJA
                .hoehereBeitraegeWegenBeeintraechtigungBeantragen === false
        ) {
            this.kindContainer.kindJA.hoehereBeitraegeUnterlagenDigital = null;
        }
    }
}
