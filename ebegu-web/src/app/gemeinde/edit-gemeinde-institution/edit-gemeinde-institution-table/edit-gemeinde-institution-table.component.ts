import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    input,
    InputSignal,
    OnInit,
    ViewChild
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {
    MatDialog,
    MatDialogConfig,
    MatDialogModule
} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {
    TSInstitution,
    TSInstitutionStammdaten
} from '@kibon/shared/model/entity';
import {TSGemeindeStammdaten} from '../../../../models/TSGemeindeStammdaten';
import {TSInstitutionUpdate} from '../../../../models/TSInstitutionUpdate';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';
import {SharedModule} from '../../../shared/shared.module';
import {TableAuswahlInstitution} from '../edit-gemeinde-institution.component';
import {EditInfomaDialogComponent} from '../edit-infoma-dialog/edit-infoma-dialog.component';

@Component({
    selector: 'dv-edit-gemeinde-institution-table',
    imports: [SharedModule, MatDialogModule],
    templateUrl: './edit-gemeinde-institution-table.component.html',
    styleUrl: './edit-gemeinde-institution-table.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class EditGemeindeInstitutionTableComponent implements OnInit {
    displayedColumns: InputSignal<string[]> = input<string[]>();
    dataSource: InputSignal<MatTableDataSource<TableAuswahlInstitution>> =
        input<MatTableDataSource<TableAuswahlInstitution>>();
    stammdaten: InputSignal<TSGemeindeStammdaten> =
        input<TSGemeindeStammdaten>();
    editMode: InputSignal<boolean> = input<boolean>();

    public matTableClass: string = '';

    @ViewChild(MatSort, {static: true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;

    private readonly panelClass: string = 'dv-mat-dialog-ts';

    @ViewChild(MatSort) set matSort(ms: MatSort) {
        this.sort = ms;
        this.setDataSourceAttributes();
    }

    @ViewChild(MatPaginator) set matPaginator(mp: MatPaginator) {
        this.paginator = mp;
        this.setDataSourceAttributes();
    }

    constructor(
        private readonly dialog: MatDialog,
        private readonly cd: ChangeDetectorRef,
        private institutionRS: InstitutionRS,
        private institutionStammdatenRS: InstitutionStammdatenRS
    ) {}

    public ngOnInit(): void {
        this.sortTable();
        this.setDataSourceAttributes();
        this.addTableStyling();
    }

    addTableStyling() {
        if (this.displayedColumns().length > 3) {
            this.matTableClass = 'dv-infoma-on';
        } else {
            this.matTableClass = 'dv-infoma-off';
        }
    }

    setDataSourceAttributes() {
        this.dataSource().paginator = this.paginator;
        this.dataSource().sort = this.sort;
    }

    public checkSelectedInstitutionen(id: string): boolean {
        return this.stammdaten().zugelasseneBgInstitutionen.some(
            (institution: TSInstitution): boolean => institution.id === id
        );
    }

    public editInstitutionenArray(institution: TSInstitution) {
        if (this.checkSelectedInstitutionen(institution.id)) {
            // remove institution from array
            const index: number =
                this.stammdaten().zugelasseneBgInstitutionen.findIndex(
                    (el: TSInstitution): boolean => el.id === institution.id
                );
            this.stammdaten().zugelasseneBgInstitutionen.splice(index, 1);
        } else {
            // add institution to array
            this.stammdaten().zugelasseneBgInstitutionen.push(institution);
        }
    }

    public checkInstitutionStatusAktiv(
        institution: TableAuswahlInstitution
    ): boolean {
        return institution.status === 'AKTIV';
    }

    public setAll(checked: boolean): void {
        if (this.stammdaten().zugelasseneBgInstitutionen == null) {
            this.stammdaten().alleBgInstitutionenZugelassen = false;
            return;
        }
        if (checked) {
            this.stammdaten().zugelasseneBgInstitutionen =
                this.dataSource().data.map(value => {
                    return value.institution;
                });
        } else {
            this.stammdaten().zugelasseneBgInstitutionen = [];
            this.stammdaten().alleBgInstitutionenZugelassen = false;
        }
    }

    public doFilter(value: string): void {
        this.dataSource().filter = value.trim().toLocaleLowerCase();
    }

    public setAllBgFlag(): boolean {
        return (
            this.dataSource().data.length ===
            this.stammdaten().zugelasseneBgInstitutionen.length
        );
    }

    private sortTable(): void {
        this.dataSource().sortingDataAccessor = (
            data: any,
            sortHeaderId: any
        ) => {
            if (typeof data[sortHeaderId] === 'string') {
                return data[sortHeaderId].toLocaleLowerCase();
            }
            return data[sortHeaderId];
        };
    }

    public editInfoma(item: TableAuswahlInstitution) {
        this.openModul(item);
    }

    private openModul(item: TableAuswahlInstitution): void {
        const dialogConfig: MatDialogConfig = new MatDialogConfig();
        dialogConfig.data = item;
        dialogConfig.panelClass = this.panelClass;
        this.dialog
            .open(EditInfomaDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe((result: TableAuswahlInstitution) => {
                if (result) {
                    this.fetchInstitutionAndStammdaten(result.id).then(
                        (iStammdaten: TSInstitutionStammdaten) => {
                            const updateModel: TSInstitutionUpdate =
                                new TSInstitutionUpdate();
                            updateModel.name = result.institution.name;
                            updateModel.traegerschaftId = result.institution
                                .traegerschaft
                                ? result.institution.traegerschaft.id
                                : null;
                            updateModel.stammdaten = iStammdaten;
                            updateModel.stammdaten.institutionStammdatenBetreuungsgutscheine.iban =
                                result.iban;
                            updateModel.stammdaten.institutionStammdatenBetreuungsgutscheine.kontoinhaber =
                                result.kontoinhaber;
                            updateModel.stammdaten.institutionStammdatenBetreuungsgutscheine.infomaKreditorennummer =
                                result.infomaKreditorenNr;
                            updateModel.stammdaten.institutionStammdatenBetreuungsgutscheine.infomaBankcode =
                                result.infomaBankCode;

                            this.updateInstitution(result.id, updateModel);
                            this.cd.markForCheck(); // needed to instantly update frontend values
                        }
                    );
                }
            });
    }

    private async fetchInstitutionAndStammdaten(institutionId: string) {
        const institutionStammdaten: TSInstitutionStammdaten =
            await this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(
                institutionId
            );
        return institutionStammdaten;
    }

    private updateInstitution(
        institutionId: string,
        updateModel: TSInstitutionUpdate
    ): void {
        this.institutionRS
            .updateInstitution(institutionId, updateModel)
            .subscribe(); //subscription needs to be called otherwise values dont update
    }
}
