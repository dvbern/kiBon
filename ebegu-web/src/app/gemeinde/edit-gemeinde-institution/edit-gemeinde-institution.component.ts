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

import {SelectionModel} from '@angular/cdk/collections';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TSInstitutionStatus} from '@kibon/shared/model/enums';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {
    TSInstitution,
    TSInstitutionStammdaten
} from '@kibon/shared/model/entity';
import {TSPublicAppConfig} from '@kibon/shared/model/einstellung';
import {TranslateService} from '@ngx-translate/core';
import {combineLatest, from, Subject} from 'rxjs';
import {mergeMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {EditGemeindeInstitutionTableComponent} from './edit-gemeinde-institution-table/edit-gemeinde-institution-table.component';

export interface TableAuswahlInstitution {
    id: string;
    name: string;
    institutionArt: string;
    institution: TSInstitution;
    iban?: string;
    kontoinhaber?: string;
    infomaKreditorenNr?: string;
    infomaBankCode?: string;
    editInfoma?: string;
    status?: TSInstitutionStatus;
}

@Component({
    selector: 'dv-edit-gemeinde-institution',
    templateUrl: './edit-gemeinde-institution.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    imports: [SharedModule, EditGemeindeInstitutionTableComponent]
})
export class EditGemeindeInstitutionComponent implements OnInit, OnDestroy {
    @Input() public editMode: boolean;
    @Input() public stammdaten: TSGemeindeStammdaten;

    public displayedColumns: string[] = ['checkbox', 'name', 'institutionArt'];
    public displayedColumnsInfoma: string[] = [
        'checkbox',
        'name',
        'institutionArt',
        'iban',
        'kontoinhaber',
        'infomaKreditorenNr',
        'infomaBankCode',
        'editInfoma'
    ];

    public dataSource: MatTableDataSource<TableAuswahlInstitution> =
        new MatTableDataSource<TableAuswahlInstitution>();
    public institutionen: TSInstitution[];
    public isInfomazahlungen: boolean = false;

    public selection: SelectionModel<TableAuswahlInstitution> =
        new SelectionModel<TableAuswahlInstitution>(true, []);
    private readonly unsubscribe$ = new Subject<void>();
    @ViewChild(MatSort, {static: true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;

    public constructor(
        private readonly translate: TranslateService,
        private readonly institutionRS: InstitutionRS,
        private readonly institutionenStammdatenRS: InstitutionStammdatenRS,
        private readonly applicationPropertyRS: SharedUtilApplicationPropertyRsService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly cd: ChangeDetectorRef
    ) {}

    public ngOnInit(): void {
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe((res: TSPublicAppConfig) => {
                this.isInfomazahlungen = res.infomaZahlungen;
                this.cd.markForCheck();
            });
        this.initInstitution();
        this.showInfomaFields();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public initInstitution(): void {
        this.institutionRS
            .getAllBgInstitutionen()
            .pipe(
                mergeMap((institutionen: TSInstitution[]) =>
                    combineLatest(
                        institutionen.map((institution: TSInstitution) =>
                            from(
                                this.institutionenStammdatenRS.fetchInstitutionStammdatenByInstitution(
                                    institution.id
                                )
                            )
                        )
                    )
                ),
                takeUntil(this.unsubscribe$)
            )
            .subscribe((institutionenArray: TSInstitutionStammdaten[]) => {
                if (this.getGemeindeAngebotBGTFO()) {
                    this.dataSource.data = institutionenArray.map(
                        (value: TSInstitutionStammdaten) => {
                            return {
                                id: value?.institution.id,
                                name: value?.institution.name,
                                institutionArt: this.translate.instant(
                                    value?.betreuungsangebotTyp
                                ),
                                institution: value?.institution,
                                iban: value
                                    ?.institutionStammdatenBetreuungsgutscheine
                                    .iban,
                                kontoinhaber:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .kontoinhaber,
                                infomaKreditorenNr:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .infomaKreditorennummer,
                                infomaBankCode:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .infomaBankcode,
                                status: value?.institution.status
                            } as TableAuswahlInstitution;
                        }
                    );

                    if (this.stammdaten.alleBgInstitutionenZugelassen) {
                        this.stammdaten.zugelasseneBgInstitutionen =
                            institutionenArray.map(
                                (value: TSInstitutionStammdaten) => {
                                    return value.institution;
                                }
                            );
                    }
                } else {
                    this.dataSource.data = institutionenArray
                        .filter((value: TSInstitutionStammdaten) => {
                            return (
                                value?.betreuungsangebotTyp !== 'TAGESFAMILIEN'
                            );
                        })
                        .map((value: TSInstitutionStammdaten) => {
                            return {
                                id: value?.institution.id,
                                name: value?.institution.name,
                                institutionArt: this.translate.instant(
                                    value?.betreuungsangebotTyp
                                ),
                                institution: value?.institution,
                                iban: value
                                    ?.institutionStammdatenBetreuungsgutscheine
                                    .iban,
                                kontoinhaber:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .kontoinhaber,
                                infomaKreditorenNr:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .infomaKreditorennummer,
                                infomaBankCode:
                                    value
                                        ?.institutionStammdatenBetreuungsgutscheine
                                        .infomaBankcode,
                                status: value?.institution.status
                            } as TableAuswahlInstitution;
                        });

                    if (this.stammdaten.alleBgInstitutionenZugelassen) {
                        this.stammdaten.zugelasseneBgInstitutionen =
                            institutionenArray
                                .filter((value: TSInstitutionStammdaten) => {
                                    return (
                                        value?.betreuungsangebotTyp !==
                                        'TAGESFAMILIEN'
                                    );
                                })
                                .map((value: TSInstitutionStammdaten) => {
                                    return value.institution;
                                });
                    }
                }
            });
    }

    private getGemeindeAngebotBGTFO(): boolean {
        return this.stammdaten.gemeinde.angebotBGTFO;
    }

    public showInfomaFields(): boolean {
        return (
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorBgGemeindeRoles()
            ) &&
            this.isInfomazahlungen &&
            this.stammdaten.gemeinde.infomaZahlungen
        );
    }
}
