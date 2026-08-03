/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
    OnDestroy,
    OnInit,
    inject
} from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    ValidationErrors,
    Validators
} from '@angular/forms';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {MatDatepicker} from '@angular/material/datepicker';
import {MatDialog} from '@angular/material/dialog';
import {CONSTANTS} from '@models/constants';
import {TranslateService} from '@ngx-translate/core';
import {UIRouterGlobals} from '@uirouter/core';
import {LogFactory} from '@utils/log';
import {electronicFormatIBAN, isValidIBAN} from 'ibantools';
import moment from 'moment';
import {Moment} from 'moment';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSAdresse} from '../../../../models/entity/TSAdresse';
import {FerienbetreuungAngabenStatus} from '../../../../models/enums/FerienbetreuungAngabenStatus';
import {TSFerienbetreuungFormularStatus} from '../../../../models/enums/TSFerienbetreuungFormularStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSFerienbetreuungAngabenStammdaten} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenStammdaten';
import {TSBfsGemeinde} from '../../../../models/TSBfsGemeinde';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {UnsavedChangesService} from '../../services/unsaved-changes.service';
import {AbstractFerienbetreuungFormular} from '../abstract.ferienbetreuung-formular';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';
import {map} from 'rxjs/operators';
import {FerienbetreuungPermissionUtil} from '../util/FerienbetreuungPermissionUtil';

const LOG = LogFactory.createLog('FerienbetreuungStammdatenGemeindeComponent');

export const MY_FORMATS = {
    parse: {
        dateInput: 'MM/YYYY'
    },
    display: {
        dateInput: 'MM/YYYY',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'LL',
        monthYearA11yLabel: 'MMMM YYYY'
    }
};

@Component({
    selector: 'dv-ferienbetreuung-stammdaten-gemeinde',
    templateUrl: './ferienbetreuung-stammdaten-gemeinde.component.html',
    styleUrls: ['./ferienbetreuung-stammdaten-gemeinde.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [{provide: MAT_DATE_FORMATS, useValue: MY_FORMATS}],
    standalone: false
})
export class FerienbetreuungStammdatenGemeindeComponent
    extends AbstractFerienbetreuungFormular
    implements OnInit, OnDestroy
{
    protected readonly errorService: ErrorService;
    protected readonly translate: TranslateService;
    protected readonly cd: ChangeDetectorRef;
    protected readonly dialog: MatDialog;
    protected readonly uiRouterGlobals: UIRouterGlobals;
    protected readonly wizardRS: WizardStepXRS;
    protected readonly CONSTANTS = CONSTANTS;
    protected readonly TSRole = TSRole;
    private readonly ferienbetreuungService = inject(FerienbetreuungService);
    private readonly fb = inject(FormBuilder);
    private readonly gemeindeRS = inject(GemeindeRS);
    private readonly authServiceRS = inject(AuthServiceRS);
    private readonly unsavedChangesService = inject(UnsavedChangesService);

    public bfsGemeinden: TSBfsGemeinde[];

    private stammdaten: TSFerienbetreuungAngabenStammdaten;
    private subscription: Subscription;

    public date?: moment.Moment;

    public form = this.fb.group({
        traegerschaft: [<null | string>null],
        amAngebotBeteiligteGemeinden: [<null | string[]>null],
        seitWannFerienbetreuungen: [<null | Moment>null],
        stammdatenAdresse: this.fb.group({
            organisation: [<null | string>null],
            zusatzzeile: [<null | string>null],
            strasse: [<null | string>null],
            hausnummer: [<null | string>null],
            plz: [<null | string>null],
            ort: [<null | string>null]
        }),
        stammdatenKontaktpersonVorname: [<null | string>null],
        stammdatenKontaktpersonNachname: [<null | string>null],
        stammdatenKontaktpersonFunktion: [<null | string>null],
        stammdatenKontaktpersonTelefon: [<null | string>null],
        stammdatenKontaktpersonEmail: [<null | string>null],
        auszahlungsdaten: this.fb.group({
            kontoinhaber: [<null | string>null],
            adresseKontoinhaber: this.fb.group({
                strasse: [<null | string>null],
                hausnummer: [<null | string>null],
                ort: [<null | string>null],
                plz: [<null | string>null],
                zusatzzeile: [<null | string>null]
            }),
            iban: [
                <null | string>null,
                (control: AbstractControl): ValidationErrors | null => {
                    const value = control.value;
                    if (EbeguUtil.isEmptyStringNullOrUndefined(value)) {
                        return null;
                    }
                    return isValidIBAN(electronicFormatIBAN(value))
                        ? null
                        : {iban: true};
                }
            ],
            vermerkAuszahlung: [<null | string>null]
        })
    });

    public constructor() {
        const errorService = inject(ErrorService);
        const translate = inject(TranslateService);
        const cd = inject(ChangeDetectorRef);
        const dialog = inject(MatDialog);
        const uiRouterGlobals = inject(UIRouterGlobals);
        const wizardRS = inject(WizardStepXRS);

        super(errorService, translate, dialog, cd, wizardRS, uiRouterGlobals);

        this.errorService = errorService;
        this.translate = translate;
        this.cd = cd;
        this.dialog = dialog;
        this.uiRouterGlobals = uiRouterGlobals;
        this.wizardRS = wizardRS;
    }

    public ngOnInit(): void {
        this.subscription = combineLatest([
            this.ferienbetreuungService.getFerienbetreuungContainer(),
            this.authServiceRS.principal$,
            this.ferienbetreuungService.getFerienbetreuungHistory()
        ]).subscribe(
            ([container, principal, history]) => {
                this.container = container;
                this.stammdaten =
                    container.isAtLeastInPruefungKantonOrZurueckgegeben()
                        ? container.angabenKorrektur?.stammdaten
                        : container.angabenDeklaration?.stammdaten;
                this.setupFormAndPermissions(
                    container,
                    this.stammdaten,
                    principal,
                    history
                );
                this.unsavedChangesService.registerForm(this.form);
            },
            error => {
                LOG.error(error);
            }
        );
        this.gemeindeRS.getAllBfsGemeinden().then(gemeinden => {
            this.bfsGemeinden = gemeinden;
            this.bfsGemeinden.sort((a, b) => a.name.localeCompare(b.name));
            this.cd.markForCheck();
        });
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    protected setupForm(stammdaten: TSFerienbetreuungAngabenStammdaten): void {
        if (!stammdaten) {
            return;
        }
        this.form.patchValue({
            amAngebotBeteiligteGemeinden:
                stammdaten?.amAngebotBeteiligteGemeinden,
            seitWannFerienbetreuungen: stammdaten?.seitWannFerienbetreuungen,
            traegerschaft: stammdaten?.traegerschaft,
            stammdatenKontaktpersonEmail:
                stammdaten?.stammdatenKontaktpersonEmail,
            stammdatenKontaktpersonNachname:
                stammdaten?.stammdatenKontaktpersonNachname,
            stammdatenKontaktpersonVorname:
                stammdaten?.stammdatenKontaktpersonVorname,
            stammdatenKontaktpersonTelefon:
                stammdaten?.stammdatenKontaktpersonTelefon,
            stammdatenKontaktpersonFunktion:
                stammdaten?.stammdatenKontaktpersonFunktion,
            auszahlungsdaten: {
                kontoinhaber: stammdaten?.kontoinhaber || '',
                vermerkAuszahlung: stammdaten?.vermerkAuszahlung,
                iban: stammdaten?.iban || '',
                adresseKontoinhaber: {
                    strasse: stammdaten?.adresseKontoinhaber?.strasse || '',
                    plz: stammdaten?.adresseKontoinhaber?.plz || '',
                    hausnummer: stammdaten?.adresseKontoinhaber?.hausnummer,
                    zusatzzeile: stammdaten?.adresseKontoinhaber?.zusatzzeile,
                    ort: stammdaten?.adresseKontoinhaber?.ort || ''
                }
            },
            stammdatenAdresse: {
                strasse: stammdaten?.stammdatenAdresse?.strasse || '',
                plz: stammdaten?.stammdatenAdresse?.plz || '',
                hausnummer: stammdaten?.stammdatenAdresse?.hausnummer,
                zusatzzeile: stammdaten?.stammdatenAdresse?.zusatzzeile,
                ort: stammdaten?.stammdatenAdresse?.ort || '',
                organisation: stammdaten?.stammdatenAdresse?.organisation
            }
        });
        this.setBasicValidation();
    }

    protected setBasicValidation(): void {
        this.removeAllValidators();

        this.form.controls.stammdatenKontaktpersonTelefon.setValidators(
            Validators.pattern(CONSTANTS.PATTERN_PHONE)
        );
        this.form.controls.stammdatenKontaktpersonEmail.setValidators(
            Validators.email
        );

        this.enableStammdatenAuszahlungValidation();
        this.triggerFormValidation();
    }

    protected enableFormValidation(): void {
        this.form.controls.stammdatenAdresse.controls.strasse.setValidators(
            Validators.required
        );
        this.form.controls.stammdatenAdresse.controls.ort.setValidators(
            Validators.required
        );
        this.form.controls.stammdatenAdresse.controls.plz.setValidators(
            Validators.required
        );
        this.form.controls.stammdatenAdresse.controls.organisation.setValidators(
            Validators.required
        );

        this.form.controls.stammdatenKontaktpersonVorname.setValidators([
            Validators.required
        ]);
        this.form.controls.stammdatenKontaktpersonNachname.setValidators([
            Validators.required
        ]);
        this.form.controls.stammdatenKontaktpersonTelefon.setValidators([
            Validators.required,
            Validators.pattern(CONSTANTS.PATTERN_PHONE)
        ]);
        this.form.controls.stammdatenKontaktpersonEmail.setValidators([
            Validators.required,
            Validators.email
        ]);

        this.form.controls.auszahlungsdaten.controls.kontoinhaber.setValidators(
            Validators.required
        );
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.strasse.setValidators(
            Validators.required
        );
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.plz.setValidators(
            Validators.required
        );
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.ort.setValidators(
            Validators.required
        );
        this.form.controls.auszahlungsdaten.controls.iban.setValidators([
            Validators.required,
            (control: AbstractControl): ValidationErrors | null => {
                const value = control.value;
                if (EbeguUtil.isEmptyStringNullOrUndefined(value)) {
                    return null;
                }
                return isValidIBAN(electronicFormatIBAN(value))
                    ? null
                    : {iban: true};
            }
        ]);

        this.form.controls.stammdatenAdresse.controls.strasse.updateValueAndValidity();
        this.form.controls.stammdatenAdresse.controls.ort.updateValueAndValidity();
        this.form.controls.stammdatenAdresse.controls.plz.updateValueAndValidity();
        this.form.controls.stammdatenAdresse.controls.organisation.updateValueAndValidity();
        this.form.controls.auszahlungsdaten.controls.kontoinhaber.updateValueAndValidity();
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.strasse.updateValueAndValidity();
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.plz.updateValueAndValidity();
        this.form.controls.auszahlungsdaten.controls.adresseKontoinhaber.controls.ort.updateValueAndValidity();
        this.form.controls.auszahlungsdaten.controls.iban.updateValueAndValidity();
        this.enableStammdatenAuszahlungValidation();
    }

    public save(): void {
        this.formAbschliessenTriggered = false;
        this.setBasicValidation();

        if (!this.form.valid) {
            this.showValidierungFehlgeschlagenErrorMessage();
            return;
        }
        this.ferienbetreuungService
            .saveStammdaten(this.container.id, this.extractFormValues())
            .subscribe(
                () => {
                    this.formValidationTriggered = false;
                    this.ferienbetreuungService.updateFerienbetreuungContainerStores(
                        this.container.id
                    );
                    this.errorService.clearAll();
                    this.errorService.addMesageAsInfo(
                        this.translate.instant('SPEICHERN_ERFOLGREICH')
                    );
                },
                err => this.handleSaveErrors(err)
            );
    }

    private extractFormValues(): TSFerienbetreuungAngabenStammdaten {
        this.stammdaten.amAngebotBeteiligteGemeinden =
            this.form.value.amAngebotBeteiligteGemeinden;
        this.stammdaten.seitWannFerienbetreuungen =
            this.form.value.seitWannFerienbetreuungen;
        this.stammdaten.traegerschaft = this.form.value.traegerschaft;

        const adresse = new TSAdresse().from(this.form.value.stammdatenAdresse);
        adresse.id = this.stammdaten.stammdatenAdresse?.id;
        adresse.version = this.stammdaten.stammdatenAdresse?.version;

        this.stammdaten.stammdatenAdresse = adresse;
        this.stammdaten.stammdatenKontaktpersonVorname =
            this.form.value.stammdatenKontaktpersonVorname;
        this.stammdaten.stammdatenKontaktpersonNachname =
            this.form.value.stammdatenKontaktpersonNachname;
        this.stammdaten.stammdatenKontaktpersonFunktion =
            this.form.value.stammdatenKontaktpersonFunktion;
        this.stammdaten.stammdatenKontaktpersonTelefon =
            this.form.value.stammdatenKontaktpersonTelefon;
        this.stammdaten.stammdatenKontaktpersonEmail =
            this.form.value.stammdatenKontaktpersonEmail;
        this.stammdaten.iban =
            this.form.controls.auszahlungsdaten.value.iban?.toUpperCase();
        this.stammdaten.kontoinhaber =
            this.form.value.auszahlungsdaten.kontoinhaber;

        const adresseKontoinhaber = new TSAdresse().from(
            this.form.value.auszahlungsdaten.adresseKontoinhaber
        );
        adresseKontoinhaber.id = this.stammdaten.adresseKontoinhaber?.id;
        adresseKontoinhaber.version =
            this.stammdaten.adresseKontoinhaber?.version;
        this.stammdaten.adresseKontoinhaber = adresseKontoinhaber;

        this.stammdaten.vermerkAuszahlung =
            this.form.value.auszahlungsdaten.vermerkAuszahlung;
        return this.stammdaten;
    }

    public fillAdress(): void {
        const gemeinde = this.container.gemeinde;
        this.gemeindeRS.getGemeindeStammdaten(gemeinde.id).then(
            stammdaten => {
                const adresse = stammdaten.extractTsAdresse();
                this.form.controls.stammdatenAdresse.controls.organisation.setValue(
                    adresse?.organisation
                );
                this.form.controls.stammdatenAdresse.controls.strasse.setValue(
                    adresse?.strasse
                );
                this.form.controls.stammdatenAdresse.controls.hausnummer.setValue(
                    adresse?.hausnummer
                );
                this.form.controls.stammdatenAdresse.controls.plz.setValue(
                    adresse?.plz
                );
                this.form.controls.stammdatenAdresse.controls.ort.setValue(
                    adresse.ort
                );
            },
            err => {
                this.errorService.addMesageAsError(
                    this.translate.instant(
                        'FERIENBETREUUNG_FEHLER_ABRUF_INFORMATIONEN'
                    )
                );
                LOG.error(err);
            }
        );
    }

    public fillBenutzer(): void {
        const benutzer = this.authServiceRS.getPrincipal();
        this.form.controls.stammdatenKontaktpersonVorname.setValue(
            benutzer.vorname
        );
        this.form.controls.stammdatenKontaktpersonNachname.setValue(
            benutzer.nachname
        );
        this.form.controls.stammdatenKontaktpersonEmail.setValue(
            benutzer.email
        );
    }

    public async onAbschliessen(): Promise<void> {
        if (await this.checkReadyForAbschliessen()) {
            this.ferienbetreuungService
                .stammdatenAbschliessen(
                    this.container.id,
                    this.extractFormValues()
                )
                .subscribe(
                    () => this.handleSaveSuccess(),
                    error => this.handleSaveErrors(error)
                );
        }
    }

    public onFalscheAngaben(): void {
        this.ferienbetreuungService
            .falscheAngabenStammdaten(
                this.container.id,
                this.extractFormValues()
            )
            .subscribe(
                () => this.handleSaveSuccess(),
                error => this.handleSaveErrors(error)
            );
    }

    private enableStammdatenAuszahlungValidation(): void {
        this.form.controls.stammdatenAdresse.markAllAsTouched();
        this.form.controls.stammdatenAdresse.controls.organisation.markAllAsTouched();
        this.form.controls.auszahlungsdaten.markAllAsTouched();

        this.triggerFormValidation();
    }

    public fillActionsVisible(): Observable<boolean> {
        return this.isZweitPruefungAndSameUserAsPruefung().pipe(
            map(
                isSame =>
                    !isSame &&
                    this.stammdaten?.status ===
                        TSFerienbetreuungFormularStatus.IN_BEARBEITUNG
            )
        );
    }

    public chosenYearHandler(normalizedYear: moment.Moment): void {
        const control = this.form.controls.seitWannFerienbetreuungen;
        const ctrlValue = control.value || moment();
        ctrlValue.year(normalizedYear.year());
        control.setValue(ctrlValue);
    }

    public chosenMonthHandler(
        normalizedMonth: moment.Moment,
        datepicker: MatDatepicker<moment.Moment>
    ): void {
        const control = this.form.controls.seitWannFerienbetreuungen;
        const ctrlValue = control.value || moment();
        ctrlValue.month(normalizedMonth.month());
        control.setValue(ctrlValue.startOf('month'));
        datepicker.close();
    }

    public isZweitPruefungAndSameUserAsPruefung() {
        return combineLatest([
            this.authServiceRS.principal$,
            this.ferienbetreuungService.getFerienbetreuungHistory()
        ]).pipe(
            map(([principal, history]) =>
                FerienbetreuungPermissionUtil.isInZweitpruefungAndSameUser(
                    principal,
                    this.container,
                    history
                )
            )
        );
    }

    public isInBearbeitungGemeindeAndKantonUser(): boolean {
        return !(
            this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantOnlyRoles()) &&
            this.container.status ===
                FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE
        );
    }
}
