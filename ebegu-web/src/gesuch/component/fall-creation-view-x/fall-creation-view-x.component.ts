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
    OnInit,
    inject
} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {GesuchUiMutationDialogComponent} from '@kibon/gesuch/ui/mutation-dialog';
import {SharedUtilApplicationPropertyRsService} from '@kibon/shared/util/application-property-rs';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {
    TSWizardStepName,
    TSWizardStepStatus,
    TSRole,
    TSGesuchsperiodeStatus
} from '@kibon/shared/model/enums';
import {TSGemeinde, TSGesuchsperiode} from '@kibon/shared/model/entity';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ErrorService} from '../../../app/core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../../app/core/service/gesuchsperiodeRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSEinstellungKey} from '../../../admin/einstellungen/TSEinstellungKey';
import {TSGesuch} from '../../../models/TSGesuch';
import {MomentUtil} from '@kibon/shared/util-fn/date';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {GesuchRS} from '../../service/gesuchRS.rest';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';
import {firstValueFrom} from 'rxjs';

const LOG = LogFactory.createLog('FallCreationViewXComponent');

@Component({
    selector: 'dv-fall-creation-view-x',
    templateUrl: './fall-creation-view-x.component.html',
    styleUrls: ['./fall-creation-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FallCreationViewXComponent
    extends AbstractGesuchViewX<TSGesuch>
    implements OnInit
{
    readonly gesuchModelManager: GesuchModelManager;
    private readonly errorService = inject(ErrorService);
    protected readonly wizardStepManager: WizardStepManager;
    private readonly $translate = inject(TranslateService);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly gesuchsperiodeRS = inject(GesuchsperiodeRS);
    private readonly cd = inject(ChangeDetectorRef);
    private readonly uiRouterGlobals = inject(UIRouterGlobals);
    private readonly einstellungService = inject(EinstellungRS);
    private readonly $state = inject(StateService);
    private readonly gesuchRS = inject(GesuchRS);
    private readonly applicationPropertyRS = inject(
        SharedUtilApplicationPropertyRsService
    );
    private readonly dialog = inject(MatDialog);

    public gesuchsperiodeId: string;
    // @ViewChild(NgForm) protected readonly form: NgForm;

    private yetUnusedGesuchsperiodenListe: Array<TSGesuchsperiode>;

    public isTagesschuleEnabledForMandant: boolean;

    private isBegruendungMutationActiv: boolean;

    public constructor() {
        const gesuchModelManager = inject(GesuchModelManager);
        const wizardStepManager = inject(WizardStepManager);

        super(
            gesuchModelManager,
            wizardStepManager,
            TSWizardStepName.GESUCH_ERSTELLEN
        );

        this.gesuchModelManager = gesuchModelManager;
        this.wizardStepManager = wizardStepManager;
    }

    public async ngOnInit(): Promise<void> {
        // TODO: do we need super.onInit? See abstractGesuchView.$onInit

        await this.readStateParams();
        this.initViewModel();
    }

    private readStateParams(): Promise<TSGesuch> {
        if (
            this.uiRouterGlobals.params.gesuchsperiodeId &&
            this.uiRouterGlobals.params.gesuchsperiodeId !== ''
        ) {
            this.gesuchsperiodeId =
                this.uiRouterGlobals.params.gesuchsperiodeId;
        }
        if (this.uiRouterGlobals.params.creationAction) {
            return this.gesuchModelManager.createNewAntrag(
                this.uiRouterGlobals.params.gesuchId,
                this.uiRouterGlobals.params.dossierId,
                this.uiRouterGlobals.params.eingangsart,
                this.uiRouterGlobals.params.gemeindeId,
                this.uiRouterGlobals.params.gesuchsperiodeId,
                this.uiRouterGlobals.params.creationAction,
                this.uiRouterGlobals.params.sozialdienstId
            ) as Promise<TSGesuch>;
        }
        return Promise.resolve(null);
    }

    private initViewModel(): void {
        // gesuch should already have been initialized in resolve function
        if (
            (EbeguUtil.isNullOrUndefined(this.gesuchsperiodeId) ||
                this.gesuchsperiodeId === '') &&
            this.gesuchModelManager.getGesuchsperiode()
        ) {
            this.gesuchsperiodeId =
                this.gesuchModelManager.getGesuchsperiode().id;
        }

        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .subscribe(res => {
                this.isTagesschuleEnabledForMandant = res.angebotTSActivated;
            });

        const dossier = this.gesuchModelManager.getDossier();
        if (!dossier) {
            return;
        }
        this.gesuchsperiodeRS
            .getAllPeriodenForGemeinde(dossier.gemeinde.id, dossier.id)
            .then((response: TSGesuchsperiode[]) => {
                this.yetUnusedGesuchsperiodenListe = response;
                this.cd.markForCheck();
            });

        const gesuchsPeriode = this.gesuchModelManager.getGesuchsperiode();
        if (EbeguUtil.isNotNullOrUndefined(gesuchsPeriode)) {
            this.einstellungService
                .findEinstellung(
                    TSEinstellungKey.BEGRUENDUNG_MUTATION_AKTIVIERT,
                    this.gesuchModelManager.getGemeinde().id,
                    gesuchsPeriode.id
                )
                .subscribe(
                    einstellung => {
                        this.isBegruendungMutationActiv =
                            einstellung.value === 'true';
                        this.cd.markForCheck();
                    },
                    error => LOG.error(error)
                );
        }
    }

    public save(
        navigateFunction: (gesuch: TSGesuch | undefined | void) => void
    ): void {
        if (!this.isGesuchValid()) {
            this.form.form.markAllAsTouched();
            navigateFunction(undefined);
            return undefined;
        }
        if (!this.isSavingNecessary()) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately

            Promise.resolve(this.gesuchModelManager.getGesuch()).then(gesuch =>
                navigateFunction(gesuch)
            );
            return;
        }
        this.errorService.clearAll();
        if (
            this.authServiceRS.isRole(TSRole.GESUCHSTELLER) &&
            this.gesuchModelManager.isNewMutation() &&
            this.getGesuchModel().gesuchsperiode.isBefore(MomentUtil.now())
        ) {
            firstValueFrom(
                this.dialog
                    .open(GesuchUiMutationDialogComponent, {
                        data: {
                            periode:
                                this.gesuchModelManager.getGesuch()
                                    .gesuchsperiode
                        }
                    })
                    .afterClosed()
            ).then(async hasConfirmed => {
                if (hasConfirmed) {
                    this.saveGesuchAndFall(navigateFunction);
                } else {
                    this.$state.go('gesuchsteller.dashboard');
                }
            });
        } else {
            this.saveGesuchAndFall(navigateFunction);
        }
    }

    private saveGesuchAndFall(
        navigateFunction: (gesuch: TSGesuch | undefined | void) => void
    ): void {
        this.gesuchModelManager
            .saveGesuchAndFall()
            .then(gesuch => {
                // if sozialdienst Fall Step muss be updated
                if (
                    EbeguUtil.isNotNullOrUndefined(
                        gesuch.dossier.fall.sozialdienstFall
                    )
                ) {
                    this.wizardStepManager.updateCurrentWizardStepStatus(
                        TSWizardStepStatus.OK
                    );
                }
                this.cd.markForCheck();
                return gesuch;
            })
            .catch(err => console.error(err))
            .then(gesuch => navigateFunction(gesuch));
    }

    private isSavingNecessary(): boolean {
        // if form is dirty or gesuch is new => save
        if (this.form.dirty || this.gesuchModelManager.getGesuch().isNew()) {
            return true;
        }
        // user is sozialdienst and step Sozialdienst is ok => don't save
        if (this.isSozialdienstAndOk()) {
            return false;
        }
        // if user is Gemeinde, Kanton or Superadmin => don't save
        return !this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAllRolesButGesuchstellerSozialdienst()
        );
    }

    public getGesuchModel(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
    }

    public getAllActiveGesuchsperioden(): Array<TSGesuchsperiode> {
        return this.yetUnusedGesuchsperiodenListe;
    }

    public setSelectedGesuchsperiode(): void {
        const gesuchsperiodeList = this.getAllActiveGesuchsperioden();
        const found = gesuchsperiodeList.find(
            gp => gp.id === this.gesuchsperiodeId
        );
        if (found) {
            this.getGesuchModel().gesuchsperiode = found;
        }
    }

    public isGesuchsperiodeActive(): boolean {
        if (this.gesuchModelManager.getGesuchsperiode()) {
            return (
                TSGesuchsperiodeStatus.AKTIV ===
                    this.gesuchModelManager.getGesuchsperiode().status ||
                TSGesuchsperiodeStatus.INAKTIV ===
                    this.gesuchModelManager.getGesuchsperiode().status
            );
        }
        return true;
    }

    public getTitle(): string {
        if (
            !this.gesuchModelManager.getGesuch() ||
            !this.gesuchModelManager.isGesuch()
        ) {
            return this.$translate.instant('ART_DER_MUTATION');
        }
        if (
            this.gesuchModelManager.isGesuchSaved() &&
            this.gesuchModelManager.getGesuchsperiode()
        ) {
            const k =
                this.gesuchModelManager.getGesuch().typ ===
                TSAntragTyp.ERNEUERUNGSGESUCH
                    ? 'KITAX_ERNEUERUNGSGESUCH_PERIODE'
                    : 'KITAX_ERSTGESUCH_PERIODE';
            return this.$translate.instant(k, {
                periode:
                    this.gesuchModelManager.getGesuchsperiode()
                        .gesuchsperiodeString
            });
        }
        const key =
            this.gesuchModelManager.getGesuch().typ ===
            TSAntragTyp.ERNEUERUNGSGESUCH
                ? 'KITAX_ERNEUERUNGSGESUCH'
                : 'KITAX_ERSTGESUCH';
        return this.$translate.instant(key);
    }

    public getNextButtonText(): string {
        if (this.gesuchModelManager.getGesuch()) {
            if (this.gesuchModelManager.getGesuch().isNew()) {
                return this.$translate.instant('ERSTELLEN');
            }
            if (
                this.gesuchModelManager.isGesuchReadonly() ||
                this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getGesuchstellerOnlyRoles()
                ) ||
                this.isSozialdienstAndOk()
            ) {
                return this.$translate.instant('WEITER_ONLY');
            }
        }
        return this.$translate.instant('WEITER');
    }

    /**
     * Checks if logged in benutzer is in Sozialdienst role and if yes, check if status of GESUCH_ERSTELLEN
     * step is ok (e.g. document is uploaded).
     */
    private isSozialdienstAndOk(): boolean {
        const sozialdienstRole = this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle()
        );
        return (
            sozialdienstRole &&
            this.wizardStepManager.isStepStatusOk(
                TSWizardStepName.GESUCH_ERSTELLEN
            )
        );
    }

    public isSelectedGesuchsperiodeInaktiv(): boolean {
        return (
            this.getGesuchModel() &&
            this.getGesuchModel().gesuchsperiode &&
            this.getGesuchModel().gesuchsperiode.status ===
                TSGesuchsperiodeStatus.INAKTIV &&
            this.getGesuchModel().isNew()
        );
    }

    public canChangeGesuchsperiode(): boolean {
        return (
            this.gesuchModelManager.getGesuch() &&
            this.gesuchModelManager.isGesuch() &&
            this.isGesuchsperiodeActive() &&
            this.gesuchModelManager.getGesuch().isNew()
        );
    }

    public getGemeinde(): TSGemeinde {
        if (this.gesuchModelManager.getDossier()) {
            return this.gesuchModelManager.getDossier().gemeinde;
        }
        return undefined;
    }

    public getPeriodString(): string {
        if (this.getGemeinde()) {
            return MomentUtil.calculatePeriodenStartdatumString(
                this.getGemeinde().betreuungsgutscheineStartdatum
            );
        }
        return undefined;
    }

    /**
     * Diese Methode prueft ob das Form valid ist. Sollte es nicht valid sein wird das erste fehlende Element gesucht
     * und fokusiert, damit der Benutzer nicht scrollen muss, um den Fehler zu finden.
     * Am Ende wird this.form.$valid zurueckgegeben
     */
    public isGesuchValid(): boolean {
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
        }

        return this.form.valid;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public getAllRolesButGesuchstellerSozialdienst(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesButGesuchstellerSozialdienst();
    }

    public getGesuchstellerOnlyRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getGesuchstellerOnlyRoles();
    }

    public isShowInputBegruendungMutation(): boolean {
        return (
            this.isBegruendungMutationActiv &&
            this.getGesuchModel().isMutation()
        );
    }
}
