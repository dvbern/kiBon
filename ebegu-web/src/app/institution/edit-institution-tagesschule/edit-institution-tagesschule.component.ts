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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    input,
    Input,
    model,
    OnInit
} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {ControlContainer, NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {
    AdminModelEinstellungTagesschuleHasAnmeldung,
    TSModulTagesschuleGroupHasAnmeldung
} from '@kibon/admin/model/institution-tagesschule-einstellungen';
import {InfoSchnittstelleDialogComponent} from '@kibon/admin/ui/tagesschule-modul-list';
import {stammdatenToGroupHasAnmeldung} from '@kibon/admin/util-fn/institution-tagesschule-einstellungen';
import {
    TSDateRange,
    TSEinstellungenTagesschule,
    TSGemeinde,
    TSInstitutionStammdaten,
    TSModulTagesschule,
    TSModulTagesschuleGroup,
    TSTextRessource
} from '@kibon/shared/model/entity';
import {
    getTSModulTagesschuleNameValues,
    TSDayOfWeek,
    TSModulTagesschuleIntervall,
    TSModulTagesschuleName,
    TSModulTagesschuleTyp,
    TSRole
} from '@kibon/shared/model/enums';
import moment from 'moment';
import {combineLatest, firstValueFrom, from, Observable, of} from 'rxjs';
import {mergeMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSInstitutionExternalClient} from '../../../models/TSInstitutionExternalClient';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TagesschuleUtil} from '../../../utils/TagesschuleUtil';
import {DvNgRemoveDialogComponent} from '@kibon/shared/ui/remove-dialog';
import {DvNgThreeButtonDialogComponent} from '../../core/component/dv-ng-three-button-dialog/dv-ng-three-button-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ModulTagesschuleDialogComponent} from '../edit-modul-tagesschule/modul-tagesschule-dialog.component';
import {DialogImportFromOtherInstitutionComponent} from './dialog-import-from-other-institution/dialog-import-from-other-institution.component';

@Component({
    selector: 'dv-edit-institution-tagesschule',
    templateUrl: './edit-institution-tagesschule.component.html',
    styleUrls: ['./edit-institution-tagesschule.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    standalone: false
})
export class EditInstitutionTagesschuleComponent implements OnInit {
    stammdaten = model.required<TSInstitutionStammdaten>();
    editMode = input(false);
    @Input() readonly assignedClients: TSInstitutionExternalClient[];

    einstellungenTagesschule$: Observable<
        AdminModelEinstellungTagesschuleHasAnmeldung[]
    > = combineLatest([
        toObservable(this.stammdaten),
        toObservable(this.editMode)
    ]).pipe(
        mergeMap(([stammdaten, editMode]) => {
            if (!editMode) {
                return of(
                    TagesschuleUtil.sortEinstellungenTagesschuleByPeriod(
                        stammdatenToGroupHasAnmeldung(stammdaten)
                    ) as AdminModelEinstellungTagesschuleHasAnmeldung[]
                );
            }
            return from(
                this.institutionStammdatenRS
                    .getEinstellungenTagesschuleAngemeldet(
                        stammdaten.institutionStammdatenTagesschule
                            .einstellungenTagesschule
                    )
                    .then(
                        eTSAngemeldet =>
                            TagesschuleUtil.sortEinstellungenTagesschuleByPeriod(
                                eTSAngemeldet
                            ) as AdminModelEinstellungTagesschuleHasAnmeldung[]
                    )
            );
        })
    );

    public gemeindeList: TSGemeinde[] = [];
    private readonly panelClass = 'dv-mat-dialog-ts';
    private konfigurationsListe: TSGemeindeKonfiguration[];

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly errorService: ErrorService,
        private readonly dialog: MatDialog,
        private readonly ref: ChangeDetectorRef,
        private readonly authServiceRS: AuthServiceRS
    ) {}

    public ngOnInit(): void {
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
        this.stammdaten().institutionStammdatenTagesschule.einstellungenTagesschule.forEach(
            einst => {
                einst.modulTagesschuleGroups =
                    TagesschuleUtil.sortModulTagesschuleGroups(
                        einst.modulTagesschuleGroups
                    );
            }
        );

        this.gemeindeRS
            .getGemeindeStammdaten(
                this.stammdaten().institutionStammdatenTagesschule.gemeinde.id
            )
            .then(gemeindeStammdaten => {
                this.konfigurationsListe =
                    gemeindeStammdaten.konfigurationsListe;
                this.konfigurationsListe.forEach(config => {
                    config.initProperties();
                });
                this.ref.markForCheck();
            });
        this.sortByPeriod();
    }

    public updateTagesschuleProperty<T extends keyof typeof tagesschulModul>(
        property: T,
        value: TSEinstellungenTagesschule[T],
        idOfTSEinstellungenTagesschule?: string
    ) {
        const tagesschulen =
            this.stammdaten().institutionStammdatenTagesschule
                .einstellungenTagesschule;

        const tagesschulModul = tagesschulen.find(
            item => item.id === idOfTSEinstellungenTagesschule
        );

        if (tagesschulModul) {
            tagesschulModul[property] = value;
        }
    }

    private sortByPeriod(): TSEinstellungenTagesschule[] {
        return TagesschuleUtil.sortEinstellungenTagesschuleByPeriod(
            this.stammdaten().institutionStammdatenTagesschule
                .einstellungenTagesschule
        );
    }

    public onPrePersist(): void {}

    public institutionStammdatenTagesschuleValid(): boolean {
        let result = true;
        this.stammdaten().institutionStammdatenTagesschule.einstellungenTagesschule.forEach(
            einst => {
                einst.modulTagesschuleGroups.forEach(grp => {
                    if (!grp.isValid()) {
                        result = false;
                    }
                });
            }
        );
        return result;
    }

    public addModulTagesschuleGroup(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        if (
            einstellungenTagesschule.modulTagesschuleGroups.length > 0 ||
            !this.isBeforeAktivierungsdatum(einstellungenTagesschule)
        ) {
            // Es ist nicht das erste Modul, wir muessen nicht mehr fragen, ob der Benutzer
            // evtl. Scolaris will ODER die Anmeldung ist eh schon offen und wir koennen keine
            // ScolarisModule mehr erstellen
            this.createDynamischesModul(einstellungenTagesschule);
            return;
        }
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'TS_DIALOG_FIRSTMODUL_TITLE',
            text: 'TS_DIALOG_FIRSTMODUL_TEXT',
            actionOneButtonLabel: 'TS_DIALOG_FIRSTMODUL_ACTION1',
            actionTwoButtonLabel: 'TS_DIALOG_FIRSTMODUL_ACTION2'
        };
        dialogConfig.role = 'dialog';
        this.dialog
            .open(DvNgThreeButtonDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(result => {
                // 1=Dynamisch, 2=Scolaris, undefined=Abbrechen
                if (!EbeguUtil.isNotNullOrUndefined(result)) {
                    return;
                }
                if (1 === result) {
                    this.createDynamischesModul(einstellungenTagesschule);
                } else if (2 === result) {
                    this.changeToScolaris(einstellungenTagesschule);
                }
            });
    }

    private createDynamischesModul(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        const group = new TSModulTagesschuleGroup();
        group.modulTagesschuleName = TSModulTagesschuleName.DYNAMISCH;
        group.bezeichnung = new TSTextRessource();
        this.openModul(einstellungenTagesschule, group);
    }

    public editModulTagesschuleGroup(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung,
        group: TSModulTagesschuleGroup
    ): void {
        this.openModul(einstellungenTagesschule, group);
    }

    private openModul(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung,
        group: TSModulTagesschuleGroup
    ): void {
        if (!this.editMode()) {
            return;
        }
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {modulTagesschuleGroup: group};
        dialogConfig.panelClass = this.panelClass;
        // Wir übergeben die Group an den Dialog. Bei OK erhalten wir die (veränderte) Group zurück, sonst undefined
        firstValueFrom(
            this.dialog
                .open(ModulTagesschuleDialogComponent, dialogConfig)
                .afterClosed()
        ).then(result => {
            if (EbeguUtil.isNotNullOrUndefined(result)) {
                this.applyModulTagesschuleGroup(
                    einstellungenTagesschule,
                    result
                );
            }
        });
    }

    public showSchnittstelleInfoForGroup(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung,
        group: TSModulTagesschuleGroup
    ): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            modulTagesschuleGroup: group,
            institution: this.stammdaten().institution,
            editMode: this.editMode()
        };
        dialogConfig.panelClass = this.panelClass;
        firstValueFrom(
            this.dialog
                .open(InfoSchnittstelleDialogComponent, dialogConfig)
                .afterClosed()
        ).then(result => {
            if (EbeguUtil.isNotNullOrUndefined(result) && this.editMode()) {
                this.applyModulTagesschuleGroup(
                    einstellungenTagesschule,
                    result
                );
            }
        });
    }

    public async removeModulTagesschuleGroup(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung,
        group: TSModulTagesschuleGroup
    ): Promise<void> {
        const confirmation = await firstValueFrom(
            this.dialog
                .open(DvNgRemoveDialogComponent, {
                    data: {title: 'MODUL_LOESCHEN_CONFIRMATION'}
                })
                .afterClosed()
        );
        if (!confirmation) {
            return;
        }
        const index = this.getIndexOfElementwithIdentifier(
            group,
            einstellungenTagesschule.modulTagesschuleGroups
        );
        if (index > -1) {
            einstellungenTagesschule.modulTagesschuleGroups.splice(index, 1);
        }
        this.updateEinstellungenToStammdaten(einstellungenTagesschule);
    }

    public applyModulTagesschuleGroup(
        einstellungenTagesschule: TSEinstellungenTagesschule,
        group: TSModulTagesschuleGroup
    ): void {
        const index = this.getIndexOfElementwithIdentifier(
            group,
            einstellungenTagesschule.modulTagesschuleGroups
        );
        if (index > -1) {
            einstellungenTagesschule.modulTagesschuleGroups[index] = group;
        } else {
            einstellungenTagesschule.modulTagesschuleGroups = [
                ...einstellungenTagesschule.modulTagesschuleGroups,
                group
            ];
        }
        einstellungenTagesschule.modulTagesschuleGroups =
            TagesschuleUtil.sortModulTagesschuleGroups(
                einstellungenTagesschule.modulTagesschuleGroups
            );
        this.updateEinstellungenToStammdaten(einstellungenTagesschule);
    }

    private updateEinstellungenToStammdaten(
        einstellungenTagesschule: TSEinstellungenTagesschule
    ): void {
        this.stammdaten.update(stammdaten => {
            const idx =
                stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.findIndex(
                    eTS => eTS.id === einstellungenTagesschule.id
                );
            stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.splice(
                idx,
                1,
                einstellungenTagesschule
            );
            // necessary since signals without custom equals track changes by reference
            return Object.assign(new TSInstitutionStammdaten(), stammdaten);
        });
    }

    public getIndexOfElementwithIdentifier(
        entityToSearch: TSModulTagesschuleGroup,
        listToSearchIn: Array<TSModulTagesschuleGroup>
    ): number {
        if (EbeguUtil.isNullOrUndefined(entityToSearch)) {
            return -1;
        }
        const idToSearch = entityToSearch.identifier;
        for (let i = 0; i < listToSearchIn.length; i++) {
            if (listToSearchIn[i].identifier === idToSearch) {
                return i;
            }
        }
        return -1;
    }

    public isModulTagesschuleTypScolaris(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): boolean {
        return (
            einstellungenTagesschule.modulTagesschuleTyp ===
            TSModulTagesschuleTyp.SCOLARIS
        );
    }

    public changeToDynamisch(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        einstellungenTagesschule.modulTagesschuleTyp =
            TSModulTagesschuleTyp.DYNAMISCH;
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'MODUL_TYP_DYNAMISCH_TITLE',
            text: 'MODUL_TYP_DYNAMISCH_INFO'
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        // Benutzer hat abgebrochen -> Flag zuruecksetzen
                        einstellungenTagesschule.modulTagesschuleTyp =
                            TSModulTagesschuleTyp.SCOLARIS;
                        return;
                    }
                    einstellungenTagesschule.modulTagesschuleTyp =
                        TSModulTagesschuleTyp.DYNAMISCH;
                    // Die Module sind neu dynamisch -> Alle eventuell vorhandenen löschen
                    einstellungenTagesschule.modulTagesschuleGroups = [];
                    this.updateEinstellungenToStammdaten(
                        einstellungenTagesschule
                    );
                },
                () => {
                    this.errorService.addMesageAsError('error');
                }
            );
    }

    public askAndChangeToScolaris(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'MODUL_TYP_SCOLARIS_TITLE',
            text: 'MODUL_TYP_SCOLARIS_INFO'
        };
        dialogConfig.panelClass = this.panelClass;
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        // Benutzer hat abgebrochen -> Flag zuruecksetzen
                        einstellungenTagesschule.modulTagesschuleTyp =
                            TSModulTagesschuleTyp.DYNAMISCH;
                        return;
                    }
                    this.changeToScolaris(einstellungenTagesschule);
                },
                () => {
                    this.errorService.addMesageAsError('error');
                }
            );
    }

    private changeToScolaris(
        einstellungenTagesschule: TSEinstellungenTagesschule
    ): void {
        einstellungenTagesschule.modulTagesschuleTyp =
            TSModulTagesschuleTyp.SCOLARIS;
        // Die Module sind neu nach Scolaris -> Alle eventuell vorhandenen werden gelöscht
        this.createModulGroupsScolaris(einstellungenTagesschule);
    }

    public createModulGroupsScolaris(
        einstellungenTagesschule: TSEinstellungenTagesschule
    ): void {
        einstellungenTagesschule.modulTagesschuleGroups = [];
        getTSModulTagesschuleNameValues().forEach(
            (modulname: TSModulTagesschuleName) => {
                const group = this.createModulGroupScolaris(modulname);
                einstellungenTagesschule.modulTagesschuleGroups.push(group);
            }
        );
        this.updateEinstellungenToStammdaten(einstellungenTagesschule);
    }

    private createModulGroupScolaris(
        modulname: TSModulTagesschuleName
    ): TSModulTagesschuleGroup {
        const group = new TSModulTagesschuleGroup();
        group.modulTagesschuleName = modulname;
        group.intervall = TSModulTagesschuleIntervall.WOECHENTLICH;
        group.wirdPaedagogischBetreut = true;
        group.module = [];
        group.bezeichnung = new TSTextRessource();
        this.createModuleScolaris(group);
        return group;
    }

    private createModuleScolaris(group: TSModulTagesschuleGroup): void {
        const montag = new TSModulTagesschule();
        montag.wochentag = TSDayOfWeek.MONDAY;
        group.module.push(montag);

        const dienstag = new TSModulTagesschule();
        dienstag.wochentag = TSDayOfWeek.TUESDAY;
        group.module.push(dienstag);

        const mittwoch = new TSModulTagesschule();
        mittwoch.wochentag = TSDayOfWeek.WEDNESDAY;
        group.module.push(mittwoch);

        const donnerstag = new TSModulTagesschule();
        donnerstag.wochentag = TSDayOfWeek.THURSDAY;
        group.module.push(donnerstag);

        const freitag = new TSModulTagesschule();
        freitag.wochentag = TSDayOfWeek.FRIDAY;
        group.module.push(freitag);
    }

    public importFromOtherInstitution(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        einstellungenTagesschule.modulTagesschuleTyp =
            TSModulTagesschuleTyp.DYNAMISCH;
        this.institutionStammdatenRS
            .getAllTagesschulenForCurrentBenutzer()
            .then((institutionStammdatenList: TSInstitutionStammdaten[]) => {
                this.openDialogImportFromOtherInstitutionComponent$(
                    institutionStammdatenList
                        .filter(is => is.id !== this.stammdaten().id)
                        .concat(this.stammdaten())
                ).subscribe(
                    (modules: TSModulTagesschuleGroup[]) => {
                        if (!modules) {
                            return;
                        }
                        einstellungenTagesschule.modulTagesschuleGroups = [
                            ...einstellungenTagesschule.modulTagesschuleGroups,
                            ...modules.map(module =>
                                Object.assign(
                                    new TSModulTagesschuleGroupHasAnmeldung(),
                                    module,
                                    {hasAnmeldung: false}
                                )
                            )
                        ];
                        this.updateEinstellungenToStammdaten(
                            einstellungenTagesschule
                        );
                    },
                    () => {
                        this.errorService.addMesageAsError('error');
                    }
                );
            });
    }

    private openDialogImportFromOtherInstitutionComponent$(
        institutionList: TSInstitutionStammdaten[]
    ): Observable<TSModulTagesschuleGroup[]> {
        if (!this.editMode()) {
            return undefined;
        }
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            institutionList
        };
        dialogConfig.panelClass = this.panelClass;
        // Wir übergeben die Group an den Dialog. Bei OK erhalten wir die (veränderte) Group zurück, sonst undefined
        return this.dialog
            .open(DialogImportFromOtherInstitutionComponent, dialogConfig)
            .afterClosed();
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }

    public trackById(einstellungGP: TSEinstellungenTagesschule): string {
        return einstellungGP.id;
    }

    public isBeforeAktivierungsdatum(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): boolean {
        if (EbeguUtil.isNullOrUndefined(this.konfigurationsListe)) {
            return false;
        }
        const konfiguration = this.konfigurationsListe.find(
            gemeindeKonfiguration =>
                gemeindeKonfiguration.gesuchsperiode.id ===
                einstellungenTagesschule.gesuchsperiode.id
        );
        if (konfiguration) {
            return konfiguration.konfigTagesschuleAktivierungsdatum.isAfter(
                moment([])
            );
        }
        return false;
    }

    public showGesuchsperiode(gueltigkeit: TSDateRange): boolean {
        let showGesuchsperiode = gueltigkeit.gueltigBis.isAfter(
            this.stammdaten().gueltigkeit.gueltigAb
        );
        if (
            EbeguUtil.isNotNullOrUndefined(
                this.stammdaten().gueltigkeit.gueltigBis
            )
        ) {
            showGesuchsperiode =
                showGesuchsperiode &&
                gueltigkeit.gueltigAb.isBefore(
                    this.stammdaten().gueltigkeit.gueltigBis
                );
        }
        return showGesuchsperiode;
    }

    public isScolarisVollstaendig(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): boolean {
        return (
            einstellungenTagesschule.modulTagesschuleGroups.length ===
            getTSModulTagesschuleNameValues().length
        );
    }

    public addFehlendeScolarisModule(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): void {
        getTSModulTagesschuleNameValues().forEach(
            (modulname: TSModulTagesschuleName) => {
                const mtg =
                    einstellungenTagesschule.modulTagesschuleGroups.filter(
                        modulTagesschuleGroup =>
                            modulTagesschuleGroup.modulTagesschuleName ===
                            modulname
                    );
                if (mtg.length === 0) {
                    const group = this.createModulGroupScolaris(modulname);
                    einstellungenTagesschule.modulTagesschuleGroups = [
                        ...einstellungenTagesschule.modulTagesschuleGroups,
                        Object.assign(
                            new TSModulTagesschuleGroupHasAnmeldung(),
                            group,
                            {hasAnmeldung: false}
                        )
                    ];
                }
            }
        );
    }

    public showTagiCheckbox(
        einstellungenTagesschule: AdminModelEinstellungTagesschuleHasAnmeldung
    ): boolean {
        if (EbeguUtil.isNullOrUndefined(this.konfigurationsListe)) {
            return false;
        }

        const konfiguration = this.konfigurationsListe.find(
            gemeindeKonfiguration =>
                gemeindeKonfiguration.gesuchsperiode.id ===
                einstellungenTagesschule.gesuchsperiode.id
        );
        if (konfiguration) {
            return konfiguration.konfigTagesschuleTagisEnabled;
        }
        return false;
    }

    public canEditTagi(): boolean {
        if (
            this.authServiceRS.isOneOfRoles([
                TSRole.ADMIN_TS,
                TSRole.ADMIN_GEMEINDE,
                TSRole.SUPER_ADMIN
            ])
        ) {
            return this.editMode();
        }
        return false;
    }
}
