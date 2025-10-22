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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.abwesenheit.WizardStepStatusUpdaterAbwesenheit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.betreuung.WizardStepStatusUpdaterBetreuung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.dokumente.WizardStepStatusUpdaterDokumente;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.familiensituation.WizardStepStatusUpdaterFamiliensituation;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation.WizardStepStatusUpdaterFinSit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.gesuchsteller.WizardStepStatusUpdaterGesuchsteller;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.kinder.WizardStepStatusUpdaterKinder;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.umzug.WizardStepStatusUpdaterUmzug;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.verfuegen.WizardStepStatusUpdaterVerfuegen;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdater {

	private WizardStepStatusUpdaterFamiliensituation wizardStepStatusUpdaterFamiliensituation;
	private WizardStepStatusUpdaterBetreuung wizardStepStatusUpdaterBetreuung;
	private WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum;
	private WizardStepStatusUpdaterKinder wizardStepStatusUpdaterKinder;
	private WizardStepStatusUpdaterDokumente wizardStepStatusUpdaterDokumente;
	private WizardStepStatusUpdaterVerfuegen wizardStepStatusUpdaterVerfuegen;
	private WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit;
	private WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller;
	private WizardStepStatusUpdaterUmzug wizardStepStatusUpdaterUmzug;
	private WizardStepStatusUpdaterAbwesenheit wizardStepStatusUpdaterAbwesenheit;
	private WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung;

	@Inject
	public WizardStepStatusUpdater(
		WizardStepStatusUpdaterFamiliensituation wizardStepStatusUpdaterFamiliensituation,
		@NotNull WizardStepStatusUpdaterBetreuung wizardStepStatusUpdaterBetreuung,
		WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum,
		WizardStepStatusUpdaterKinder wizardStepStatusUpdaterKinder,
		WizardStepStatusUpdaterDokumente wizardStepStatusUpdaterDokumente,
		WizardStepStatusUpdaterVerfuegen wizardStepStatusUpdaterVerfuegen,
		WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit,
		WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller,
		WizardStepStatusUpdaterUmzug wizardStepStatusUpdaterUmzug,
		WizardStepStatusUpdaterAbwesenheit wizardStepStatusUpdaterAbwesenheit,
		WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung
	) {
		this.wizardStepStatusUpdaterFamiliensituation =
			wizardStepStatusUpdaterFamiliensituation;
		this.wizardStepStatusUpdaterBetreuung =
			wizardStepStatusUpdaterBetreuung;
		this.wizardStepStatusUpdaterErwerbspensum =
			wizardStepStatusUpdaterErwerbspensum;
		this.wizardStepStatusUpdaterKinder = wizardStepStatusUpdaterKinder;
		this.wizardStepStatusUpdaterDokumente =
			wizardStepStatusUpdaterDokumente;
		this.wizardStepStatusUpdaterVerfuegen =
			wizardStepStatusUpdaterVerfuegen;
		this.wizardStepStatusUpdaterFinSit = wizardStepStatusUpdaterFinSit;
		this.wizardStepStatusUpdaterGesuchsteller =
			wizardStepStatusUpdaterGesuchsteller;
		this.wizardStepStatusUpdaterUmzug = wizardStepStatusUpdaterUmzug;
		this.wizardStepStatusUpdaterAbwesenheit =
			wizardStepStatusUpdaterAbwesenheit;
		this.wizardStepStatusUpdaterEinkommensverschlechterung =
			wizardStepStatusUpdaterEinkommensverschlechterung;
	}

	/**
	 * Hier wird es geschaut, was fuer ein Objekttyp aktualisiert wurde. Dann wird die entsprechende Logik
	 * durchgefuehrt, um zu
	 * wissen welche anderen Steps von diesen Aenderungen beeinflusst wurden. Mit dieser Information werden alle
	 * betroffenen
	 * Status dementsprechend geaendert. Dazu werden die Angaben in oldEntity mit denen in newEntity verglichen und dann
	 * wird
	 * entsprechend reagiert
	 */
	public void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		WizardStepName stepThatChanged,
		@Nullable Integer substep
	) {
		AbstractStatusUpdater statusUpdater = getStatusUpdater(stepThatChanged);
		if (statusUpdater != null) {
			statusUpdater.updateAllStatus(
				wizardSteps,
				oldEntity,
				newEntity,
				substep
			);
		} else {
			AbstractStatusUpdater.updateStepThatChanged(
				wizardSteps,
				stepThatChanged
			);
		}
	}

	public WizardStepStatus getWizardStepStatusOkOrMutiert(
		@Nonnull WizardStep wizardStep
	) {
		AbstractStatusUpdater statusUpdater = getStatusUpdater(
			wizardStep.getWizardStepName()
		);
		if (statusUpdater != null) {
			return statusUpdater.getWizardStepStatusOkOrMutiert(wizardStep);
		}
		return WizardStepStatus.OK;
	}

	private AbstractStatusUpdater getStatusUpdater(
		WizardStepName stepThatChanged
	) {
		if (WizardStepName.FAMILIENSITUATION == stepThatChanged
		) {
			return wizardStepStatusUpdaterFamiliensituation;
		}
		if (WizardStepName.GESUCHSTELLER == stepThatChanged) {
			return wizardStepStatusUpdaterGesuchsteller;
		}
		if (WizardStepName.UMZUG == stepThatChanged) {
			return wizardStepStatusUpdaterUmzug;
		}
		if (WizardStepName.BETREUUNG == stepThatChanged) {
			return wizardStepStatusUpdaterBetreuung;
		}
		if (WizardStepName.ABWESENHEIT == stepThatChanged) {
			return wizardStepStatusUpdaterAbwesenheit;
		}
		if (WizardStepName.KINDER == stepThatChanged) {
			return wizardStepStatusUpdaterKinder;
		}
		if (WizardStepName.ERWERBSPENSUM == stepThatChanged) {
			return wizardStepStatusUpdaterErwerbspensum;
		}
		if (stepThatChanged.isEKVWizardStepName()) {
			return wizardStepStatusUpdaterEinkommensverschlechterung;
		}
		if (WizardStepName.DOKUMENTE == stepThatChanged) {
			return wizardStepStatusUpdaterDokumente;
		}
		if (WizardStepName.VERFUEGEN == stepThatChanged) {
			return wizardStepStatusUpdaterVerfuegen;
		}
		if (stepThatChanged.isFinSitWizardStepName()) {
			return wizardStepStatusUpdaterFinSit;
		}
		return null;
	}

	public void updateFinSitStep(
		WizardStep wizardStep
	) {
		if (!wizardStep.getWizardStepName().isFinSitWizardStepName()) {
			throw new IllegalArgumentException(
				"Method should only be called for FinSit WizardStep"
			);
		}
		wizardStepStatusUpdaterFinSit.setStatusDueToFinSitRequired(
			wizardStep
		);
	}

	public void updateEKVStep(
		WizardStep wizardStep
	) {
		if (!wizardStep.getWizardStepName().isEKVWizardStepName()) {
			throw new IllegalArgumentException(
				"Method should only be called for EKV WizardStep"
			);
		}
		wizardStepStatusUpdaterFinSit.setStatusDueToFinSitRequired(
			wizardStep
		);
	}
}
