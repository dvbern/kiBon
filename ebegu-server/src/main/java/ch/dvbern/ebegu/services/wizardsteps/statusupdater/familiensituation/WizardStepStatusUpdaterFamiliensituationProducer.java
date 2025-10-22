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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.http.HttpServletRequest;

import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation.WizardStepStatusUpdaterFinSit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.gesuchsteller.WizardStepStatusUpdaterGesuchsteller;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

@ApplicationScoped
public class WizardStepStatusUpdaterFamiliensituationProducer {

	@Produces
	@RequestScoped
	public WizardStepStatusUpdaterFamiliensituation produceWizardStepStatusUpdater(
		KindService kindService,
		HttpServletRequest httpServletRequest,
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

		if (MandantCookieUtil.getMandantFromCookie(httpServletRequest)
			== MandantIdentifier.SCHWYZ) {
			return new SchwyzWizardStepStatusUpdaterFamiliensituation(
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
		return new SharedWizardStepStatusUpdaterFamiliensituation(
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
}
