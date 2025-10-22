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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ErwerbspensumAbschnittRuleTest {

	private Gesuch gesuch;
	private Familiensituation familiensituation;

	private AbstractPlatz platz;

	private ErwerbspensumAsivAbschnittRule erwerbspensumAsivAbschnittRule =
		new ErwerbspensumAsivAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			1,
			Locale.GERMAN
		);

	private LocalDate famSitAenderungDatum;

	@BeforeEach
	void setUp() {
		// define the Gueltigkeit dynamically
		famSitAenderungDatum = LocalDate.now();
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(
			LocalDate.now().withDayOfMonth(1).minusMonths(1)
		);
		gueltigkeit.setGueltigBis(LocalDate.now().plusDays(30));

		// Setup Erwerbspensum
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setPensum(80);
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setGueltigkeit(
			new DateRange(Constants.DEFAULT_GUELTIGKEIT)
		);
		ErwerbspensumContainer erwerbspensumContainer =
			new ErwerbspensumContainer();
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		Set<ErwerbspensumContainer> erwerbspensen = new HashSet<>();
		erwerbspensen.add(erwerbspensumContainer);

		// Setup Gesuch
		gesuch = new Gesuch();
		gesuch.setDossier(new Dossier());
		gesuch.getDossier().setFall(new Fall());
		gesuch.getFall().setMandant(new Mandant());
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setErwerbspensenContainers(erwerbspensen);
		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		gesuch.getGesuchsteller2().setErwerbspensenContainers(erwerbspensen);
		gesuch.setTyp(AntragTyp.MUTATION);

		// Setup familiensituation
		familiensituation = new Familiensituation();
		familiensituation.setAenderungPer(famSitAenderungDatum);
		familiensituation.setGesuchstellerKardinalitaet(
			EnumGesuchstellerKardinalitaet.ZU_ZWEIT
		);

		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer()
			.setFamiliensituationJA(familiensituation);

		Familiensituation familiensituationErstGesuch = new Familiensituation();
		familiensituationErstGesuch.setGesuchstellerKardinalitaet(
			EnumGesuchstellerKardinalitaet.ALLEINE
		);
		gesuch.getFamiliensituationContainer()
			.setFamiliensituationErstgesuch(familiensituationErstGesuch);

		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(gueltigkeit);
		gesuch.setGesuchsperiode(gesuchsperiode);

		platz = new Betreuung();
		platz.setKind(new KindContainer());
		platz.getKind().setGesuch(gesuch);
	}

	@ParameterizedTest
	@EnumSource(
		value = EnumFamilienstatus.class,
		names = { "SCHWYZ", "ALLEINERZIEHEND", "APPENZELL" },
		mode = Mode.EXCLUDE
	)
	void testErwerbspensumAbschnittRule_FamSitAenderungPerDatumFrist(
		EnumFamilienstatus enumFamilienstatus
	) {
		gesuch.getFall()
			.getMandant()
			.setMandantIdentifier(MandantIdentifier.BERN);
		// Test specific expectations
		familiensituation.setFamilienstatus(enumFamilienstatus);
		gesuch.setRegelnGueltigAb(famSitAenderungDatum.minusMonths(2));

		List<VerfuegungZeitabschnitt> result = erwerbspensumAsivAbschnittRule
			.createVerfuegungsZeitabschnitte(platz);

		// Assert that the GS2 Ewerbspensen Abschnitt start at the beginning of the month after the FamSit Aenderung per Datum
		// The two first entry are the GS1 and the EWP Zuschlag Abschnitt, the third one is the GS2 Abschnitt setted according the
		// FamSit Frist and the last one is the EWP Zuschlag for GS2
		assertThat(
			famSitAenderungDatum.plusMonths(1).withDayOfMonth(1),
			is(result.get(2).getGueltigkeit().getGueltigAb())
		);
	}

	@Test
	void testErwerbspensumAbschnittRuleSchwyz_EinreicheDatumFrist() {

		gesuch.getFall()
			.getMandant()
			.setMandantIdentifier(MandantIdentifier.SCHWYZ);
		// Test specific expectations
		familiensituation.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		LocalDate einreicheDatum = famSitAenderungDatum.minusMonths(2);
		gesuch.setRegelnGueltigAb(einreicheDatum);

		List<VerfuegungZeitabschnitt> result = erwerbspensumAsivAbschnittRule
			.createVerfuegungsZeitabschnitte(platz);

		// Assert that the GS2 Abschnitt start at the beginning of the month after the Einreichedatum
		// The two first entry are the GS1 and the EWP Zuschlag Abschnitt, the third one is the GS2 Abschnitt setted according the
		// FamSit Frist and the last one is the EWP Zuschlag for GS2
		assertThat(
			einreicheDatum.plusMonths(1).withDayOfMonth(1),
			is(result.get(2).getGueltigkeit().getGueltigAb())
		);
	}
}
