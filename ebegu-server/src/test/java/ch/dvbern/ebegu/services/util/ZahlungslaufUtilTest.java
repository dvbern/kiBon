package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.test.ZahlungsauftragBuilder;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZahlungslaufUtilTest {

	@Test
	public void isZahlunglaufRepetition() {
		Zahlungsauftrag first = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 15)
				)
				.withGueltigkeit(
					new DateRange(
						LocalDate.of(2022, Month.AUGUST, 1),
						LocalDate.of(2022, Month.AUGUST, 31)
					)
				)
		);
		assertFalse(
			ZahlungslaufUtil.isZahlunglaufRepetition(
				first.getGueltigkeit().getGueltigBis(),
				null
			),
			"Dies ist der erste Zahlungslauf ueberhaupt. Darf keine Repetition sein"
		);

		Zahlungsauftrag secondInAugust = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 16)
				)
				.withGueltigkeit(
					new DateRange(
						LocalDate.of(2022, Month.AUGUST, 1),
						LocalDate.of(2022, Month.AUGUST, 31)
					)
				)
		);
		assertTrue(
			ZahlungslaufUtil.isZahlunglaufRepetition(
				secondInAugust.getGueltigkeit().getGueltigBis(),
				first
			),
			"Repetition, gleicher Monat"
		);

		Zahlungsauftrag firstInSeptember = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.SEPTEMBER, 16)
				)
				.withGueltigkeit(
					new DateRange(
						LocalDate.of(2022, Month.SEPTEMBER, 1),
						LocalDate.of(2022, Month.SEPTEMBER, 30)
					)
				)
		);
		assertFalse(
			ZahlungslaufUtil.isZahlunglaufRepetition(
				firstInSeptember.getGueltigkeit().getGueltigBis(),
				secondInAugust
			),
			"Neuer Monat, erster Zahlungslauf"
		);
	}

	@Test
	public void ermittleZahlungslaufGueltigBis() {
		Zahlungsauftrag auftragAnfangMonat = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(LocalDate.of(2022, Month.AUGUST, 1))
		);
		Zahlungsauftrag auftragMitteMonat = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 15)
				)
		);
		Zahlungsauftrag auftragEndeMonat = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 31)
				)
		);
		assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragAnfangMonat,
				0
			)
		);
		assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragMitteMonat,
				0
			)
		);
		assertEquals(
			LocalDate.of(2022, Month.AUGUST, 31),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragEndeMonat,
				0
			)
		);
		assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragAnfangMonat,
				1
			)
		);
		assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragMitteMonat,
				1
			)
		);
		assertEquals(
			LocalDate.of(2022, Month.SEPTEMBER, 30),
			ZahlungslaufUtil.ermittleZahlungslaufGueltigBis(
				auftragEndeMonat,
				1
			)
		);
	}

	@Test
	public void ermittleZahlungslaufGueltigVon() {
		LocalDate gueltigBisFirst = LocalDate.of(2022, Month.AUGUST, 31);
		LocalDate expectedGueltigVonFirst = Constants.START_OF_DATETIME
			.toLocalDate();
		assertEquals(
			expectedGueltigVonFirst,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(
				gueltigBisFirst,
				null
			),
			"Erster Zahlungslauf ist ab Beginn der Zeit gültig"
		);
		Zahlungsauftrag first = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 15)
				)
				.withGueltigkeit(
					new DateRange(
						expectedGueltigVonFirst,
						gueltigBisFirst
					)
				)
		);

		LocalDate gueltigBisSecondInAugust = LocalDate.of(
			2022,
			Month.AUGUST,
			31
		);
		LocalDate expectedGueltigVonSecondInAugust = LocalDate.of(
			2022,
			Month.AUGUST,
			1
		);
		assertEquals(
			expectedGueltigVonSecondInAugust,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(
				gueltigBisSecondInAugust,
				first
			),
			"Repetition, Beginn des (letzter) Monat der Gueltigkeit des letzten Zahlungslaufs"
		);
		Zahlungsauftrag secondInAugust = ZahlungsauftragBuilder.create(
			builder -> builder
				.withDatumGeneriert(
					LocalDate.of(2022, Month.AUGUST, 16)
				)
				.withGueltigkeit(
					new DateRange(
						expectedGueltigVonSecondInAugust,
						gueltigBisSecondInAugust
					)
				)
		);

		LocalDate gueltigBis = LocalDate.of(2022, Month.SEPTEMBER, 30);
		LocalDate expectedGueltigVon = LocalDate.of(2022, Month.SEPTEMBER, 1);
		assertEquals(
			expectedGueltigVon,
			ZahlungslaufUtil.ermittleZahlungslaufGueltigVon(
				gueltigBis,
				secondInAugust
			),
			"Repetition, Beginn des (letzter) Monat der Gueltigkeit des letzten Zahlungslaufs"
		);
	}
}
