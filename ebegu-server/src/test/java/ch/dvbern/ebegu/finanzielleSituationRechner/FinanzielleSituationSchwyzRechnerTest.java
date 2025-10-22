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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getFinanzielleSituationContainer;
import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getFinanzielleSituationJA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FinanzielleSituationSchwyzRechnerTest {

	private final FinanzielleSituationSchwyzRechner finanzielleSituationSchwyzRechner =
		new FinanzielleSituationSchwyzRechner();

	@Nested
	class SingleGSTest {

		@Nested
		class EKVTest {

			@Nested
			class CalculationTest {
				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void calculateForNichtQuellenBesteuerteTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(10000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP1VorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP2VorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 200'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenAnGrenzeTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						new BigDecimal(200000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP1VorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP2VorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 250'000 - 200'000, 10% = + 5'000
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 67'000
				 */
				@Test
				void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(250000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP1VorAbzFamGr(),
						is(BigDecimal.valueOf(67000))
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP2VorAbzFamGr(),
						is(BigDecimal.valueOf(67000))
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 */
				@Test
				void calculateForQuellenBesteuerteTest() {

					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000)
					);
					ekvJA.setBruttoLohn(new BigDecimal(60000));
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP1VorAbzFamGr(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjP2VorAbzFamGr(),
						is(BigDecimal.valueOf(48000))
					);
				}
			}

			@Nested
			class AcceptedAndNotAnnulliertTest {
				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void nichtQuellenBesteuerteShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(10000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 200'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void nichtQuellenBesteuerteMitSteuerbaresVermoegenAnGrenzeShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						new BigDecimal(200000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 250'000 - 200'000, 10% = + 5'000
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 67'000
				 */
				@Test
				void nichtQuellenBesteuerteMitSteuerbaresVermoegenShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(250000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 */
				@Test
				void quellenBesteuerteShouldBeAcceptedNotAnnulliertIfIsSinking() {

					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForQuellenbesteuert(
						finsitJA,
						BigDecimal.valueOf(100000)
					);
					ekvJA.setBruttoLohn(new BigDecimal(60000));
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void nichtQuellenBesteuerteShouldNotBeAnnulliertIfIsRising() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(10000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 */
				@Test
				void quellenBesteuerteShouldNotBeAnnulliertIfIsRising() {

					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForQuellenbesteuert(
						finsitJA,
						BigDecimal.ZERO
					);
					ekvJA.setBruttoLohn(new BigDecimal(60000));
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 */
				@Test
				void quellenBesteuerteShouldBeAnnulliertIfEKVIsAnnulliert() {

					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					extractEinkommensverschlechterungInfoJANullSafe(gesuch)
						.setEkvBasisJahrPlus1Annulliert(true);

					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForQuellenbesteuert(
						finsitJA,
						BigDecimal.ZERO
					);
					ekvJA.setBruttoLohn(new BigDecimal(60000));
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
				}

				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 */
				@Test
				void nichtQuellenBesteuerteShouldBeAnnulliertIfEKVIsAnnulliert() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					extractEinkommensverschlechterungInfoJANullSafe(gesuch)
						.setEkvBasisJahrPlus1Annulliert(true);
					FinanzielleSituation finsitJA = extractFinSitJANullsafe(
						gesuch.getGesuchsteller1()
					);
					Einkommensverschlechterung ekvJA = extractEKVJANullsafe(
						gesuch.getGesuchsteller1()
					);
					setFinSitValueForNichtQuellenbesteuert(
						finsitJA,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						ekvJA,
						new BigDecimal(60000),
						new BigDecimal(10000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
				}

			}

			private Gesuch prepareGesuchWithEmptyEKV() {
				Gesuch gesuch = prepareGesuch();
				createEKVInfoFor(gesuch);
				createEmptyEKVForGS1(gesuch);

				return gesuch;
			}

		}

		@Nested
		class FinSitTest {
			/**
			 * Steuerbares Einkommen 60'000
			 * <p>
			 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
			 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
			 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
			 * -------
			 * 62'000
			 */
			@Test
			void calculateForNichtQuellenBesteuerteTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					new BigDecimal(60000),
					new BigDecimal(10000),
					new BigDecimal(1000),
					new BigDecimal(1000)
				);
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					BigDecimal.ZERO
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(62000))
				);
			}

			/**
			 * Steuerbares Einkommen 60'000
			 * <p>
			 * Steuerbares Vermögen 200'000 - 200'000, 10% = + 0
			 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
			 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
			 * -------
			 * 62'000
			 */
			@Test
			void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenAnGrenzeTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					new BigDecimal(60000),
					new BigDecimal(200000),
					new BigDecimal(1000),
					new BigDecimal(1000)
				);
				getFinanzielleSituationJA(gesuch.getGesuchsteller1())
					.setSteuerbaresVermoegen(new BigDecimal(200000));
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					BigDecimal.ZERO
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(62000))
				);
			}

			/**
			 * Steuerbares Einkommen 60'000
			 * <p>
			 * Steuerbares Vermögen 250'000 - 200'000, 10% = + 5'000
			 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
			 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
			 * -------
			 * 67'000
			 */
			@Test
			void calculateForNichtQuellenBesteuerteMitSteuerbaresVermoegenTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					new BigDecimal(60000),
					new BigDecimal(250000),
					new BigDecimal(1000),
					new BigDecimal(1000)
				);
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					BigDecimal.ZERO
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(67000))
				);
			}

			/**
			 * Brutto Einkommen 60'000
			 * <p>
			 * Brutto Einkommen 20% = - 12'000
			 * -------
			 * 48'000
			 */
			@Test
			void calculateForQuellenBesteuerteTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					new BigDecimal(60000)
				);
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					BigDecimal.ZERO
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(48000))
				);
			}

			@Test
			void quellenBesteuertAllesNullTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					BigDecimal.ZERO
				);
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					BigDecimal.ZERO
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(0))
				);
			}

			@Test
			void nichtQuellenBesteuertAllesNullTest() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
					null,
					null,
					null,
					null
				);
				finanzielleSituationSchwyzRechner.calculateFinanzDaten(
					gesuch,
					null
				);
				assertThat(
					gesuch.getFinanzDatenDTO_alleine()
						.getMassgebendesEinkBjVorAbzFamGr(),
					is(BigDecimal.valueOf(0))
				);
			}

			@Nested
			class NegativeValues {
				@Test
				void nichtQuellenbesteuertNegativeEinkommenShouldBeSubtracted() {
					Gesuch gesuch = prepareGesuch();
					setFinSitValueForNichtQuellenbesteuert(
						getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
						new BigDecimal(-5000),
						BigDecimal.ZERO,
						new BigDecimal(10000),
						BigDecimal.ZERO
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjVorAbzFamGr(),
						is(BigDecimal.valueOf(5000))
					);
				}

				@Test
				void nichtQuellenbesteuertNegativeEinkommenLargerThanRemainingShouldBeNegative() {
					Gesuch gesuch = prepareGesuch();
					setFinSitValueForNichtQuellenbesteuert(
						getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
						new BigDecimal(-5000),
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjVorAbzFamGr(),
						is(new BigDecimal(-5000))
					);
				}

				// Übersteigt die Freigrenze nicht und wird entsprechend nicht zum Einkommen dazu gerechnet
				@Test
				void nichtQuellenbesteuertNegativeReinvermoegenShouldNotBeSubtracted() {
					Gesuch gesuch = prepareGesuch();
					setFinSitValueForNichtQuellenbesteuert(
						getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
						new BigDecimal(50000),
						new BigDecimal(-10000),
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjVorAbzFamGr(),
						is(BigDecimal.valueOf(50000))
					);
				}

				@Test
				void nichtQuellenbesteuertNegativeReinvermoegenLargerThanEinkommenShouldBeZero() {
					Gesuch gesuch = prepareGesuch();
					setFinSitValueForNichtQuellenbesteuert(
						getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
						BigDecimal.ZERO,
						new BigDecimal(-10000),
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.getMassgebendesEinkBjVorAbzFamGr(),
						is(BigDecimal.ZERO)
					);
				}
			}
		}

		private Gesuch prepareGesuch() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsteller1(createGesuchstellerMitLeerenFinSit());
			return gesuch;
		}
	}

	private static void createEmptyEKVForGS1(Gesuch gesuch) {
		final GesuchstellerContainer gs1 = gesuch.getGesuchsteller1();
		Objects.requireNonNull(gs1);
		createEmptyEKVForGS(gs1);
	}

	private static void createEmptyEKVForGS(GesuchstellerContainer gs) {
		final EinkommensverschlechterungContainer ekvContainer =
			new EinkommensverschlechterungContainer();
		ekvContainer.setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
		gs.setEinkommensverschlechterungContainer(ekvContainer);
	}

	private static void createEKVInfoFor(Gesuch gesuch) {
		final EinkommensverschlechterungInfoContainer ekvInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		ekvInfoContainer.getEinkommensverschlechterungInfoJA()
			.setEinkommensverschlechterung(true);
		ekvInfoContainer.getEinkommensverschlechterungInfoJA()
			.setEkvFuerBasisJahrPlus1(true);
		gesuch.setEinkommensverschlechterungInfoContainer(ekvInfoContainer);
	}

	@Nested
	class TwoGSTest {

		@Nested
		class EKVTest {

			@Nested
			class CalculationTest {
				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 200'000 * 200'000 / (200'000 + 200'000), 10% = +10'000
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 72'000
				 * <p>
				 * GS2 gleich => 72'000 x 2 = 144'000
				 */
				@Test
				void calculateForNichtQuellenBesteuerteTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, false);
					createEmptyEKVForGS2(gesuch);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller2()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					var resultatBkP1 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					var resultatBkP2 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(72000))
					);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(144000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGrGS2(),
						is(BigDecimal.valueOf(72000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(144000))
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 * <p>
				 * GS 2 gleich => 48'000 x 2 = 96'000
				 */
				@Test
				void calculateForQuellenBesteuerteTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					createEmptyEKVForGS2(gesuch);
					setGemeinsameSteuererklaerung(gesuch, false);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000)
					);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller2()),
						BigDecimal.valueOf(100000)
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					extractEKVJANullsafe(gesuch.getGesuchsteller2())
						.setBruttoLohn(new BigDecimal(60000));

					var resultatBkP1 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					var resultatBkP2 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(96000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(96000))
					);
				}

				@Test
				void quellenBesteuertGemeinsameStekTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000)
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					var resultatBkP1 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					var resultatBkP2 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(48000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(48000))
					);
				}

				@Test
				void nichtQuellenBesteuertGemeinsameStekNullTest() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					var resultatBkP1 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					var resultatBkP2 = finanzielleSituationSchwyzRechner
						.calculateResultateEinkommensverschlechterung(
							gesuch,
							1,
							true
						);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(62000))
					);
					assertThat(
						resultatBkP1.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGrGS1(),
						is(BigDecimal.valueOf(62000))
					);
					assertThat(
						resultatBkP2.getMassgebendesEinkVorAbzFamGr(),
						is(BigDecimal.valueOf(62000))
					);
				}
			}

			@Nested
			class AcceptedAndNotAnnulatedTest {
				/**
				 * Steuerbares Einkommen 60'000
				 * <p>
				 * Steuerbares Vermögen 10'000 - 200'000, 10% = + 0
				 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
				 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
				 * -------
				 * 62'000
				 * <p>
				 * GS2 gleich => 62'000 x 2 = 124'000
				 */
				@Test
				void nichtQuellenBesteuerteShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, false);
					createEmptyEKVForGS2(gesuch);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller2()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				/**
				 * Brutto Einkommen 60'000
				 * <p>
				 * Brutto Einkommen 20% = - 12'000
				 * -------
				 * 48'000
				 * <p>
				 * GS 2 gleich => 48'000 x 2 = 96'000
				 */
				@Test
				void quellenBesteuerteShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					createEmptyEKVForGS2(gesuch);
					setGemeinsameSteuererklaerung(gesuch, false);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000)
					);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller2()),
						BigDecimal.valueOf(100000)
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					extractEKVJANullsafe(gesuch.getGesuchsteller2())
						.setBruttoLohn(new BigDecimal(60000));

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				@Test
				void quellenBesteuerteShouldBeAnnulliertIfIsAnnulliert() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					extractEinkommensverschlechterungInfoJANullSafe(gesuch)
						.setEkvBasisJahrPlus1Annulliert(true);
					createEmptyEKVForGS2(gesuch);
					setGemeinsameSteuererklaerung(gesuch, false);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000)
					);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller2()),
						BigDecimal.valueOf(100000)
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					extractEKVJANullsafe(gesuch.getGesuchsteller2())
						.setBruttoLohn(new BigDecimal(60000));

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);

					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
				}

				@Test
				void quellenBesteuerteShouldNotBeAnnulliertIfIsRising() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					createEmptyEKVForGS2(gesuch);
					setGemeinsameSteuererklaerung(gesuch, false);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.ZERO
					);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller2()),
						BigDecimal.ZERO
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					extractEKVJANullsafe(gesuch.getGesuchsteller2())
						.setBruttoLohn(new BigDecimal(60000));

					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				@Test
				void quellenBesteuertGemeinsameStekShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000)
					);
					extractEKVJANullsafe(gesuch.getGesuchsteller1())
						.setBruttoLohn(new BigDecimal(60000));
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				@Test
				void nichtQuellenBesteuertGemeinsameStekNullShouldBeAcceptedNotAnnulliertIfIsSinking() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000),
						BigDecimal.valueOf(100000)
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);

					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				@Test
				void nichtQuellenBesteuertGemeinsameStekNullShouldNotBeAnnulliertIfIsRising() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);

					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(true)
					);
				}

				@Test
				void nichtQuellenBesteuertGemeinsameStekNullShouldBeAnnulliertIfIsAnnulliert() {
					Gesuch gesuch = prepareGesuchWithEmptyEKV();
					extractEinkommensverschlechterungInfoJANullSafe(gesuch)
						.setEkvBasisJahrPlus1Annulliert(true);
					setGemeinsameSteuererklaerung(gesuch, true);
					setFinSitValueForNichtQuellenbesteuert(
						extractFinSitJANullsafe(gesuch.getGesuchsteller1()),
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO,
						BigDecimal.ZERO
					);
					setAbstractFinSitValuesNichtQuellenbesteuert(
						extractEKVJANullsafe(gesuch.getGesuchsteller1()),
						new BigDecimal(60000),
						new BigDecimal(200000),
						new BigDecimal(1000),
						new BigDecimal(1000)
					);
					finanzielleSituationSchwyzRechner.calculateFinanzDaten(
						gesuch,
						BigDecimal.ZERO
					);

					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv1Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit().isEkv2Erfasst(),
						is(true)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_zuZweit()
							.isEkv1AcceptedAndNotAnnuliert(),
						is(false)
					);
					assertThat(
						gesuch.getFinanzDatenDTO_alleine()
							.isEkv2AcceptedAndNotAnnuliert(),
						is(false)
					);
				}
			}

			private void setGemeinsameSteuererklaerung(
				Gesuch gesuch,
				boolean gemeinsameSteuererklaerung
			) {
				final Familiensituation familiensituation = gesuch
					.extractFamiliensituation();
				Objects.requireNonNull(familiensituation);
				familiensituation.setGemeinsameSteuererklaerung(
					gemeinsameSteuererklaerung
				);
			}

			private Gesuch prepareGesuchWithEmptyEKV() {
				Gesuch gesuch = prepareGesuch();
				createEKVInfoFor(gesuch);
				createEmptyEKVForGS1(gesuch);
				return gesuch;
			}

			private void createEmptyEKVForGS2(Gesuch gesuch) {
				GesuchstellerContainer gs2 = gesuch.getGesuchsteller2();
				Objects.requireNonNull(gs2);
				createEmptyEKVForGS(gs2);
			}
		}

		/**
		 * Steuerbares Einkommen 60'000
		 * <p>
		 * Steuerbares Vermögen 200'000 * 200'000 / (200'000 + 200'000), 10% = + 10'000
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
		 * -------
		 * 72'000
		 * <p>
		 * GS2 gleich => 72'000 x 2 = 144'000
		 */
		@Test
		void calculateForNichtQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(
				gesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			setFinSitValueForNichtQuellenbesteuert(
				gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(72000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(144000))
			);
		}

		@Nested
		class NegativeValues {
			@Test
			void gs1NegativeReinvermoegenGs2LargerPositiveReinvermoegenShouldLeadToZeroMassgebendesEinkommenGS1() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller1()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(-40000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller2()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(80000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				var result = finanzielleSituationSchwyzRechner
					.calculateResultateFinanzielleSituation(
						gesuch,
						true
					);
				// Freibetrag = 200'000 * -80'000 / (200'000 + (-80'000)) = -133'000
				// Freibetrag negativ → Vermögen ohne Abzug anrechnen, Vermögen ist negativ, daher 0
				assertThat(
					result.getMassgebendesEinkVorAbzFamGrGS1(),
					is(BigDecimal.ZERO)
				);
			}

			@Test
			void gs1PositiveReinvermoegenGs2NegativeReinvermoegenSummedUnderFreibetragShouldLeadToZeroAnrechenbaresVermoegenGS1() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller1()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(1_000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller2()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(-400_000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				var result = finanzielleSituationSchwyzRechner
					.calculateResultateFinanzielleSituation(
						gesuch,
						true
					);
				assertThat(
					result.getMassgebendesEinkVorAbzFamGrGS1(),
					is(BigDecimal.ZERO)
				);
			}

			@Test
			void gs1PositiveReinvermoegenGs2NegativeReinvermoegenSummedOverFreibetragShouldLeadTo10PercentOfSumMinusFreibetragOf200kAnrechenbaresVermoegenGS1() {
				Gesuch gesuch = prepareGesuch();
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller1()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(220_000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				setFinSitValueForNichtQuellenbesteuert(
					gesuch.getGesuchsteller2()
						.getFinanzielleSituationContainer()
						.getFinanzielleSituationJA(),
					BigDecimal.ZERO,
					new BigDecimal(-10_000),
					BigDecimal.ZERO,
					BigDecimal.ZERO
				);
				var result = finanzielleSituationSchwyzRechner
					.calculateResultateFinanzielleSituation(
						gesuch,
						true
					);
				// 220'000 + (-10'000) - 200'000, davon 10% => 1'000
				assertThat(
					result.getMassgebendesEinkVorAbzFamGrGS1(),
					is(BigDecimal.valueOf(1_000))
				);
			}
		}

		@Test
		void gs2NullVermoegenShouldResultInZeroAnrechenbaresVermoegen() {
			Gesuch gesuch = prepareGesuch();
			BigDecimal einkommen = BigDecimal.valueOf(50_000);
			BigDecimal einkaeufe = BigDecimal.ZERO;
			BigDecimal liegenschaftsUnterhaltsKosten = BigDecimal.ZERO;
			BigDecimal reinvermoegen = BigDecimal.valueOf(800_000);
			setGemeinsameSteuererklaerung(
				gesuch,
				einkommen,
				einkaeufe,
				liegenschaftsUnterhaltsKosten,
				reinvermoegen
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(gesuch, true);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(110_000))
			);
		}

		private void setGemeinsameSteuererklaerung(
			Gesuch gesuch,
			BigDecimal einkommen,
			BigDecimal einkaeufe,
			BigDecimal liegenschaftsUnterhaltsKosten,
			BigDecimal reinvermoegen
		) {
			Familiensituation familiensituation = extractFamSitJANullsafe(
				gesuch
			);
			familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);

			gesuch.setGesuchsteller2(createGesuchstellerMitLeerenFinSit());

			FinanzielleSituation gemeinsameFinSit = extractFinSitJANullsafe(
				gesuch.getGesuchsteller1()
			);
			setFinSitValueForNichtQuellenbesteuert(
				gemeinsameFinSit,
				einkommen,
				reinvermoegen,
				einkaeufe,
				liegenschaftsUnterhaltsKosten
			);
		}

		/**
		 * Steuerbares Einkommen 60'000
		 * <p>
		 * Steuerbares Vermögen GS1 -5000 => 0.-
		 * Steuerbares Vermögen GS2 (200'000 + -5000) - 200000, 10% = 0
		 * Abzüge für den effektiven Liegenschaftsunterhalt... + 1'000
		 * Einkäufe in die berufliche Vorsorge Subtrahieren + 1'000
		 * -------
		 * 62'000
		 * <p>
		 */
		@Test
		void gs1NegativeEinkommenGs2PositiveShouldSubtractGS1FromGS2() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(
				gesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA(),
				new BigDecimal(-5000),
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			setFinSitValueForNichtQuellenbesteuert(
				gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA(),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(-5000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(57000))
			);
		}

		/**
		 * Brutto Einkommen 60'000
		 * <p>
		 * Brutto Einkommen 20% = - 12'000
		 * -------
		 * 48'000
		 * <p>
		 * GS 2 gleich => 48'000 x 2 = 96'000
		 *
		 */
		@Test
		void calculateForQuellenBesteuerteTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				new BigDecimal(60000)
			);
			setFinSitValueForQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller2()),
				new BigDecimal(60000)
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(48000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(96000))
			);
		}

		@Test
		void quellenBesteuertZweiteGSNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				new BigDecimal(60000)
			);
			getFinanzielleSituationContainer(gesuch.getGesuchsteller2())
				.setFinanzielleSituationJA(null);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(48000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(48000))
			);
		}

		@Test
		void nichtQuellenBesteuertZweiteGSNullTest() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				new BigDecimal(60000),
				new BigDecimal(200000),
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			getFinanzielleSituationContainer(gesuch.getGesuchsteller2())
				.setFinanzielleSituationJA(null);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(62000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(62000))
			);
		}

		@Test
		void testReinvermoegenGS1Quellenbesteuert() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				new BigDecimal(60000)
			);
			setFinSitValueForNichtQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller2()),
				new BigDecimal(60000),
				new BigDecimal(250_000),
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(48_000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(115_000))
			);
		}

		@Test
		void testReinvermoegenZero() {
			Gesuch gesuch = prepareGesuch();
			setFinSitValueForNichtQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				new BigDecimal(60000),
				BigDecimal.ZERO,
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			setFinSitValueForNichtQuellenbesteuert(
				getFinanzielleSituationJA(gesuch.getGesuchsteller2()),
				new BigDecimal(60000),
				BigDecimal.ZERO,
				new BigDecimal(1000),
				new BigDecimal(1000)
			);
			var result = finanzielleSituationSchwyzRechner
				.calculateResultateFinanzielleSituation(
					gesuch,
					true
				);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGrGS1(),
				is(BigDecimal.valueOf(62_000))
			);
			assertThat(
				result.getMassgebendesEinkVorAbzFamGr(),
				is(BigDecimal.valueOf(124_000))
			);
		}

		private Gesuch prepareGesuch() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(
				TestDataUtil.createDefaultFamiliensituationContainer()
			);
			gesuch.setGesuchsteller1(createGesuchstellerMitLeerenFinSit());
			gesuch.setGesuchsteller2(createGesuchstellerMitLeerenFinSit());
			return gesuch;
		}
	}

	private GesuchstellerContainer createGesuchstellerMitLeerenFinSit() {
		GesuchstellerContainer gesuchstellerContainer =
			new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer =
			new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituationForTest =
			new FinanzielleSituation();
		finanzielleSituationContainer.setFinanzielleSituationJA(
			finanzielleSituationForTest
		);
		gesuchstellerContainer.setFinanzielleSituationContainer(
			finanzielleSituationContainer
		);
		return gesuchstellerContainer;
	}

	private void setFinSitValueForNichtQuellenbesteuert(
		@Nonnull FinanzielleSituation finanzielleSituationForTest,
		@Nonnull BigDecimal steuerbaresEinkommen,
		@Nonnull BigDecimal steuerbaresVermoegen,
		@Nonnull BigDecimal einkaeufeVorsorge,
		@Nonnull BigDecimal abzuegeLiegenschaft
	) {
		finanzielleSituationForTest.setQuellenbesteuert(false);
		setAbstractFinSitValuesNichtQuellenbesteuert(
			finanzielleSituationForTest,
			steuerbaresEinkommen,
			steuerbaresVermoegen,
			einkaeufeVorsorge,
			abzuegeLiegenschaft
		);
	}

	private static void setAbstractFinSitValuesNichtQuellenbesteuert(
		@Nonnull AbstractFinanzielleSituation finanzielleSituationForTest,
		@Nonnull BigDecimal steuerbaresEinkommen,
		@Nonnull BigDecimal steuerbaresVermoegen,
		@Nonnull BigDecimal einkaeufeVorsorge,
		@Nonnull BigDecimal abzuegeLiegenschaft
	) {
		finanzielleSituationForTest.setSteuerbaresEinkommen(
			steuerbaresEinkommen
		);
		finanzielleSituationForTest.setEinkaeufeVorsorge(einkaeufeVorsorge);
		finanzielleSituationForTest.setAbzuegeLiegenschaft(abzuegeLiegenschaft);
		finanzielleSituationForTest.setSteuerbaresVermoegen(
			steuerbaresVermoegen
		);
	}

	private void setFinSitValueForQuellenbesteuert(
		@Nonnull FinanzielleSituation finanzielleSituationForTest,
		@Nonnull BigDecimal bruttolohn
	) {
		finanzielleSituationForTest.setQuellenbesteuert(true);
		finanzielleSituationForTest.setBruttoLohn(bruttolohn);
	}

	private FinanzielleSituation extractFinSitJANullsafe(
		@Nullable GesuchstellerContainer gesuchstellerContainer
	) {
		Objects.requireNonNull(gesuchstellerContainer);
		Objects.requireNonNull(
			gesuchstellerContainer.getFinanzielleSituationContainer()
		);
		return gesuchstellerContainer.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA();
	}

	private Einkommensverschlechterung extractEKVJANullsafe(
		@Nullable GesuchstellerContainer gesuchstellerContainer
	) {
		Objects.requireNonNull(gesuchstellerContainer);
		Objects.requireNonNull(
			gesuchstellerContainer.getEinkommensverschlechterungContainer()
		);
		Objects.requireNonNull(
			gesuchstellerContainer.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus1()
		);
		return gesuchstellerContainer.getEinkommensverschlechterungContainer()
			.getEkvJABasisJahrPlus1();
	}

	private Familiensituation extractFamSitJANullsafe(
		@Nullable Gesuch gesuch
	) {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(
			gesuch.getFamiliensituationContainer()
		);
		Familiensituation familiensituationJA = gesuch
			.getFamiliensituationContainer()
			.getFamiliensituationJA();
		Objects.requireNonNull(
			familiensituationJA
		);
		return familiensituationJA;
	}

	@Nonnull
	private EinkommensverschlechterungInfo extractEinkommensverschlechterungInfoJANullSafe(
		Gesuch gesuch
	) {
		return Objects.requireNonNull(
			gesuch.getEinkommensverschlechterungInfoContainer()
		).getEinkommensverschlechterungInfoJA();
	}
}
