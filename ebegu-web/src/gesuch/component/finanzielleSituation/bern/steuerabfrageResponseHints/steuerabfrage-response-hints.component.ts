/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    ViewEncapsulation,
    inject
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DvNgRemoveDialogComponent} from '@app/shared/component/remove-dialog';
import {TranslateService} from '@ngx-translate/core';
import moment from 'moment';
import {BehaviorSubject} from 'rxjs';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSAntragStatus} from '../../../../../models/enums/TSAntragStatus';
import {TSRole} from '../../../../../models/enums/TSRole';
import {
    isSteuerdatenAnfrageStatusErfolgreich,
    TSSteuerdatenAnfrageStatus
} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSGesuchstellerContainer} from '../../../../../models/TSGesuchstellerContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {
    DialogInitStekNummerVerknuepfenComponent,
    InitStekIdentifierDialogData
} from '../dialog-init-stek-identifier-verknuepfen/dialog-init-stek-identifier-verknpuefen.component';

@Component({
    selector: 'dv-steuerabfrage-response-hints',
    templateUrl: './steuerabfrage-response-hints.component.html',
    styleUrls: ['./steuerabfrage-response-hints.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class SteuerabfrageResponseHintsComponent implements OnInit, OnChanges {
    readonly gesuchModelManager = inject(GesuchModelManager);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly dialog = inject(MatDialog);
    private readonly finSitRS = inject(FinanzielleSituationRS);
    private readonly changeDetectorRef = inject(ChangeDetectorRef);
    private readonly translate = inject(TranslateService);
    private readonly errorService = inject(ErrorService);

    @Input()
    public readonly status: TSSteuerdatenAnfrageStatus;

    @Input()
    private readonly gsStatus: TSSteuerdatenAnfrageStatus;

    @Input()
    public readonly timestampAbruf: moment.Moment;

    @Input()
    public steuerAbfrageResponeHintStatusText: string;

    @Input()
    public steuerAbfrageRequestRunning: boolean;

    @Input()
    public isStekIdentifierSetOnGS: boolean;

    @Output()
    private readonly tryAgainEvent: EventEmitter<void> =
        new EventEmitter<void>();

    @Output()
    readonly resetStekIdentifierClicked: EventEmitter<void> =
        new EventEmitter<void>();

    public geburtstagNotMatching$: BehaviorSubject<boolean> =
        new BehaviorSubject<boolean>(false);

    public ngOnChanges(): void {
        this.changeDetectorRef.markForCheck();
    }

    public ngOnInit(): void {
        const gsContainer = this.getGesuchstellerContainer();

        if (this.showZugriffErfolgreich(this.status)) {
            this.finSitRS
                .geburtsdatumMatchesSteuerabfrage(
                    gsContainer.gesuchstellerJA.geburtsdatum,
                    gsContainer.finanzielleSituationContainer.id
                )
                .then(isMatching => {
                    this.geburtstagNotMatching$.next(!isMatching);
                });
        }
    }

    public showZugriffErfolgreich(
        statusToCheck: TSSteuerdatenAnfrageStatus
    ): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(statusToCheck) &&
            isSteuerdatenAnfrageStatusErfolgreich(statusToCheck)
        );
    }

    public showWarningRetry(): boolean {
        return (
            this.showZugriffFailed() ||
            this.showWarningKeinPartnerGemeinsam() ||
            this.showWarningGeburtsdatum() ||
            this.showWarningPartnerNichtGemeinsam()
        );
    }

    public getWarningText(): string {
        switch (this.status) {
            case TSSteuerdatenAnfrageStatus.FAILED:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_FAILED',
                    {gs1: this.getGS1Name()}
                );
            case TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_PARTNER_NICHT_GEMEINSAM'
                );
            case TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_FAILED_GEBURTSDATUM',
                    {namegs2: this.getGS2name()}
                );
            case TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEIN_PARTNER_GEMEINSAM'
                );
            case TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_UNTERJAEHRIG'
                );
            case TSSteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_VERAENDERTE_PARTNERSCHAFT'
                );
            case TSSteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_UNREGELMAESSIGKEIT'
                );
            case TSSteuerdatenAnfrageStatus.FAILED_KEINE_NUMMER:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEINE_ZPV',
                    {email: this.getEmailBesitzende()}
                );
            case TSSteuerdatenAnfrageStatus.FAILED_KEINE_NUMMER_GS2:
                return this.translate.instant(
                    'FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEINE_ZPV_GS2',
                    {gs2: this.getGS2name()}
                );
            default:
                return '';
        }
    }

    private showZugriffFailed(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED;
    }

    private showWarningKeinPartnerGemeinsam(): boolean {
        return (
            this.status ===
            TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM
        );
    }

    private showWarningGeburtsdatum(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM;
    }

    private showWarningPartnerNichtGemeinsam(): boolean {
        return (
            this.status ===
            TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM
        );
    }

    public showWarningWithoutRetry(): boolean {
        return (
            this.showZugriffUnterjaehrigeFall() ||
            this.showWarningUnregelmaessigkeit() ||
            this.showWarningVeraendertePartnerschaft()
        );
    }

    private showZugriffUnterjaehrigeFall(): boolean {
        return (
            this.status ===
            TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL
        );
    }

    private showWarningVeraendertePartnerschaft(): boolean {
        return (
            this.status ===
            TSSteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT
        );
    }

    private showWarningUnregelmaessigkeit(): boolean {
        return (
            this.status === TSSteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT
        );
    }

    public showZugriffKeineNummer(): boolean {
        return (
            this.showZugriffKeineNummerGS1() || this.showZugriffKeineNummerGS2()
        );
    }

    private showZugriffKeineNummerGS1(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_NUMMER;
    }

    private showZugriffKeineNummerGS2(): boolean {
        return (
            this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_NUMMER_GS2
        );
    }

    public showRetry(): boolean {
        return (
            this.status === TSSteuerdatenAnfrageStatus.RETRY &&
            !this.isGesuchReadonly()
        );
    }

    public showRetryForGemeinde(): boolean {
        return (
            this.showZugriffErfolgreich(this.gsStatus) &&
            this.isGemeindeOrSuperadmin()
        );
    }

    public getGS1Name(): string {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1
            ? this.gesuchModelManager
                  .getGesuch()
                  .gesuchsteller1.extractFullName()
            : this.gesuchModelManager
                  .getGesuch()
                  .gesuchsteller2.extractFullName();
    }

    public getGS2Name(): string {
        return this.gesuchModelManager
            .getGesuch()
            .gesuchsteller2.extractFullName();
    }

    public tryAgain(): void {
        this.dialog
            .open(DvNgRemoveDialogComponent, {
                data: {
                    title: this.translate.instant(
                        'SCHNITTSTELLE_ERENEUT_ABFRAGEN'
                    )
                }
            })
            .afterClosed()
            .subscribe({
                next: confirmation => {
                    if (confirmation) {
                        this.tryAgainEvent.emit();
                    }
                },
                error: () => {
                    this.errorService.addMesageAsInfo(
                        this.translate.instant('ERROR_UNEXPECTED')
                    );
                }
            });
    }

    public getEmailBesitzende(): string {
        return this.gesuchModelManager.getGesuch().dossier.fall.besitzer.email;
    }

    public getGS2name(): string {
        return this.gesuchModelManager
            .getGesuch()
            .gesuchsteller2.gesuchstellerJA.getFullName();
    }

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    public openDialogGSStekIdentifierVerknuepfen(): void {
        const dialogOptions: MatDialogConfig<InitStekIdentifierDialogData> = {
            data: {
                gs: this.getGesuchstellerContainer(),
                korrespondenzSprache:
                    this.gesuchModelManager.getGesuch().gesuchsteller1
                        .gesuchstellerJA.korrespondenzSprache,
                gesuch: this.gesuchModelManager.getGesuch()
            },
            panelClass: 'steuerdaten-email-dialog'
        };
        this.dialog.open(
            DialogInitStekNummerVerknuepfenComponent,
            dialogOptions
        );
    }

    private getGesuchstellerContainer(): TSGesuchstellerContainer {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1
            ? this.gesuchModelManager.getGesuch().gesuchsteller1
            : this.gesuchModelManager.getGesuch().gesuchsteller2;
    }

    public isGemeindeOrSuperadmin() {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getGemeindeOrBGOrTSRoles().concat(TSRole.SUPER_ADMIN)
        );
    }

    public tryAgainPossible(): boolean {
        return (
            !this.gesuchModelManager.isGesuchReadonly() &&
            (this.status === TSSteuerdatenAnfrageStatus.PROVISORISCH ||
                this.status === TSSteuerdatenAnfrageStatus.NEUE_VERANLAGUNG)
        );
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public translateVeranlagungsstand(): string {
        return this.translate.instant(`VERANLAGUNGSSTAND_${this.status}`);
    }

    public checkboxInformierenPossible(): boolean {
        return (
            this.isGemeindeOrSuperadmin() &&
            this.status === TSSteuerdatenAnfrageStatus.PROVISORISCH
        );
    }

    public showWarningNeueVeranlagung(): boolean {
        return (
            !this.gesuchModelManager.isGesuchReadonly() &&
            this.status === TSSteuerdatenAnfrageStatus.NEUE_VERANLAGUNG &&
            this.gesuchModelManager.getGesuch().status !==
                TSAntragStatus.FREIGABEQUITTUNG
        );
    }

    public showWarningNeueVeranlagungInStatusFreigegeben() {
        return (
            this.gesuchModelManager.getGesuch().status ===
                TSAntragStatus.FREIGABEQUITTUNG &&
            this.status === TSSteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
        );
    }
}
