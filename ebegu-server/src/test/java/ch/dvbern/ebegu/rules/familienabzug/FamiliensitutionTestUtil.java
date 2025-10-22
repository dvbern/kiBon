package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.time.Month;
import java.util.stream.Stream;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import org.junit.jupiter.params.provider.Arguments;

import static ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet.ALLEINE;
import static ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet.ZU_ZWEIT;
import static ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG;
import static ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
import static ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;

public class FamiliensitutionTestUtil {

	public FamiliensitutionTestUtil() {
	}

	protected static Stream<Arguments> getAllFamiliensituationsWithTwoGesuchstellerBernFKJV() {
		return Stream.of(
			getVerheiratet(),
			getKonkubinat(),
			getKonkubinatOhneKindOverMinDauer(),
			getKonkubinatOhneKindUnderMinDauerZuZweit(),
			getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNein(),
			getAlleinerziehendZuZweit(),
			getAlleinerziehendUnterhaltsvereinbarungNein()
		);
	}

	protected static Stream<Arguments> getAllFamiliensituationsWithOneGesuchstellerBernFKJV() {
		return Stream.of(
			getKonkubinatOhneKindUnderMinDauerAlleine(),
			getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungJa(),
			getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNichtmoeglich(),
			getAlleinerziehendAlleine(),
			getAlleinerziehendUnterhaltsvereinbarungNichtmoeglich(),
			getAlleinerziehendUnterhaltsvereinbarungJa()
		);
	}

	protected static Arguments getVerheiratet() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinat() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindOverMinDauer() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2012, Month.JULY, 1));
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindUnderMinDauerZuZweit() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2022, Month.JULY, 1));
		familiensituation.setGeteilteObhut(true);
		familiensituation.setGesuchstellerKardinalitaet(ZU_ZWEIT);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindUnderMinDauerAlleine() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2022, Month.JULY, 1));
		familiensituation.setGeteilteObhut(true);
		familiensituation.setGesuchstellerKardinalitaet(ALLEINE);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungJa() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2022, Month.JULY, 1));
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(JA_UNTERHALTSVEREINBARUNG);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNein() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2022, Month.JULY, 1));
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(
			NEIN_UNTERHALTSVEREINBARUNG
		);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNichtmoeglich() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(
			EnumFamilienstatus.KONKUBINAT_KEIN_KIND
		);
		familiensituation.setStartKonkubinat(LocalDate.of(2022, Month.JULY, 1));
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(
			UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH
		);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getAlleinerziehendZuZweit() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(true);
		familiensituation.setGesuchstellerKardinalitaet(ZU_ZWEIT);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getAlleinerziehendAlleine() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(true);
		familiensituation.setGesuchstellerKardinalitaet(ALLEINE);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getAlleinerziehendUnterhaltsvereinbarungJa() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(JA_UNTERHALTSVEREINBARUNG);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getAlleinerziehendUnterhaltsvereinbarungNein() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(
			NEIN_UNTERHALTSVEREINBARUNG
		);
		return Arguments.of(familiensituation);
	}

	protected static Arguments getAlleinerziehendUnterhaltsvereinbarungNichtmoeglich() {
		Familiensituation familiensituation = getFamiliensituationFKJV();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(
			UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH
		);
		return Arguments.of(familiensituation);
	}

	private static Familiensituation getFamiliensituationFKJV() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		familiensituation.setMinDauerKonkubinat(2);
		return familiensituation;
	}
}
