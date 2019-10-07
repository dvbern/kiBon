/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {getTSModulTagesschuleIntervallValues, TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import EbeguUtil from '../../../utils/EbeguUtil';

@Component({
    selector: 'dv-edit-modul-tagesschule',
    templateUrl: './edit-modul-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditModulTagesschuleComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    @Input() public modulTagesschuleGroup: TSModulTagesschuleGroup;
    @Output() public readonly callback = new EventEmitter<TSModulTagesschuleGroup>();

    public patternHoursAndMinutes: '[0-9]{1,2}:[0-9]{1,2}';

    public constructor(
    ) {
    }

    public ngOnInit(): void {
        this.modulTagesschuleGroup.initializeTempModule();
    }

    public getModulTagesschuleIntervallOptions(): Array<TSModulTagesschuleIntervall> {
        return getTSModulTagesschuleIntervallValues();
    }

    public apply(): void {
        this.modulTagesschuleGroup.applyTempModule();
        if (EbeguUtil.isNullOrUndefined(this.modulTagesschuleGroup.identifier)) {
            this.modulTagesschuleGroup.identifier = TSModulTagesschuleGroup.createIdentifier();
        }
        if (this.modulTagesschuleGroup.isValid()) {
            this.modulTagesschuleGroup.validated =  true;
            this.callback.emit(this.modulTagesschuleGroup);
        } else {
            this.ngOnInit();
        }
    }
}
