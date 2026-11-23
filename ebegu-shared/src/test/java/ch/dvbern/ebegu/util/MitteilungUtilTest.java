package ch.dvbern.ebegu.util;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static shadow.org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MitteilungUtilTest {

	private static final DateRange PERIODE_20_21 = new DateRange(
		LocalDate.of(2020, 8, 1),
		LocalDate.of(2021, 7, 31)
	);

	@ParameterizedTest(name = "Schliessdatum {0} -> betroffen: {1}")
	@CsvSource({
		// Schliessung Jahre nach Periodenende (Fall aus KIBONBE-304): nicht betroffen
		"2026-11-30, false",
		// Schliessung genau am Periodenende: Betreuung endet ohnehin regulaer
		"2021-07-31, false",
		// Schliessung waehrend der Periode
		"2021-03-31, true",
		"2020-08-01, true",
		// Schliessung vor Periodenbeginn
		"2020-07-31, true",
	})
	void isGesuchsperiodeVonSchliessungBetroffen(
		LocalDate schliessdatum,
		boolean expected
	) {
		assertThat(
			MitteilungUtil.isGesuchsperiodeVonSchliessungBetroffen(
				PERIODE_20_21,
				schliessdatum
			)
		).isEqualTo(expected);
	}

	@Test
	void shouldReturnFalse_whenPlainMitteilung() {
		Mitteilung mitteilung = new Mitteilung();

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}

	@Test
	void shouldReturnFalse_whenBetreuungsmitteilungAndFlagNotSet() {
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		mitteilung.setSchliessungMitteilung(false);

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}

	@Test
	void shouldReturnTrue_whenBetreuungsmitteilungAndFlagSet() {
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		mitteilung.setSchliessungMitteilung(true);

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isTrue();
	}

	@Test
	void shouldReturnFalse_whenSubclassOfMitteilungButNotBetreuungsmitteilung() {
		// e.g. NeueVeranlagungsMitteilung or any other Mitteilung subtype
		Mitteilung mitteilung = new NeueVeranlagungsMitteilung();

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}

	@Test
	void shouldReturnFalse_whenBetreuungenHasNoVerfuegung() {
		Gesuch gesuch = new Gesuch();
		KindContainer kindContainer = new KindContainer();
		kindContainer.getBetreuungen().add(new Betreuung());
		gesuch.addKindContainer(kindContainer);
		assertThat(MitteilungUtil.areZeitabschnittIgnorierend(gesuch))
			.isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = VerfuegungsZeitabschnittZahlungsstatus.class,
		names = { "IGNORIEREND",
			"IGNORIEREND_DEFINITIV" },
		mode = EnumSource.Mode.INCLUDE)
	void shouldReturnTrue_whenBetreuungenHasVerfuegungWithIgnorierendZahlungsstatusFuerInstitution(
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus verfuegungsZeitabschnittZahlungsstatus
	) {
		Gesuch gesuch = createGesuchWithZeitabschnitt(
			verfuegungsZeitabschnittZahlungsstatus,
			null
		);
		assertThat(MitteilungUtil.areZeitabschnittIgnorierend(gesuch)).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = VerfuegungsZeitabschnittZahlungsstatus.class,
		names = { "IGNORIEREND",
			"IGNORIEREND_DEFINITIV" },
		mode = EnumSource.Mode.INCLUDE)
	void shouldReturnTrue_whenBetreuungenHasVerfuegungWithIgnorierendZahlungsstatusFuerAntragstellende(
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus verfuegungsZeitabschnittZahlungsstatus
	) {
		Gesuch gesuch = createGesuchWithZeitabschnitt(
			null,
			verfuegungsZeitabschnittZahlungsstatus
		);

		assertThat(MitteilungUtil.areZeitabschnittIgnorierend(gesuch)).isTrue();

	}

	@ParameterizedTest
	@EnumSource(value = VerfuegungsZeitabschnittZahlungsstatus.class,
		names = { "IGNORIEREND",
			"IGNORIEREND_DEFINITIV" },
		mode = EnumSource.Mode.EXCLUDE)
	void shouldReturnFalse_whenBetreuungenHasVerfuegungWithoutIgnorierteZahlungsstatus(
		@Nonnull VerfuegungsZeitabschnittZahlungsstatus verfuegungsZeitabschnittZahlungsstatus
	) {
		Gesuch gesuch = createGesuchWithZeitabschnitt(
			verfuegungsZeitabschnittZahlungsstatus,
			verfuegungsZeitabschnittZahlungsstatus
		);
		assertThat(MitteilungUtil.areZeitabschnittIgnorierend(gesuch))
			.isFalse();
	}

	private Gesuch createGesuchWithZeitabschnitt(
		@Nullable VerfuegungsZeitabschnittZahlungsstatus institutionStatus,
		@Nullable VerfuegungsZeitabschnittZahlungsstatus antragstellerStatus
	) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		if (institutionStatus != null) {
			zeitabschnitt.setZahlungsstatusInstitution(institutionStatus);
		}
		if (antragstellerStatus != null) {
			zeitabschnitt.setZahlungsstatusAntragsteller(antragstellerStatus);
		}

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.getZeitabschnitte().add(zeitabschnitt);

		Betreuung betreuung = new Betreuung();
		betreuung.setVerfuegung(verfuegung);

		KindContainer kindContainer = new KindContainer();
		kindContainer.getBetreuungen().add(betreuung);

		Gesuch gesuch = new Gesuch();
		gesuch.addKindContainer(kindContainer);

		return gesuch;
	}
}
