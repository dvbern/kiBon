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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterEinkommensverschlechterung
	extends
	AbstractStatusUpdater {

	@Inject
	public WizardStepStatusUpdaterEinkommensverschlechterung(
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		if (gesuch == null) {
			return relatedObjects;
		}
		final EinkommensverschlechterungInfoContainer ekvInfo =
			gesuch.getEinkommensverschlechterungInfoContainer();
		if (ekvInfo == null) {
			return relatedObjects;
		}
		relatedObjects.add(ekvInfo);
		if (Boolean.TRUE.equals(
			ekvInfo.getEinkommensverschlechterungInfoJA()
				.getEinkommensverschlechterung()
		)) {
			if (gesuch.getGesuchsteller1() != null
				&& gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					!= null) {
				relatedObjects.add(
					gesuch.getGesuchsteller1()
						.getEinkommensverschlechterungContainer()
				);
			}
			if (gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2()
					.getEinkommensverschlechterungContainer()
					!= null) {
				relatedObjects.add(
					gesuch.getGesuchsteller2()
						.getEinkommensverschlechterungContainer()
				);
			}
		}
		return relatedObjects;
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		if ((oldEntity == null
			|| oldEntity instanceof EinkommensverschlechterungInfoContainer)
			&& (newEntity instanceof EinkommensverschlechterungInfoContainer
				||
				newEntity instanceof EinkommensverschlechterungContainer)) {
			if (newEntity instanceof EinkommensverschlechterungInfoContainer) {
				updateAllStatusForEinkommensverschlechterungInfoChange(
					wizardSteps,
					(EinkommensverschlechterungInfoContainer) oldEntity,
					(EinkommensverschlechterungInfoContainer) Objects
						.requireNonNull(newEntity)
				);
			} else {
				updateAllStatusForEinkommensverschlechterungChange(wizardSteps); // I think we never use this one
			}
		} else {
			WizardStepName.getEKVWizardSteps()
				.forEach(
					wizardStepName -> updateStepThatChanged(
						wizardSteps,
						wizardStepName
					)
				);
		}
	}

	/**
	 * Wenn die Seite schon besucht ist dann soll der Status auf ok/mutiert
	 * oder
	 * notOK (bei wechsel ekv von nein auf ja) gesetzt werden
	 */
	private void updateAllStatusForEinkommensverschlechterungInfoChange(
		List<WizardStep> wizardSteps,
		@Nullable EinkommensverschlechterungInfoContainer oldEntity,
		EinkommensverschlechterungInfoContainer newEntity
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()
				&& wizardStep.getWizardStepName().isEKVWizardStepName()) {

				if (Boolean.FALSE.equals(
					newEntity.getEinkommensverschlechterungInfoJA()
						.getEinkommensverschlechterung()
				)) {
					setWizardStepOkOrMutiert(wizardStep);
				} else if (oldEntity == null
					|| !oldEntity.getEinkommensverschlechterungInfoJA()
						.getEinkommensverschlechterung()
					|| (!oldEntity.getEinkommensverschlechterungInfoJA()
						.getEkvFuerBasisJahrPlus1()
						&& newEntity
							.getEinkommensverschlechterungInfoJA()
							.getEkvFuerBasisJahrPlus1())
					|| (!oldEntity.getEinkommensverschlechterungInfoJA()
						.getEkvFuerBasisJahrPlus2()
						&& newEntity
							.getEinkommensverschlechterungInfoJA()
							.getEkvFuerBasisJahrPlus2())) {
					// beim Wechseln von KEIN_EV auf EV oder von
					// KEIN_EV_FUER_BASISJAHR2 auf EV_FUER_BASISJAHR2
					wizardStep.setWizardStepStatus(WizardStepStatus.NOK);

				} else if (wizardStep.getGesuch().isMutation()
					&& WizardStepStatus.NOK
						!= wizardStep.getWizardStepStatus()) {
					setWizardStepOkOrMutiert(wizardStep);
				}
			}
		}
	}

	private void updateAllStatusForEinkommensverschlechterungChange(
		List<WizardStep> wizardSteps
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()
				&& WizardStepStatus.NOK != wizardStep.getWizardStepStatus()
				&& wizardStep.getWizardStepName().isEKVWizardStepName()
				&& wizardStep.getGesuch().isMutation()) {

				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}
}
