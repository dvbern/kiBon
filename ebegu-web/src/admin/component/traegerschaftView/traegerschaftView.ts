/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Component, Input, OnInit, ViewChild} from '@angular/core';
import './traegerschaftView.less';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig, MatSort, MatSortable, MatTableDataSource} from '@angular/material';
import {DvNgRemoveDialogComponent} from '../../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import ErrorService from '../../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../../core/service/traegerschaftRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import EbeguUtil from '../../../utils/EbeguUtil';
import AbstractAdminViewController from '../../abstractAdminView';

const style = require('./traegerschaftView.less');


@Component({
    selector: 'dv-traegerschaft-view',
    template: require('./traegerschaftView.html'),
})
export class TraegerschaftViewComponent extends AbstractAdminViewController implements OnInit {

    @Input() traegerschaften: TSTraegerschaft[];

    displayedColumns: string[] = ['name', 'remove'];
    traegerschaft: TSTraegerschaft = undefined;
    dataSource: MatTableDataSource<TSTraegerschaft>;

    @ViewChild(NgForm) form: NgForm;
    @ViewChild(MatSort) sort: MatSort;


    constructor(private readonly traegerschaftRS: TraegerschaftRS, private readonly errorService: ErrorService,
                private readonly dialog: MatDialog,
                authServiceRS: AuthServiceRS) {

        super(authServiceRS);
    }

    public ngOnInit(): void {
        this.dataSource = new MatTableDataSource(this.traegerschaften);
        this.sortTable();
    }

    /**
     * It sorts the table by default using the variable sort.
     */
    private sortTable() {
        this.sort.sort(<MatSortable>{
                id: 'name',
                start: 'asc'
            }
        );
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }

    removeTraegerschaft(traegerschaft: any): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = false; // dialog is canceled by clicking outside
        dialogConfig.autoFocus = true;
        dialogConfig.data = {
            title: 'LOESCHEN_DIALOG_TITLE',
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe((userAccepted) => {   //User confirmed removal
                if (userAccepted) {
                    this.traegerschaft = undefined;
                    this.traegerschaftRS.removeTraegerschaft(traegerschaft.id).then(() => {
                        const index = EbeguUtil.getIndexOfElementwithID(traegerschaft, this.traegerschaften);
                        if (index > -1) {
                            this.traegerschaften.splice(index, 1);
                            this.refreshTraegerschaftenList();
                        }
                    });
                }
            });
    }

    createTraegerschaft(): void {
        this.traegerschaft = new TSTraegerschaft();
        this.traegerschaft.active = true;
    }

    saveTraegerschaft(): void {
        if (this.form.valid) {
            this.errorService.clearAll();
            const newTraegerschaft: boolean = this.traegerschaft.isNew();
            this.traegerschaftRS.createTraegerschaft(this.traegerschaft).then((traegerschaft: TSTraegerschaft) => {
                if (newTraegerschaft) {
                    this.traegerschaften.push(traegerschaft);
                } else {
                    const index = EbeguUtil.getIndexOfElementwithID(traegerschaft, this.traegerschaften);
                    if (index > -1) {
                        this.traegerschaften[index] = traegerschaft;
                        EbeguUtil.handleSmarttablesUpdateBug(this.traegerschaften);
                    }
                }
                this.refreshTraegerschaftenList();
                this.traegerschaft = undefined;
            });
        }
    }

    /**
     * To refresh the traegerschaftenlist we need to refresh the MatTableDataSource with the new list of Traegerschaften.
     */
    private refreshTraegerschaftenList() {
        this.dataSource.data = this.traegerschaften;
    }

    cancelTraegerschaft(): void {
        this.traegerschaft = undefined;
    }

    setSelectedTraegerschaft(selected: TSTraegerschaft): void {
        this.traegerschaft = angular.copy(selected);
    }

    public showNoContentMessage(): boolean {
        return !this.dataSource || this.dataSource.data.length === 0;
    }
}
