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

package ch.dvbern.ebegu.api.converter.gesuch;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxTsCalculationResult;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegung;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegungZeitabschnitt;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegungZeitabschnittBemerkung;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnittBemerkung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguFingerWegException;

@Dependent
public class JaxVerfuegungConverter extends AbstractBaseConverter {
	/**
	 * converts the given verfuegung into a JaxVerfuegung
	 *
	 * @return dto with the values of the verfuegung
	 */
	@Nullable
	public JaxVerfuegung verfuegungToJax(@Nullable Verfuegung verfuegung) {
		if (verfuegung == null) {
			return null;
		}

		final JaxVerfuegung jaxVerfuegung = new JaxVerfuegung();
		convertAbstractVorgaengerFieldsToJAX(verfuegung, jaxVerfuegung);
		jaxVerfuegung.setGeneratedBemerkungen(
			verfuegung.getGeneratedBemerkungen()
		);
		jaxVerfuegung.setManuelleBemerkungen(
			verfuegung.getManuelleBemerkungen()
		);
		jaxVerfuegung.setKategorieKeinPensum(
			verfuegung.isKategorieKeinPensum()
		);
		jaxVerfuegung.setKategorieMaxEinkommen(
			verfuegung.isKategorieMaxEinkommen()
		);
		jaxVerfuegung.setKategorieNichtEintreten(
			verfuegung.isKategorieNichtEintreten()
		);
		jaxVerfuegung.setKategorieNormal(verfuegung.isKategorieNormal());
		jaxVerfuegung.setVeraenderungVerguenstigungGegenueberVorgaenger(
			verfuegung.getVeraenderungVerguenstigungGegenueberVorgaenger()
		);
		jaxVerfuegung.setIgnorable(verfuegung.getIgnorable());
		jaxVerfuegung.setKorrekturAusbezahltEltern(
			verfuegung.getKorrekturAusbezahltEltern()
		);
		jaxVerfuegung.setKorrekturAusbezahltInstitution(
			verfuegung.getKorrekturAusbezahltInstitution()
		);

		List<JaxVerfuegungZeitabschnitt> zeitabschnitte = verfuegung
			.getZeitabschnitte()
			.stream()
			.map(this::verfuegungZeitabschnittToJax)
			.collect(Collectors.toList());
		jaxVerfuegung.setZeitabschnitte(zeitabschnitte);

		return jaxVerfuegung;
	}

	@Nullable
	public Verfuegung verfuegungToEntity(
		@Nullable JaxVerfuegung jaxVerfuegung
	) {
		throw new EbeguFingerWegException(
			"verfuegungToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	@Nullable
	private JaxVerfuegungZeitabschnitt verfuegungZeitabschnittToJax(
		@Nullable VerfuegungZeitabschnitt zeitabschnitt
	) {
		if (zeitabschnitt == null) {
			return null;
		}

		// Achtung: Hier sollten nur Daten aus dem RelevantBGCalculation*Result* verwendet werden, da die Daten aus den
		// RelevantBgCalculation*Input* nicht gespeichert werden und somit bei verfuegten Angeboten nicht mehr
		// zugaenglich
		// sind. Ausnahme sind Daten, die ZUM VERFUEGEN gebraucht werden, wie z.B.
		// getRelevantBgCalculationInput().isSameVerfuegteVerfuegungsrelevanteDaten()

		final JaxVerfuegungZeitabschnitt jaxZeitabschn =
			new JaxVerfuegungZeitabschnitt();
		convertAbstractDateRangedFieldsToJAX(zeitabschnitt, jaxZeitabschn);
		jaxZeitabschn.setAbzugFamGroesse(zeitabschnitt.getAbzugFamGroesse());
		jaxZeitabschn.setErwerbspensumGS1(
			zeitabschnitt.getRelevantBgCalculationInput().getErwerbspensumGS1()
		);
		jaxZeitabschn.setErwerbspensumGS2(
			zeitabschnitt.getRelevantBgCalculationInput().getErwerbspensumGS2()
		);
		jaxZeitabschn.setBetreuungspensumProzent(
			zeitabschnitt.getBetreuungspensumProzent()
		);
		jaxZeitabschn.setAnspruchspensumRest(
			zeitabschnitt.getRelevantBgCalculationInput()
				.getAnspruchspensumRest()
		);
		jaxZeitabschn.setBgPensum(zeitabschnitt.getBgPensum());
		jaxZeitabschn.setAnspruchspensumProzent(
			zeitabschnitt.getAnspruchberechtigtesPensum()
		);
		jaxZeitabschn.setBetreuungspensumZeiteinheit(
			zeitabschnitt.getBetreuungspensumZeiteinheit()
		);
		jaxZeitabschn.setVollkosten(zeitabschnitt.getVollkosten());
		jaxZeitabschn.setVerguenstigungOhneBeruecksichtigungVollkosten(
			zeitabschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		jaxZeitabschn.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(
			zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		jaxZeitabschn.setVerguenstigung(zeitabschnitt.getVerguenstigung());
		jaxZeitabschn.setVerguenstigungProZeiteinheit(
			zeitabschnitt.getVerguenstigungProZeiteinheit()
		);
		jaxZeitabschn.setMinimalerElternbeitrag(
			zeitabschnitt.getMinimalerElternbeitrag()
		);
		jaxZeitabschn.setMinimalerElternbeitragGekuerzt(
			zeitabschnitt.getMinimalerElternbeitragGekuerzt()
		);
		jaxZeitabschn.setElternbeitrag(zeitabschnitt.getElternbeitrag());
		jaxZeitabschn.setMassgebendesEinkommenVorAbzugFamgr(
			zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr()
		);
		jaxZeitabschn.setVerfuegungZeitabschnittBemerkungList(
			verfuegungZeitabschnittBemerkungenToJax(
				zeitabschnitt.getVerfuegungZeitabschnittBemerkungList()
			)
		);
		jaxZeitabschn.setFamGroesse(zeitabschnitt.getFamGroesse());
		jaxZeitabschn.setEinkommensjahr(zeitabschnitt.getEinkommensjahr());
		jaxZeitabschn.setVerfuegteAnzahlZeiteinheiten(
			zeitabschnitt.getVerfuegteAnzahlZeiteinheiten()
		);
		jaxZeitabschn.setAnspruchsberechtigteAnzahlZeiteinheiten(
			zeitabschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten()
		);
		jaxZeitabschn.setZeiteinheit(zeitabschnitt.getZeiteinheit());
		jaxZeitabschn.setKategorieKeinPensum(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isKategorieKeinPensum()
		);
		jaxZeitabschn.setKategorieMaxEinkommen(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isKategorieMaxEinkommen()
		);
		jaxZeitabschn.setZuSpaetEingereicht(
			zeitabschnitt.isZuSpaetEingereicht()
		);
		jaxZeitabschn.setMinimalesEwpUnterschritten(
			zeitabschnitt.isMinimalesEwpUnterschritten()
		);
		jaxZeitabschn.setZahlungsstatusInstitution(
			zeitabschnitt.getZahlungsstatusInstitution()
		);
		jaxZeitabschn.setZahlungsstatusAntragsteller(
			zeitabschnitt.getZahlungsstatusAntragsteller()
		);
		jaxZeitabschn.setSameVerfuegteVerfuegungsrelevanteDaten(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isSameVerfuegteVerfuegungsrelevanteDaten()
		);
		jaxZeitabschn.setSameAusbezahlteVerguenstigung(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isSameAusbezahlterBetragInstitution()
		);
		jaxZeitabschn.setSameAusbezahlteMahlzeiten(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isSameAusbezahlterBetragAntragsteller()
		);
		jaxZeitabschn.setSameVerfuegteMahlzeitenVerguenstigung(
			zeitabschnitt.getRelevantBgCalculationInput()
				.isSameVerfuegteMahlzeitenVerguenstigung()
		);
		jaxZeitabschn.setTsCalculationResultMitPaedagogischerBetreuung(
			tsCalculationResultToJax(
				zeitabschnitt.getTsCalculationResultMitPaedagogischerBetreuung()
			)
		);
		jaxZeitabschn.setTsCalculationResultOhnePaedagogischerBetreuung(
			tsCalculationResultToJax(
				zeitabschnitt
					.getTsCalculationResultOhnePaedagogischerBetreuung()
			)
		);
		jaxZeitabschn.setVerguenstigungMahlzeitTotal(
			zeitabschnitt.getRelevantBgCalculationResult()
				.getVerguenstigungMahlzeitenTotal()
		);
		jaxZeitabschn.setAuszahlungAnEltern(
			zeitabschnitt.isAuszahlungAnEltern()
		);
		jaxZeitabschn.setBeitragshoeheProzent(
			zeitabschnitt.getBeitraghoheProzent()
		);
		jaxZeitabschn.setZusaetzlicherGutscheinGemeindeBetrag(
			zeitabschnitt.getRelevantBgCalculationResult()
				.getZusaetzlicherGutscheinGemeindeBetrag()
		);
		jaxZeitabschn.setHoehererBeitrag(zeitabschnitt.getHoehererBeitrag());
		jaxZeitabschn.setBedarfsstufe(zeitabschnitt.getBedarfsstufe());
		return jaxZeitabschn;
	}

	public VerfuegungZeitabschnitt verfuegungZeitabschnittToEntity(
		@Nullable JaxVerfuegungZeitabschnitt jaxVerfuegungZeitabschnitt
	) {
		throw new EbeguFingerWegException(
			"verfuegungZeitabschnittToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	@Nonnull
	private List<JaxVerfuegungZeitabschnittBemerkung> verfuegungZeitabschnittBemerkungenToJax(
		@Nonnull List<VerfuegungZeitabschnittBemerkung> verfuegungZeitabschnittBemerkungen
	) {
		return verfuegungZeitabschnittBemerkungen.stream()
			.map(this::verfuegungZeitabschnittBemerkungToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	private JaxVerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkungToJax(
		@Nonnull VerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkung
	) {
		JaxVerfuegungZeitabschnittBemerkung result =
			new JaxVerfuegungZeitabschnittBemerkung();
		convertAbstractDateRangedFieldsToJAX(
			verfuegungZeitabschnittBemerkung,
			result
		);
		result.setBemerkung(verfuegungZeitabschnittBemerkung.getBemerkung());
		return result;
	}

	public VerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkungToEntity(
		@Nullable JaxVerfuegungZeitabschnittBemerkung jaxVerfuegungZeitabschnittBemerkung
	) {
		throw new EbeguFingerWegException(
			"VerfuegungZeitabschnittBemerkung",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	@Nullable
	private JaxTsCalculationResult tsCalculationResultToJax(
		@Nullable TSCalculationResult zeitabschnitt
	) {
		if (zeitabschnitt == null) {
			return null;
		}
		JaxTsCalculationResult result = new JaxTsCalculationResult();
		result.setBetreuungszeitProWoche(
			zeitabschnitt.getBetreuungszeitProWoche()
		);
		result.setBetreuungszeitProWocheFormatted(
			zeitabschnitt.getBetreuungszeitProWocheFormatted()
		);
		result.setVerpflegungskosten(zeitabschnitt.getVerpflegungskosten());
		result.setVerpflegungskostenVerguenstigt(
			zeitabschnitt.getVerpflegungskostenVerguenstigt()
		);
		result.setGebuehrProStunde(zeitabschnitt.getGebuehrProStunde());
		result.setTotalKostenProWoche(zeitabschnitt.getTotalKostenProWoche());
		return result;
	}
}
