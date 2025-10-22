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

import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class WizardStepStatusUpdaterFamiliensituation extends
	AbstractStatusUpdater {

	protected WizardStepStatusUpdaterFamiliensituation(
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
	}

	public static void setVerguegbarAndNOK(WizardStep wizardStep) {
		wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		wizardStep.setVerfuegbar(true);
	}
}
