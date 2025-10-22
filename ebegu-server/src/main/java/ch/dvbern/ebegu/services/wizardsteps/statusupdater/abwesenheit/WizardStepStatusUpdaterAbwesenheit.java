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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.abwesenheit;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterAbwesenheit extends AbstractStatusUpdater {

	@Inject
	public WizardStepStatusUpdaterAbwesenheit(
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		return new ArrayList<>(gesuch.extractAllAbwesenheiten());
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.ABWESENHEIT == wizardStep.getWizardStepName()) {
				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}
}
