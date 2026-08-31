/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    inject
} from '@angular/core';
import {StateService} from '@uirouter/core';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-pulldown-user-menu-button',
    templateUrl: './pulldown-user-menu-button.component.html',
    styleUrls: ['./pulldown-user-menu-button.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PulldownUserMenuButtonComponent implements OnChanges {
    private readonly stateService = inject(StateService);

    @Input()
    public allowedRoles: ReadonlyArray<TSRole>;

    @Input()
    public label: string;

    @Input()
    public uiSRef: string;

    @Output()
    public readonly buttonClick = new EventEmitter<MouseEvent>();

    public allRoles = TSRoleUtil.getAllRoles();
    public href: string;

    public ngOnChanges(): void {
        this.href = this.stateService.href(this.uiSRef);
    }
}
