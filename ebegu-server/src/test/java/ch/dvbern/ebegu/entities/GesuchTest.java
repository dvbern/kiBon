package ch.dvbern.ebegu.entities;

import java.time.LocalDate;
import java.util.Collections;

import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.test.GesuchBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GesuchTest {

	@Test
	void familiennameNormalesGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(
			builder -> builder
				.withGesuchsteller1("Meier", "Thomas")
		);
		Assertions.assertEquals("Meier", gesuch.extractFamiliennamenString());
	}

	@Test
	void familiennameNormalesGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(
			builder -> builder
				.withGesuchsteller1("Meier", "Thomas")
				.withGesuchsteller2("Müller", "Anna")
		);
		Assertions.assertEquals(
			"Meier, Müller",
			gesuch.extractFamiliennamenString()
		);
	}

	@Test
	void familiennameSozialfallGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(
			builder -> builder
				.withoutGesuchsteller1()
				.withoutGesuchsteller2()
				.withSozialdienst("Hostettler", "Jonas", null, null)
		);
		Assertions.assertEquals(
			"Hostettler",
			gesuch.extractFamiliennamenString()
		);
	}

	@Test
	void familiennameSozialfallGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(
			builder -> builder
				.withoutGesuchsteller1()
				.withoutGesuchsteller2()
				.withSozialdienst(
					"Meier",
					"Thomas",
					"Schmied",
					"Katherina"
				)
		);
		Assertions.assertEquals(
			"Meier, Schmied",
			gesuch.extractFamiliennamenString()
		);
	}

	@Test
	void familiennameSozialfallGesuchZweiGesuchstellerAusgefuellt() {
		Gesuch gesuch = GesuchBuilder.create(
			builder -> builder
				.withSozialdienst(
					"Meier",
					"Thomas",
					"Schmied",
					"Katherina"
				)
				.withGesuchsteller1("Muster", "Thomas")
				.withGesuchsteller2("Müller", "Anna")
		);
		Assertions.assertEquals(
			"Muster, Müller",
			gesuch.extractFamiliennamenString()
		);
	}

	@Test
	void copyForMutation_WithBetreuunungsstatusNichtEingetreten_BetreuungsstatusIsNowWarten() {

		// test setup
		Gesuch gesuch = new Gesuch();
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.NICHT_EINGETRETEN);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setBetreuungen(Collections.singleton(betreuung));
		gesuch.setKindContainers(Collections.singleton(kindContainer));

		// mandatory references (but not test relevant)
		Fall fall = new Fall();
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		gesuch.setDossier(dossier);
		Kind kindJa = new Kind();
		kindJa.setGeburtsdatum(LocalDate.now());
		kindContainer.setKindJA(kindJa);

		// test
		Gesuch copy = gesuch.copyForMutation(
			new Gesuch(),
			Eingangsart.ONLINE,
			LocalDate.now(),
			0
		);

		// assert
		copy.getKindContainers().forEach(eachKindContainer -> {
			eachKindContainer.getBetreuungen()
				.forEach(
					eachBetreuung -> Assertions.assertEquals(
						Betreuungsstatus.WARTEN,
						eachBetreuung.getBetreuungsstatus()
					)
				);
		});
	}
}
