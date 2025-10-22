/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.familiensituation;

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.finanziellesituation.validation.FinanzielleSituationValidatorSZ;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation.WizardStepStatusUpdaterFinSit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.gesuchsteller.WizardStepStatusUpdaterGesuchsteller;

public class SchwyzWizardStepStatusUpdaterFamiliensituation extends
	SharedWizardStepStatusUpdaterFamiliensituation {

	public SchwyzWizardStepStatusUpdaterFamiliensituation(
		KindService kindService,
		ErwerbspensumService erwerbspensumService,
		EinstellungService einstellungService,
		WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum,
		FinanzielleSituationValidationService finanzielleSituationValidationService,
		GesuchService gesuchService,
		Persistence persistence,
		WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller,
		WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit,
		WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung
	) {
		super(
			kindService,
			erwerbspensumService,
			einstellungService,
			wizardStepStatusUpdaterErwerbspensum,
			finanzielleSituationValidationService,
			gesuchService,
			persistence,
			wizardStepStatusUpdaterGesuchsteller,
			wizardStepStatusUpdaterFinSit,
			wizardStepStatusUpdaterEinkommensverschlechterung
		);
	}

	@Override
	protected void updateStatusForFamiliensituationChange(
		@Nonnull WizardStep wizardStep,
		Familiensituation oldFamiliensituation,
		Familiensituation newFamiliensituation
	) {
		switch (wizardStep.getWizardStepName()) {
		case FAMILIENSITUATION:
			setWizardStepOkOrMutiert(wizardStep);
			break;
		case KINDER:
			updateKinderStepStatusOnFamSitChange(
				wizardStep,
				oldFamiliensituation,
				newFamiliensituation
			);
			break;
		case ERWERBSPENSUM:
			getWizardStepStatusUpdaterErwerbspensum()
				.updateErwerbspensumStepStatus(wizardStep, true);
			break;
		case GESUCHSTELLER:
			if (areGSComplete(wizardStep.getGesuch())) {
				getWizardStepStatusUpdaterGesuchsteller()
					.setWizardStepOkOrMutiert(wizardStep);
			} else {
				setVerguegbarAndNOK(wizardStep);
			}
			break;
		case FINANZIELLE_SITUATION_SCHWYZ:
		case EINKOMMENSVERSCHLECHTERUNG_SCHWYZ:
			if (isFinSitOfGesuchComplete(wizardStep.getGesuch())) {
				if (newFamiliensituation.getGesuchstellerKardinalitaet()
					!= oldFamiliensituation.getGesuchstellerKardinalitaet()
					|| wizardStep.getWizardStepStatus()
						.equals(
							WizardStepStatus.NOK
						)) {
					this.getWizardStepStatusUpdaterForFinSitOrEKV(wizardStep)
						.setWizardStepOkOrMutiert(wizardStep);
				}
				break;
			}
			setVerguegbarAndNOK(wizardStep);
			break;
		default:
			//noop
		}
	}

	private AbstractStatusUpdater getWizardStepStatusUpdaterForFinSitOrEKV(
		WizardStep wizardStep
	) {
		if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
			return this.getWizardStepStatusUpdaterFinSit();
		}
		return this.getWizardStepStatusUpdaterEinkommensverschlechterung();
	}

	protected void updateKinderStepStatusOnFamSitChange(
		WizardStep wizardStepKinder,
		@Nonnull Familiensituation oldFamiliensituation,
		@Nonnull Familiensituation newFamiliensituation
	) {
		//Falls bereits ein GS2 exisitiert müssen die Wizardsteps beim Wechsel von ein GS auf zwei GS nicht updated
		// werden
		if (wizardStepKinder.getGesuch().getGesuchsteller2() != null) {
			return;
		}
		super.updateKinderStepStatusOnFamSitChange(
			wizardStepKinder,
			oldFamiliensituation,
			newFamiliensituation
		);
	}

	private boolean areGSComplete(Gesuch gesuch) {
		if (getFamiliensituation(gesuch).getGesuchstellerKardinalitaet()
			== EnumGesuchstellerKardinalitaet.ZU_ZWEIT) {
			return gesuch.getGesuchsteller1() != null
				&& gesuch.getGesuchsteller2() != null;
		}
		return gesuch.getGesuchsteller1() != null;
	}

	@Nonnull
	private static Familiensituation getFamiliensituation(Gesuch gesuch) {
		return Objects.requireNonNull(gesuch.extractFamiliensituation());
	}

	private boolean isFinSitOfGesuchComplete(Gesuch gesuch) {
		final FinanzielleSituationValidatorSZ validator =
			new FinanzielleSituationValidatorSZ();
		if (gesuch.getGesuchsteller1() == null
			|| gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
				== null) {
			return false;
		}
		if (getFamiliensituation(gesuch).hasSecondGesuchsteller(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
		)) {
			if (gesuch.getGesuchsteller2() == null
				|| getFamiliensituation(gesuch)
					.getGemeinsameSteuererklaerung()
					== null
				|| gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					== null) {
				return false;
			}
			if (Boolean.FALSE.equals(
				getFamiliensituation(gesuch).getGemeinsameSteuererklaerung()
			)) {
				return validator.isFinanzielleSituationComplete(
					getFinanzielleSituationJA(gesuch.getGesuchsteller2()),
					gesuch
				)
					&& validator.isFinanzielleSituationComplete(
						getFinanzielleSituationJA(
							gesuch.getGesuchsteller1()
						),
						gesuch
					);
			}
			return Boolean.TRUE.equals(
				getFamiliensituation(gesuch).getGemeinsameSteuererklaerung()
			)
				&& validator.isFinanzielleSituationComplete(
					getFinanzielleSituationJA(
						gesuch.getGesuchsteller1()
					),
					gesuch
				);
		}
		return validator.isFinanzielleSituationComplete(
			getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
			gesuch
		);
	}

	private static FinanzielleSituation getFinanzielleSituationJA(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return Objects.requireNonNull(
			gesuchstellerContainer
				.getFinanzielleSituationContainer()
		)
			.getFinanzielleSituationJA();
	}

}
