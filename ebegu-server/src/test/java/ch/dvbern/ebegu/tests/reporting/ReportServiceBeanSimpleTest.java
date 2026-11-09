/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.reporting;

import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportServiceBeanSimpleTest {

	@Test
	public void testGesuchStichtagQueryAssumptions() {
		assertEquals(
			"IN_BEARBEITUNG_JA",
			AntragStatus.IN_BEARBEITUNG_JA.name()
		);
		assertEquals(
			"FREIGABEQUITTUNG",
			AntragStatus.FREIGABEQUITTUNG.name()
		);
		assertEquals(
			"BESCHWERDE_HAENGIG",
			AntragStatus.BESCHWERDE_HAENGIG.name()
		);
		assertEquals(
			"PRUEFUNG_STV",
			AntragStatus.PRUEFUNG_STV.name()
		);
		assertEquals(
			"GEPRUEFT_STV",
			AntragStatus.GEPRUEFT_STV.name()
		);
		assertEquals(
			"TAGESSCHULE",
			BetreuungsangebotTyp.TAGESSCHULE.name()
		);
	}

	@Test
	public void testGesuchZeitraumQueryAssumptions() {
		assertEquals("ONLINE", Eingangsart.ONLINE.name());
		assertEquals("PAPIER", Eingangsart.PAPIER.name());

		assertEquals("ERSTGESUCH", AntragTyp.ERSTGESUCH.name());
		assertEquals(
			"ERNEUERUNGSGESUCH",
			AntragTyp.ERNEUERUNGSGESUCH.name()
		);
		assertEquals("MUTATION", AntragTyp.MUTATION.name());

		assertEquals(
			"ABWESENHEIT",
			WizardStepName.ABWESENHEIT.name()
		);
		assertEquals("BETREUUNG", WizardStepName.BETREUUNG.name());
		assertEquals("DOKUMENTE", WizardStepName.DOKUMENTE.name());
		assertEquals(
			"EINKOMMENSVERSCHLECHTERUNG",
			WizardStepName.EINKOMMENSVERSCHLECHTERUNG.name()
		);
		assertEquals(
			"ERWERBSPENSUM",
			WizardStepName.ERWERBSPENSUM.name()
		);
		assertEquals(
			"FAMILIENSITUATION",
			WizardStepName.FAMILIENSITUATION.name()
		);
		assertEquals(
			"FINANZIELLE_SITUATION",
			WizardStepName.FINANZIELLE_SITUATION.name()
		);
		assertEquals("FREIGABE", WizardStepName.FREIGABE.name());
		assertEquals(
			"GESUCH_ERSTELLEN",
			WizardStepName.GESUCH_ERSTELLEN.name()
		);
		assertEquals(
			"GESUCHSTELLER",
			WizardStepName.GESUCHSTELLER.name()
		);
		assertEquals("KINDER", WizardStepName.KINDER.name());
		assertEquals("UMZUG", WizardStepName.UMZUG.name());
		assertEquals("VERFUEGEN", WizardStepName.VERFUEGEN.name());

		assertEquals("MUTIERT", WizardStepStatus.MUTIERT.name());

		assertEquals(
			"TAGESSCHULE",
			BetreuungsangebotTyp.TAGESSCHULE.name()
		);

		assertEquals("VERFUEGT", AntragStatus.VERFUEGT.name());
		assertEquals(
			"NUR_SCHULAMT",
			AntragStatus.NUR_SCHULAMT.name()
		);
		assertEquals(
			"BESCHWERDE_HAENGIG",
			AntragStatus.BESCHWERDE_HAENGIG.name()
		);
		assertEquals(
			"PRUEFUNG_STV",
			AntragStatus.PRUEFUNG_STV.name()
		);
		assertEquals(
			"IN_BEARBEITUNG_STV",
			AntragStatus.IN_BEARBEITUNG_STV.name()
		);
		assertEquals(
			"GEPRUEFT_STV",
			AntragStatus.GEPRUEFT_STV.name()
		);
		assertEquals(
			"KEIN_ANGEBOT",
			AntragStatus.KEIN_ANGEBOT.name()
		);
	}
}
