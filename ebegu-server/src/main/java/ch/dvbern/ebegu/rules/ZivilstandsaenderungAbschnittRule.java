/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.mutationsmerger.util.VerfuegungZeitabschnittSplitter;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.RuleUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Umsetzung der ASIV Revision: Finanzielle Situation bei Mutation der Familiensituation anpassen
 * <p>
 * Gem. neuer ASIV Verordnung muss bei einem Wechsel von einem auf zwei Gesuchsteller oder umgekehrt die finanzielle
 * Situation ab
 * dem Folgemonat angepasst werden.
 * </p>
 */
public class ZivilstandsaenderungAbschnittRule extends AbstractAbschnittRule {

	private final Integer paramMinDauerKonkubinat;

	public ZivilstandsaenderungAbschnittRule(
		DateRange validityPeriod,
		Integer paramMinDauerKonkubinat,
		@Nonnull Locale locale
	) {
		super(
			RuleKey.ZIVILSTANDSAENDERUNG,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
		this.paramMinDauerKonkubinat = paramMinDauerKonkubinat;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {

		Gesuch gesuch = platz.extractGesuch();

		// Ueberpruefen, ob die Gesuchsteller-Kardinalität geändert hat. Nur dann muss evt. anders berechnet werden!
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		Familiensituation familiensituationErstgesuch = gesuch
			.extractFamiliensituationErstgesuch();

		final List<VerfuegungZeitabschnitt> zivilstandsaenderungAbschnitte =
			calculateZeitabschnitte(
				platz,
				familiensituation,
				familiensituationErstgesuch,
				gesuch
			);
		final List<VerfuegungZeitabschnitt> zeitabschnitteErstgesuch =
			familiensituationErstgesuch == null ?
				List.of() :
				calculateZeitabschnitte(
					platz,
					familiensituationErstgesuch,
					null,
					gesuch
				);
		return zivilstandsaenderungAbschnitte.stream()
			.flatMap(
				verfuegungZeitabschnitt -> VerfuegungZeitabschnittSplitter
					.splitOn(verfuegungZeitabschnitt, zeitabschnitteErstgesuch)
					.stream()
			)
			.peek(verfuegungZeitabschnitt -> {
				if (familiensituationErstgesuch != null) {
					verfuegungZeitabschnitt.setErstgesuchAnzahlGesuchstellende(
						familiensituationErstgesuch.hasSecondGesuchsteller(
							verfuegungZeitabschnitt.getGueltigkeit()
								.getGueltigBis()
						) ? 2 : 1
					);
				}
				setHasSecondGesuchstellerForFinSit(
					verfuegungZeitabschnitt,
					familiensituation,
					familiensituationErstgesuch
				);
			})
			.collect(Collectors.toList());
	}

	public List<VerfuegungZeitabschnitt> calculateZeitabschnitte(
		AbstractPlatz platz,
		Familiensituation familiensituation,
		@Nullable Familiensituation familiensituationErstgesuch,
		Gesuch gesuch
	) {
		final List<VerfuegungZeitabschnitt> zivilstandsaenderungAbschnitte =
			new ArrayList<>();

		LocalDate gesuchsperiodeBis = platz.extractGesuch()
			.getGesuchsperiode()
			.getGueltigkeit()
			.getGueltigBis();

		if (familiensituation.getAenderungPer() != null
			&& familiensituationErstgesuch != null
			&&
			familiensituation.hasSecondGesuchsteller(gesuchsperiodeBis)
				!= familiensituationErstgesuch.hasSecondGesuchsteller(
					gesuchsperiodeBis
				)) {

			// Die Zivilstandsaenderung gilt ab anfang nächstem Monat, die Bemerkung muss aber "per Heirat/Trennung" erfolgen
			final LocalDate stichtag = getStichtagForEreignis(
				RuleUtil.getFamSitAenderungPerDatum(
					gesuch,
					familiensituation.getAenderungPer()
				)
			);
			// Bemerkung erstellen
			MsgKey msgKey;
			if (familiensituation.hasSecondGesuchsteller(gesuchsperiodeBis)) {
				// Heirat
				msgKey = MsgKey.FAMILIENSITUATION_HEIRAT_MSG;
			} else {
				// Trennung
				msgKey = MsgKey.FAMILIENSITUATION_TRENNUNG_MSG;
			}

			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigAb(),
					stichtag.minusDays(1)
				)
			);

			VerfuegungZeitabschnitt abschnittNachMutation =
				createVerfuegungZeitabschnittForZivilstand(
					stichtag,
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigBis()
				);
			if (!gesuch.extractMandant()
				.getMandantIdentifier()
				.equals(MandantIdentifier.SCHWYZ)) {
				abschnittNachMutation.getBgCalculationInputAsiv()
					.addBemerkung(msgKey, getLocale());
			}
			zivilstandsaenderungAbschnitte.add(abschnittNachMutation);

		} else if (familiensituation.getFamilienstatus()
			== EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			&& familiensituation.getStartKonkubinat() != null
			&& gesuch.getGesuchsperiode()
				.getGueltigkeit()
				.contains(
					familiensituation.getStartKonkubinat()
						.plusYears(paramMinDauerKonkubinat)
				)
		) {
			final LocalDate startKonkubinatPlusXJahre = RuleUtil
				.getStichtagForEreignis(
					familiensituation.getStartKonkubinat()
						.plusYears(paramMinDauerKonkubinat)
				);

			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigAb(),
					startKonkubinatPlusXJahre.minusDays(1)
				)
			);

			final VerfuegungZeitabschnitt abschnittKonkubinat2GS =
				createVerfuegungZeitabschnittForZivilstand(
					startKonkubinatPlusXJahre,
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigBis()
				);
			abschnittKonkubinat2GS.getBgCalculationInputAsiv()
				.addBemerkung(
					MsgKey.FAMILIENSITUATION_KONKUBINAT_MSG,
					getLocale()
				);
			zivilstandsaenderungAbschnitte.add(abschnittKonkubinat2GS);

		} else {
			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigAb(),
					gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigBis()
				)
			);
		}
		return zivilstandsaenderungAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createVerfuegungZeitabschnittForZivilstand(
		@Nonnull LocalDate dateAb,
		@Nonnull LocalDate dateBis
	) {
		return createZeitabschnittWithinValidityPeriodOfRule(
			new DateRange(dateAb, dateBis)
		);
	}

	private static void setHasSecondGesuchstellerForFinSit(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		Familiensituation familiensituation,
		@Nullable Familiensituation familiensituationErstgesuch
	) {
		var relatedFamiliensituation = familiensituation.getAenderungPer()
			!= null
			&& verfuegungZeitabschnitt.getGueltigkeit()
				.getGueltigAb()
				.isBefore(familiensituation.getAenderungPer()) ?
					Objects.requireNonNull(familiensituationErstgesuch) :
					familiensituation;
		verfuegungZeitabschnitt
			.setHasSecondGesuchstellerForFinanzielleSituationForAsivAndGemeinde(
				relatedFamiliensituation.hasSecondGesuchsteller(
					// it must be checked at the end of the zeitabschnitt
					verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis()
				)
					// beim Spezialfall in Appenzell gibt es nur einen Antragsteller aber zwei finanzielle Situationen.
					|| relatedFamiliensituation.isSpezialFallAR()
			);
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
