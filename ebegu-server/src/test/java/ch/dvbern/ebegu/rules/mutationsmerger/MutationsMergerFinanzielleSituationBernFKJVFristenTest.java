/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.test.TestDataUtil.PERIODE_JAHR_0;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;

public class MutationsMergerFinanzielleSituationBernFKJVFristenTest {

	//01.09.XXXX
	private static final LocalDate START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(1);
	//30.09.XXXX
	private static final LocalDate END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(2).minusDays(1);

	private static final LocalDate EINREICHEDATUM_NACH_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(5).plusDays(5);

	private static final MutationsMergerFinanzielleSituationBernFKJVFristen MUTATIONS_MERGER_FINANZIELLE_SITUATION_BERN_FKJV_FRISTEN =
		new MutationsMergerFinanzielleSituationBernFKJVFristen(
			Locale.GERMAN
		);

	private static final BigDecimal HUNDERT_TAUSEND = Objects.requireNonNull(
		MathUtil.DEFAULT.from(100000)
	);

	private static final BigDecimal ZWEI_HUNDERT_TAUSEND = Objects
		.requireNonNull(MathUtil.DEFAULT.from(200000));

	private static final BigDecimal THREE = BigDecimal.valueOf(3);
	private static final BigDecimal TWO = BigDecimal.valueOf(2);

	@Test
	void test_kleinereMassgegebeneseinkommens_Familiengroesse_aendert() {
		BGCalculationInput bgCalculationInput = initInputData(
			HUNDERT_TAUSEND,
			THREE
		);
		BGCalculationResult resultVorgaenger = initResultData(
			ZWEI_HUNDERT_TAUSEND,
			TWO
		);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_BERN_FKJV_FRISTEN
			.handleEinkommen(
				bgCalculationInput,
				resultVorgaenger,
				prepareBetreuung(
					HUNDERT_TAUSEND,
					EINREICHEDATUM_NACH_ABSCHNITT_ERSTGESUCH
				),
				EINREICHEDATUM_NACH_ABSCHNITT_ERSTGESUCH
			);
		Assertions.assertEquals(
			HUNDERT_TAUSEND,
			bgCalculationInput.getMassgebendesEinkommen()
		);
		Assertions.assertEquals(
			THREE,
			bgCalculationInput.getFamGroesseTotal()
		);
	}

	@Test
	void test_hoereMassgegebeneseinkommens_Familiengroesse_aendert() {
		BGCalculationInput bgCalculationInput = initInputData(
			ZWEI_HUNDERT_TAUSEND,
			THREE
		);
		BGCalculationResult resultVorgaenger = initResultData(
			HUNDERT_TAUSEND,
			TWO
		);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_BERN_FKJV_FRISTEN
			.handleEinkommen(
				bgCalculationInput,
				resultVorgaenger,
				prepareBetreuung(
					ZWEI_HUNDERT_TAUSEND,
					EINREICHEDATUM_NACH_ABSCHNITT_ERSTGESUCH
				),
				EINREICHEDATUM_NACH_ABSCHNITT_ERSTGESUCH
			);
		Assertions.assertEquals(
			ZWEI_HUNDERT_TAUSEND,
			bgCalculationInput.getMassgebendesEinkommen()
		);
		Assertions.assertEquals(
			THREE,
			bgCalculationInput.getFamGroesseTotal()
		);
	}

	private BGCalculationInput initInputData(
		BigDecimal massgegebendeseinkommen,
		BigDecimal famgroesse
	) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH,
				END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH
			)
		);
		BGCalculationInput input = new BGCalculationInput(
			verfuegungZeitabschnitt,
			RuleValidity.ASIV
		);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgegebendeseinkommen);
		input.setEinkommensjahr(PERIODE_JAHR_0);
		input.setFamGroesseTotal(famgroesse);
		input.setAbzugFamGroesseTotal(BigDecimal.ZERO);
		input.setSozialhilfeempfaenger(false);
		input.setVerguenstigungGewuenscht(true);
		return input;
	}

	private BGCalculationResult initResultData(
		BigDecimal massgegebendeseinkommen,
		BigDecimal famgroesse
	) {
		BGCalculationResult result = new BGCalculationResult();
		result.setMassgebendesEinkommenVorAbzugFamgr(massgegebendeseinkommen);
		result.setEinkommensjahr(PERIODE_JAHR_0);
		result.setFamGroesse(famgroesse);
		result.setAbzugFamGroesse(BigDecimal.ZERO);
		result.setSozialhilfeAkzeptiert(true);
		result.setVerguenstigungGewuenscht(true);
		return result;
	}

	private Betreuung prepareBetreuung(
		BigDecimal massgegebenesEinkommen,
		LocalDate mutationEingangsdatum
	) {
		Betreuung betreuung = new Betreuung();
		betreuung.setKind(new KindContainer());
		FinanzDatenDTO finanzDatenDTO = new FinanzDatenDTO(BigDecimal.ONE);
		finanzDatenDTO.setMassgebendesEinkBjVorAbzFamGr(massgegebenesEinkommen);
		Gesuch gesuch = new Gesuch();
		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTO);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer()
			.setFamiliensituationJA(new Familiensituation());
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.getGueltigkeit()
			.setGueltigAb(START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH);
		gesuch.setGesuchsperiode(gesuchsperiode);
		betreuung.setInstitutionStammdaten(new InstitutionStammdaten());
		betreuung.getInstitutionStammdaten()
			.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		betreuung.getKind().setGesuch(gesuch);
		betreuung.getKind().getGesuch().setEingangsdatum(mutationEingangsdatum);
		betreuung.initVorgaengerVerfuegungen(new Verfuegung(), null);
		return betreuung;
	}
}
