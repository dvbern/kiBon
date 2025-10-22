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
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AbstractStatusUpdater {

	private GesuchService gesuchService;

	protected Persistence persistence;

	@Inject
	protected AbstractStatusUpdater(
		GesuchService gesuchService,
		Persistence persistence
	) {
		this.gesuchService = gesuchService;
		this.persistence = persistence;
	}

	public WizardStepStatus getWizardStepStatusOkOrMutiert(
		WizardStep wizardStep
	) {
		if (AntragTyp.MUTATION != wizardStep.getGesuch().getTyp()) {
			// just to avoid doing the calculation for Gesuche that are not of Type Mutation if it is not needed
			return WizardStepStatus.OK;
		}

		final List<AbstractMutableEntity> newObjects =
			getStepRelatedObjects(
				wizardStep.getGesuch()
			);
		Objects.requireNonNull(wizardStep.getGesuch().getVorgaengerId());
		Optional<Gesuch> vorgaengerGesuch =
			this.gesuchService.findGesuch(
				wizardStep.getGesuch().getVorgaengerId(),
				false
			);
		if (!vorgaengerGesuch.isPresent()) {
			throw new EbeguEntityNotFoundException(
				"getWizardStepStatusOkOrMutiert",
				ErrorCodeEnum.ERROR_VORGAENGER_MISSING,
				"Vorgaenger Gesuch fuer Mutation nicht gefunden"
			);
		}
		final List<AbstractMutableEntity> vorgaengerObjects =
			getStepRelatedObjects(
				vorgaengerGesuch.get()
			);
		boolean isMutiert = isObjectMutiert(newObjects, vorgaengerObjects);
		if (AntragTyp.MUTATION == wizardStep.getGesuch().getTyp()
			&& isMutiert) {
			return WizardStepStatus.MUTIERT;
		}
		return WizardStepStatus.OK;
	}

	/**
	 * Returns all Objects that are related to the given Step. For instance for the Step GESUCHSTELLER it returns
	 * the object Gesuchsteller1 and Gesuchsteller2. These objects can then be used to check for changes.
	 */
	protected abstract List<AbstractMutableEntity> getStepRelatedObjects(
		@NotNull Gesuch gesuch
	);

	protected abstract void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	);

	/**
	 * Returns true when given list have different sizes. If not, it checks whether the content of each object
	 * of the list newEntities is the same as it was in the list oldEntities. Any change will make the method return
	 * true
	 */
	protected boolean isObjectMutiert(
		@NotNull List<AbstractMutableEntity> newEntities,
		@NotNull List<AbstractMutableEntity> oldEntities
	) {
		if (oldEntities.size() != newEntities.size()) {
			return true;
		}
		for (AbstractMutableEntity newEntity : newEntities) {
			if (newEntity != null && newEntity.getVorgaengerId() == null) {
				return true; // if there is no vorgaenger it must have changed
			}
			if (newEntity != null && newEntity.getVorgaengerId() != null) {
				final AbstractEntity vorgaengerEntity =
					persistence.find(
						newEntity.getClass(),
						newEntity.getVorgaengerId()
					);
				if (vorgaengerEntity == null
					|| !newEntity.isSame(vorgaengerEntity)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setWizardStepOkOrMutiert(@NotNull WizardStep wizardStep) {
		wizardStep.setWizardStepStatus(
			getWizardStepStatusOkOrMutiert(wizardStep)
		);
	}

	/**
	 * Der Step mit dem uebergebenen StepName bekommt den Status OK. Diese Methode wird immer aufgerufen, um den
	 * Status vom aktualisierten
	 * Objekt auf OK zu setzen
	 */
	protected static void updateStepThatChanged(
		List<WizardStep> wizardSteps,
		WizardStepName stepName
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (wizardStep.getWizardStepName() == stepName) {
				wizardStep.setWizardStepStatus(WizardStepStatus.OK);
			}
		}
	}
}
