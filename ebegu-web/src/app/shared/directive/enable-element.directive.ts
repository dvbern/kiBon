import {
    Directive,
    Input,
    OnChanges,
    SimpleChanges,
    inject
} from '@angular/core';
import {NgControl} from '@angular/forms';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Directive({
    selector: '[dvEnableElement]',
    standalone: false
})
export class EnableElementDirective implements OnChanges {
    private readonly authService = inject(AuthServiceRS);
    ngControl = inject(NgControl);

    @Input() public allowedRoles: ReadonlyArray<TSRole> =
        TSRoleUtil.getAllRoles();
    @Input() public enableExpression: boolean = true;

    public ngOnChanges(changes: SimpleChanges): void {
        if (
            !this.authService.isOneOfRoles(this.allowedRoles) ||
            !this.enableExpression
        ) {
            if (changes.enableExpression && this.ngControl?.control) {
                const action = this.enableExpression ? 'enable' : 'disable';
                this.ngControl?.control[action]();
            }
        }
    }
}
