package ch.dvbern.ebegu.rules.familienabzug;

import java.util.HashMap;
import java.util.Map;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleTestUtil.getDefaultEinstellungMap;

class FamilienCalcRuleTest {

	private final FamilienabzugCalcRuleASIV famabzugCalcRule =
		new FamilienabzugCalcRuleASIV(
			getEinstellungMapForAsiv(),
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	@Test
	void testCalculateAbzugAufgrundFamiliengroesse_EBEGU_1185_NR32_Familiengroesse_Berechnung() {

		/* Beispiel Nr. 1:
		 * 1 Erwachsene Person (Alleinerziehend) und 1 Kind zu 50% in den Steuern abzugsberechtigt. Die Anzahl der Personen,
		 * die im Haushalt wohnen, beträgt zwei, die anrechenbare Familiengrösse ist 1,5. Es ist damit kein Abzug möglich,
		 * da 2-Personenhaushalt. Daher Fr. 0.00 in der Berechnung.
		 */
		Assertions.assertEquals(
			0,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(1.5, 2)
				.intValue()
		);

		/* Beispiel Nr. 2:
		 * 1 Erwachsene Person (Alleinerziehend) und 2 Kindern zu je 50% Abzugsmöglichkeit in den Steuern. Die Anzahl der
		 * Personen,
		 * die im gleichen Haushalt wohnen, beträgt somit 3 Personen und es wird nun der Ansatz 3-Personenhaushalt von
		 * Fr. 3'800.00 angenommen. Die anrechenbare Familiengrösse ist 2 und dieser Wert wird mit dem Ansatz von
		 * 3-Personenhaushalt
		 * von Fr. 3'800.00 multipliziert; Ergebnis Fr. 7'600.00
		 */
		Assertions.assertEquals(
			7600,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(2, 3)
				.intValue()
		);

		/* Beispiel Nr. 3:
		 * 1 Erwachsene Person (Alleinerziehend) mit 3 Kindern, für die Kinder ist je 50% Kinderabzug möglich. Es sind insgesamt
		 * 4 Personen im gleichen Haushalt wohnhaft, somit wird die Pauschale einer 4-Personenhaushalt von Fr. 5'960.00 genommen.
		 * Die anrechenbare Familiengrösse beträgt 2,5 und dieser Wert wird mit der Pauschale 4-Personenhaushalt von
		 * Fr. 6'000.00 multipliziert; Ergebnis Fr. 15'000.00.
		 */
		Assertions.assertEquals(
			15000,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(2.5, 4)
				.intValue()
		);

		/*
		 * Beispiel Nr. 4:
		 * 1 Erwachsene Person (Alleinerziehend) mit 4 Kindern, für das erste Kind ist kein Abzug in der Steuererklärung möglich,
		 * für das zweite Kind ist 100% möglich und für das dritte Kind 50%. Insgesamt sind 3 Personen im gleichen Haushalt
		 * wohnhaft. Deshalb wird die Pauschale 3-Personenhaushalt genommen
		 * (das erste Kind hat unter der Frage Kinderabzug "nein" stehen und zählt damit nicht dazu).
		 * Die anrechenbare Familiengrösse beträgt 2,5 und diese Familiengrösse wird mit der
		 * Pauschale 3-Personenhaushalt von Fr. 3'800.00 multipliziert; Ergebnis Fr. 9'500.00.
		 */
		Assertions.assertEquals(
			9500,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(2.5, 3)
				.intValue()
		);

		/*
		 * Beispiel Nr. 5:
		 * 2 Erwachsene Personen (Konkubinat) und 4 Kindern, zwei eigene Kinder sind je 100% abzugsberechtigt in der
		 * Steuererklärung und für zwei Kindern sind zu je 50% Abzug möglich. Insgesamt leben 6 Personen im gleichen Haushalt,
		 * es wird nun der Ansatz von 6 Personenhaushalt von Fr. 7'700.00 genommen. Die anrechenbare Familiengrösse von 4,5
		 * wird mit der Pauschale 6-Personenhaushalt von Fr. 7'700.00 multipliziert; Ergebnis Fr. 34'650.00.
		 */
		Assertions.assertEquals(
			34650,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(4.5, 6)
				.intValue()
		);

		/*
		 * Beispiel Nr. 6:
		 * 2 Erwachsene Personen (verheiratet) und 2 Kindern mit je 100% Abzugsmöglichkeit in den Steuern. Somit beträgt
		 * die Anzahl der Personen im gleichen Haushalt 4. Damit wird der Pauschalabzug von 4-Personenhaushalt angewendet.
		 * Die anrechenbare Familiengrösse 4 wird mit 4-Personhaushalt von Fr. 6000.00 multipliziert; Ergebnis Fr. 24000.00.
		 */
		Assertions.assertEquals(
			24000,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(4.0, 4)
				.intValue()
		);
	}

	@Test
	void testCalculateAbzugAufgrundFamiliengroesseZero() {
		Assertions.assertEquals(
			0,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(0, 0)
				.intValue()
		);
		Assertions.assertEquals(
			0,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(1, 1)
				.intValue()
		);
		Assertions.assertEquals(
			0,
			famabzugCalcRule.calculateAbzugAufgrundFamiliengroesse(1.5, 2)
				.intValue()
		);
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForAsiv() {
		Map<EinstellungKey, Einstellung> einstellungMapForAsiv =
			new HashMap<>(getDefaultEinstellungMap());
		Einstellung einstellungMinimalKonkubinat = new Einstellung(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			"5",
			new Gesuchsperiode()
		);
		einstellungMapForAsiv.put(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			einstellungMinimalKonkubinat
		);
		Einstellung einstellungKinderabzugTyp = new Einstellung(
			EinstellungKey.KINDERABZUG_TYP,
			KinderabzugTyp.ASIV.name(),
			new Gesuchsperiode()
		);
		einstellungMapForAsiv.put(
			EinstellungKey.KINDERABZUG_TYP,
			einstellungKinderabzugTyp
		);

		return einstellungMapForAsiv;
	}
}
