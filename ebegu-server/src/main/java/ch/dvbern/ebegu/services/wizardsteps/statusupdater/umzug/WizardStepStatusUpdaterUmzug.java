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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.umzug;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.util.Constants;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterUmzug extends AbstractStatusUpdater {

	@Inject
	public WizardStepStatusUpdaterUmzug(
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		addRelatedObjectsForUmzug(
			gesuch.getGesuchsteller1(),
			relatedObjects
		);
		addRelatedObjectsForUmzug(
			gesuch.getGesuchsteller2(),
			relatedObjects
		);
		return relatedObjects;
	}

	/**
	 * Adds all Adressen of the given Gesuchsteller that are set as umzug
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addRelatedObjectsForUmzug(
		@Nullable GesuchstellerContainer gesuchsteller,
		List<AbstractMutableEntity> relatedObjects
	) {
		if (gesuchsteller != null) {
			for (GesuchstellerAdresseContainer adresse : gesuchsteller
				.getAdressen()) {
				if (!adresse.extractIsKorrespondenzAdresse()
					&& !adresse.extractIsRechnungsAdresse()
					&& !adresse.getGesuchstellerAdresseJA()
						.getGueltigkeit()
						.getGueltigAb()
						.isEqual(Constants.START_OF_TIME)) { // only the first Adresse starts at START_OF_TIME
					relatedObjects.add(adresse);
				}
			}
		}
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.UMZUG == wizardStep.getWizardStepName()) {
				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}
}
