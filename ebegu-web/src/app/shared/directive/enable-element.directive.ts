import {Directive, Input, OnChanges, SimpleChanges} from '@angular/core';
import {NgControl} from '@angular/forms';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '@kibon/shared/model/enums';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Directive({
    selector: '[dvEnableElement]',
    standalone: false
})
export class EnableElementDirective implements OnChanges {
    @Input() public allowedRoles: ReadonlyArray<TSRole> =
        TSRoleUtil.getAllRoles();
    @Input() public enableExpression: boolean = true;

    public constructor(
        private readonly authService: AuthServiceRS,
        public ngControl: NgControl
    ) {}

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
