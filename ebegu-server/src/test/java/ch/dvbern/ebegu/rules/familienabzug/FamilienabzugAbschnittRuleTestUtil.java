package ch.dvbern.ebegu.rules.familienabzug;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.util.Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FamilienabzugAbschnittRuleTestUtil {

	public FamilienabzugAbschnittRuleTestUtil() {
	}

	protected static Map<EinstellungKey, Einstellung> getDefaultEinstellungMap() {
		Map<EinstellungKey, Einstellung> defaultEinstellungMap =
			new HashMap<>();
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse3 =
			new Einstellung(
				EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS,
				new Gesuchsperiode()
			);
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			einstellungPauschalabzugProPersonFamiliengroesse3
		);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse4 =
			new Einstellung(
				EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS,
				new Gesuchsperiode()
			);
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			einstellungPauschalabzugProPersonFamiliengroesse4
		);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse5 =
			new Einstellung(
				EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS,
				new Gesuchsperiode()
			);
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			einstellungPauschalabzugProPersonFamiliengroesse5
		);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse6 =
			new Einstellung(
				EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS,
				new Gesuchsperiode()
			);
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			einstellungPauschalabzugProPersonFamiliengroesse6
		);

		return defaultEinstellungMap;
	}

	protected static void assertEqualsNumberValue(
		double expected,
		@Nullable BigDecimal actual
	) {
		BigDecimal expectedBigDecimal = BigDecimal.valueOf(expected);
		assertEqualsNumberValue(expectedBigDecimal, actual);
	}

	protected static void assertEqualsNumberValue(
		BigDecimal expected,
		@Nullable BigDecimal actual
	) {
		assertNotNull(actual);
		assertEquals(
			expected.stripTrailingZeros(),
			actual.stripTrailingZeros()
		);
	}
}
