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

package ch.dvbern.ebegu.api.converter.gesuch;

import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.converter.AbstractBaseSonderConverter;
import ch.dvbern.ebegu.api.dtos.JaxWizardStep;
import ch.dvbern.ebegu.entities.WizardStep;

@Dependent
public class JaxWizardStepConverter extends AbstractBaseSonderConverter {

	public JaxWizardStep wizardStepToJAX(WizardStep wizardStep) {
		// OptimisticLocking: Version richtig behandeln
		flush();

		final JaxWizardStep jaxWizardStep =
			convertAbstractVorgaengerFieldsToJAX(
				wizardStep,
				new JaxWizardStep()
			);
		jaxWizardStep.setGesuchId(wizardStep.getGesuch().getId());
		jaxWizardStep.setVerfuegbar(wizardStep.getVerfuegbar());
		jaxWizardStep.setWizardStepName(wizardStep.getWizardStepName());
		jaxWizardStep.setWizardStepStatus(wizardStep.getWizardStepStatus());
		jaxWizardStep.setBemerkungen(wizardStep.getBemerkungen());
		return jaxWizardStep;
	}

	public WizardStep wizardStepToEntity(
		final JaxWizardStep jaxWizardStep,
		final WizardStep wizardStep
	) {
		convertAbstractVorgaengerFieldsToEntity(jaxWizardStep, wizardStep);
		wizardStep.setVerfuegbar(jaxWizardStep.isVerfuegbar());
		wizardStep.setWizardStepName(jaxWizardStep.getWizardStepName());
		wizardStep.setWizardStepStatus(jaxWizardStep.getWizardStepStatus());
		wizardStep.setBemerkungen(jaxWizardStep.getBemerkungen());

		// OptimisticLocking: Version richtig behandeln
		return checkVersionSaveAndFlush(wizardStep, jaxWizardStep.getVersion());
	}
}
