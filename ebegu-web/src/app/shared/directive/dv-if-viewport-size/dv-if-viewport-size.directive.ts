import {
    BreakpointObserver,
    Breakpoints,
    BreakpointState
} from '@angular/cdk/layout';
import {
    ChangeDetectorRef,
    Directive,
    Input,
    OnDestroy,
    TemplateRef,
    ViewContainerRef,
    inject
} from '@angular/core';
import {Subscription} from 'rxjs';

type Size = 'screen' | 'mobile';

const config = {
    mobile: [Breakpoints.XSmall],
    screen: [
        Breakpoints.Small,
        Breakpoints.Medium,
        Breakpoints.Large,
        Breakpoints.XLarge
    ]
};

@Directive({
    selector: '[dvIfViewportSize]',
    standalone: false
})
export class DvIfViewportSizeDirective implements OnDestroy {
    private readonly observer = inject(BreakpointObserver);
    private readonly vcRef = inject(ViewContainerRef);
    private readonly templateRef = inject<TemplateRef<any>>(TemplateRef);
    private readonly cd = inject(ChangeDetectorRef);

    private subscription = new Subscription();

    @Input()
    public set dvIfViewportSize(value: Size) {
        this.subscription.unsubscribe();
        this.subscription = this.observer
            .observe(config[value])
            .subscribe(this.updateView);
    }

    public ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private readonly updateView = ({matches}: BreakpointState) => {
        if (matches && !this.vcRef.length) {
            this.vcRef.createEmbeddedView(this.templateRef);
        } else if (!matches && this.vcRef.length) {
            this.vcRef.clear();
        }
        this.cd.markForCheck();
    };
}
