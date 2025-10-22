package ch.dvbern.ebegu.abweichungen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class AbweichungInitializingUtilTest {

	private static final LocalDate AUG_1 = LocalDate.of(2023, 8, 1);
	private static final LocalDate AUG_31 = LocalDate.of(2023, 8, 31);
	private static final LocalDate SEP_1 = LocalDate.of(2023, 9, 1);
	private static final LocalDate SEP_10 = LocalDate.of(2023, 9, 10);
	private static final LocalDate SEP_7 = LocalDate.of(2023, 9, 7);
	private static final LocalDate SEP_8 = LocalDate.of(2023, 9, 8);
	private static final LocalDate SEP_11 = LocalDate.of(2023, 9, 11);
	private static final LocalDate SEP_15 = LocalDate.of(2023, 9, 15);
	private static final LocalDate SEP_30 = LocalDate.of(2023, 9, 30);
	private static final LocalDate JUL_31 = LocalDate.of(2024, 7, 31);
	private static final LocalDate NOV_30 = LocalDate.of(2023, 11, 30);
	private static final LocalDate AUG_15 = LocalDate.of(2023, 8, 15);
	private static final LocalDate AUG_16 = LocalDate.of(2023, 8, 16);

	@Nested
	class MZVTest {
		@Test
		void oneFullMonthPensum_whenFillAbweichungen_shouldHaveSameAnzahlAndTarifVertraglicheHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeiten = BigDecimal.valueOf(5);
			final BigDecimal tarifProHauptmahlzeit = BigDecimal.valueOf(10);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						AUG_1,
						AUG_31,
						anzahlHauptmahlzeiten,
						tarifProHauptmahlzeit,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungAug = abweichungen
				.get(0);
			assertThat(
				abweichungAug.getVertraglicheHauptmahlzeiten(),
				comparesEqualTo(anzahlHauptmahlzeiten)
			);
			assertThat(
				abweichungAug.getVertraglicherTarifHauptmahlzeit(),
				comparesEqualTo(tarifProHauptmahlzeit)
			);
		}

		@Test
		void oneFullMonthPensum_whenFillAbweichungen_shouldHaveSameAnzahlAndTarifVertraglicheNebenmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlNebenmahlzeiten = BigDecimal.valueOf(5);
			final BigDecimal tarifProNebenmahlzeit = BigDecimal.valueOf(10);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						AUG_1,
						AUG_31,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						anzahlNebenmahlzeiten,
						tarifProNebenmahlzeit
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungAug = abweichungen
				.get(0);
			assertThat(
				abweichungAug.getVertraglicheNebenmahlzeiten(),
				comparesEqualTo(anzahlNebenmahlzeiten)
			);
			assertThat(
				abweichungAug.getVertraglicherTarifNebenmahlzeit(),
				comparesEqualTo(tarifProNebenmahlzeit)
			);
		}

		@Test
		void halfMonthPensum_whenFillAbweichungen_shouldHaveAnteilOfMonthAnzahlVertraglichHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeiten = BigDecimal.valueOf(10);
			final BigDecimal anteilMonthOfAnzahlHauptmahlzeiten = BigDecimal
				.valueOf(5);
			final BigDecimal tarifProHauptmahlzeit = BigDecimal.valueOf(10);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_15,
						anzahlHauptmahlzeiten,
						tarifProHauptmahlzeit,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicheHauptmahlzeiten(),
				comparesEqualTo(anteilMonthOfAnzahlHauptmahlzeiten)
			);
		}

		@Test
		void halfMonthPensum_whenFillAbweichungen_shouldHaveAnteilOfMonthAnzahlVertraglicheNebenmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlNebenmahlzeiten = BigDecimal.valueOf(10);
			final BigDecimal tarifProNebenmahlzeit = BigDecimal.valueOf(10);
			final BigDecimal anteilMonthOfAnzahlNebenmahlzeiten = BigDecimal
				.valueOf(5);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_15,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						anzahlNebenmahlzeiten,
						tarifProNebenmahlzeit
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicheNebenmahlzeiten(),
				comparesEqualTo(anteilMonthOfAnzahlNebenmahlzeiten)
			);
		}

		@Test
		void halfMonthPensum_whenFillAbweichungen_shouldHaveSameVertraglicherTarifHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeiten = BigDecimal.valueOf(10);
			final BigDecimal tarifProHauptmahlzeit = BigDecimal.valueOf(10);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_15,
						anzahlHauptmahlzeiten,
						tarifProHauptmahlzeit,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifHauptmahlzeit(),
				comparesEqualTo(tarifProHauptmahlzeit)
			);
		}

		@Test
		void halfMonthPensum_whenFillAbweichungen_shouldHaveSameVertraglicherTarifNebenmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlNebenmahlzeiten = BigDecimal.valueOf(10);
			final BigDecimal tarifProNebenmahlzeit = BigDecimal.valueOf(10);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_15,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						anzahlNebenmahlzeiten,
						tarifProNebenmahlzeit
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifNebenmahlzeit(),
				comparesEqualTo(tarifProNebenmahlzeit)
			);
		}

		@Test
		void twoHalfMonthPensen_whenFillAbweichungen_shouldCalculateAverageTarifHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeitenFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal tarifProHauptmahlzeitFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal anzahlHauptmahlzeitenSecondHalf = BigDecimal
				.valueOf(5);
			final BigDecimal tarifProHauptmahlzeitSecondHalf = BigDecimal
				.valueOf(5);
			// Durchschnittlicher Tarif = (Total Kosten / Total Anzahl) = ((1/3) * 10 * 10 + (2/3) * 5 * 5) / ((1/3) * 10 + (2/3) * 5), gerundet
			// Total Kosten sind Anteil am Monat * Anzahl Mahlzeiten * Kosten
			// Total Anzahl sind Anteil am Monat * Anzahl Mahlzeiten
			final BigDecimal averageTarifHauptmahlzeiten = BigDecimal
				.valueOf(6.65);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_10,
						anzahlHauptmahlzeitenFirstHalf,
						tarifProHauptmahlzeitFirstHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_11,
						SEP_30,
						anzahlHauptmahlzeitenSecondHalf,
						tarifProHauptmahlzeitSecondHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifHauptmahlzeit(),
				comparesEqualTo(averageTarifHauptmahlzeiten)
			);
		}

		@Test
		void twoDifferentMonthPensen_whenFillAbweichungen_shouldCalculateAverageTarifHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeitenFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal tarifProHauptmahlzeitFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal anzahlHauptmahlzeitenSecondHalf = BigDecimal
				.valueOf(5);
			final BigDecimal tarifProHauptmahlzeitSecondHalf = BigDecimal
				.valueOf(5);
			// Durchschnittlicher Tarif = (Total Kosten / Total Anzahl) = ((7/30) * 10 * 10 + (23/30) * 5 * 5) / ((7/30) * 10 + (23/30) * 5), gerundet
			// Total Kosten sind Anteil am Monat * Anzahl Mahlzeiten * Kosten
			// Total Anzahl sind Anteil am Monat * Anzahl Mahlzeiten
			final BigDecimal averageTarifHauptmahlzeiten = BigDecimal
				.valueOf(6.15);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_7,
						anzahlHauptmahlzeitenFirstHalf,
						tarifProHauptmahlzeitFirstHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_8,
						SEP_30,
						anzahlHauptmahlzeitenSecondHalf,
						tarifProHauptmahlzeitSecondHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifHauptmahlzeit(),
				comparesEqualTo(averageTarifHauptmahlzeiten)
			);
		}

		@Test
		void twoDifferentMonthOverlappingMonthPensen_whenFillAbweichungen_shouldCalculateAverageTarifHauptmahlzeiten() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlHauptmahlzeitenFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal tarifProHauptmahlzeitFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal anzahlHauptmahlzeitenSecondHalf = BigDecimal
				.valueOf(5);
			final BigDecimal tarifProHauptmahlzeitSecondHalf = BigDecimal
				.valueOf(5);
			// Durchschnittlicher Tarif = (Total Kosten / Total Anzahl) = ((7/30) * 10 * 10 + (23/30) * 5 * 5) / ((7/30) * 10 + (23/30) * 5), gerundet
			// Total Kosten sind Anteil am Monat * Anzahl Mahlzeiten * Kosten
			// Total Anzahl sind Anteil am Monat * Anzahl Mahlzeiten
			final BigDecimal averageTarifHauptmahlzeiten = BigDecimal
				.valueOf(6.15);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						AUG_1,
						SEP_7,
						anzahlHauptmahlzeitenFirstHalf,
						tarifProHauptmahlzeitFirstHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_8,
						SEP_30,
						anzahlHauptmahlzeitenSecondHalf,
						tarifProHauptmahlzeitSecondHalf,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifHauptmahlzeit(),
				comparesEqualTo(averageTarifHauptmahlzeiten)
			);
		}

		@Test
		void twoHalfMonthPensen_whenFillAbweichungen_shouldCalculateAverageTarifNebenmahlzeit() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			final BigDecimal anzahlNebenmahlzeitenFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal tarifProNebenmahlzeitFirstHalf = BigDecimal
				.valueOf(10);
			final BigDecimal anzahlNebenmahlzeitenSecondHalf = BigDecimal
				.valueOf(5);
			final BigDecimal tarifProNebenmahlzeitSecondHalf = BigDecimal
				.valueOf(5);
			// Durchschnittlicher Tarif = (Total Kosten / Total Anzahl) = ((1/3) * 10 * 10 + (2/3) * 5 * 5) / ((1/3) * 10 + (2/3) * 5)
			// Total Kosten sind Anteil am Monat * Anzahl Mahlzeiten * Kosten
			// Total Anzahl sind Anteil am Monat * Anzahl Mahlzeiten
			final BigDecimal averageTarifNebenmahlzeit = BigDecimal.valueOf(
				6.65
			);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_1,
						SEP_10,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						anzahlNebenmahlzeitenFirstHalf,
						tarifProNebenmahlzeitFirstHalf
					)
				);
			betreuung.getBetreuungspensumContainers()
				.add(
					createBetreuungspensumWithMahlzeiten(
						pensum,
						SEP_11,
						SEP_30,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						anzahlNebenmahlzeitenSecondHalf,
						tarifProNebenmahlzeitSecondHalf
					)
				);
			List<BetreuungspensumAbweichung> abweichungen =
				AbweichungInitializingUtil
					.fillAbweichungen(BigDecimal.ONE, betreuung);
			final BetreuungspensumAbweichung abweichungSep = abweichungen
				.get(1);
			assertThat(
				abweichungSep.getVertraglicherTarifNebenmahlzeit(),
				comparesEqualTo(averageTarifNebenmahlzeit)
			);
		}
	}

	@Test
	void oneMultiMonthPensum_whenFillAbweichungen_shouldHaveAbweichungWithPensumForFullPensum() {
		final BigDecimal pensum = BigDecimal.valueOf(80);
		Betreuung betreuung = setupBetreuung();
		betreuung.getBetreuungspensumContainers()
			.add(createBetreuungspensum(pensum, AUG_1, NOV_30));

		List<BetreuungspensumAbweichung> abweichungen =
			AbweichungInitializingUtil.fillAbweichungen(
				BigDecimal.ONE,
				betreuung
			);
		final BetreuungspensumAbweichung abweichungAug = abweichungen.get(
			0
		);
		final BetreuungspensumAbweichung abweichungSep = abweichungen.get(
			1
		);
		final BetreuungspensumAbweichung abweichungOct = abweichungen.get(
			2
		);
		final BetreuungspensumAbweichung abweichungNov = abweichungen.get(
			3
		);
		final BetreuungspensumAbweichung abweichungDec = abweichungen.get(
			4
		);

		assertThat(abweichungen.size(), is(12));
		assertThat(
			abweichungAug.getVertraglichesPensum(),
			comparesEqualTo(pensum)
		);
		assertThat(
			abweichungSep.getVertraglichesPensum(),
			comparesEqualTo(pensum)
		);
		assertThat(
			abweichungOct.getVertraglichesPensum(),
			comparesEqualTo(pensum)
		);
		assertThat(
			abweichungNov.getVertraglichesPensum(),
			comparesEqualTo(pensum)
		);
		assertThat(abweichungDec.getVertraglichesPensum(), nullValue());
	}

	@Test
	void monthWithPartialPensum_whenFillAbweichungen_shouldHaveAbweichungWithPartialPensumForEntireFirstMonth() {
		final BigDecimal monthAnteil = DateUtil
			.calculateAnteilMonatInklWeekend(AUG_1, AUG_15);
		final BigDecimal pensum = BigDecimal.valueOf(80);
		Betreuung betreuung = setupBetreuung();
		betreuung.getBetreuungspensumContainers()
			.add(createBetreuungspensum(pensum, AUG_1, AUG_15));

		List<BetreuungspensumAbweichung> abweichungen =
			AbweichungInitializingUtil.fillAbweichungen(
				BigDecimal.ONE,
				betreuung
			);
		final BetreuungspensumAbweichung abweichungAug = abweichungen.get(
			0
		);
		final BetreuungspensumAbweichung abweichungSep = abweichungen.get(
			1
		);

		assertThat(abweichungen.size(), is(12));
		assertThat(
			abweichungAug.getVertraglichesPensum(),
			comparesEqualTo(pensum.multiply(monthAnteil))
		);
		assertThat(abweichungSep.getVertraglichesPensum(), nullValue());
	}

	@Test
	void monthWithMultiplePensen_whenFillAbweichungen_shouldHaveAbweichungWithAddedPensenForEntireFirstMonthOnly() {
		Betreuung betreuung = setupBetreuung();
		final BigDecimal pensum = BigDecimal.valueOf(80);
		betreuung.getBetreuungspensumContainers()
			.add(createBetreuungspensum(pensum, AUG_1, AUG_15));
		betreuung.getBetreuungspensumContainers()
			.add(createBetreuungspensum(pensum, AUG_16, NOV_30));

		List<BetreuungspensumAbweichung> abweichungen =
			AbweichungInitializingUtil.fillAbweichungen(
				BigDecimal.ONE,
				betreuung
			);
		final BetreuungspensumAbweichung abweichungAug = abweichungen.get(
			0
		);

		assertThat(abweichungen.size(), is(12));
		assertThat(
			abweichungAug.getVertraglichesPensum(),
			comparesEqualTo(pensum)
		);
	}

	@Nonnull
	private static Betreuung setupBetreuung() {
		final Betreuung betreuung = new Betreuung();
		betreuung.setKind(setupKind());
		betreuung.setInstitutionStammdaten(setupInstitutionStammdaten());
		return betreuung;

	}

	private static InstitutionStammdaten setupInstitutionStammdaten() {
		InstitutionStammdaten institutionStammdaten =
			new InstitutionStammdaten();
		institutionStammdaten.setBetreuungsangebotTyp(
			BetreuungsangebotTyp.KITA
		);

		return institutionStammdaten;
	}

	private static BetreuungspensumContainer createBetreuungspensum(
		BigDecimal pensum,
		LocalDate von,
		LocalDate bis
	) {
		Betreuungspensum betreuungspensum = new Betreuungspensum(
			new DateRange(von, bis)
		);
		betreuungspensum.setPensum(pensum);

		BetreuungspensumContainer betreuungspensumContainer =
			new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);

		return betreuungspensumContainer;
	}

	private static BetreuungspensumContainer createBetreuungspensumWithMahlzeiten(
		BigDecimal pensum,
		LocalDate von,
		LocalDate bis,
		BigDecimal anzahlHauptmahlzeiten,
		BigDecimal tarifProHauptmahlzeit,
		BigDecimal anzahlNebenmahlzeiten,
		BigDecimal tarifProNebenmahlzeit
	) {
		BetreuungspensumContainer container = createBetreuungspensum(
			pensum,
			von,
			bis
		);
		Betreuungspensum betreuungspensum = container.getBetreuungspensumJA();
		betreuungspensum.setMonatlicheHauptmahlzeiten(anzahlHauptmahlzeiten);
		betreuungspensum.setTarifProHauptmahlzeit(tarifProHauptmahlzeit);
		betreuungspensum.setMonatlicheNebenmahlzeiten(anzahlNebenmahlzeiten);
		betreuungspensum.setTarifProNebenmahlzeit(tarifProNebenmahlzeit);
		return container;
	}

	private static KindContainer setupKind() {
		KindContainer kindContainer = new KindContainer();
		Gesuch gesuch = new Gesuch();
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(AUG_1, JUL_31));

		gesuch.setGesuchsperiode(gesuchsperiode);
		kindContainer.setGesuch(gesuch);

		return kindContainer;
	}
}
