/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungComparator;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import lombok.Getter;
import lombok.Setter;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.BETREUUNG_COMPARATOR;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.FKJV_PAUSCHALE_BEI_ANSPRUCH;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.FKJV_TEXTE;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.GESCHWISTERNBONUS_TYP;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MIN_TARIF;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.SCHULERGAENZENDE_BETREUUNGEN;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD;
import static ch.dvbern.ebegu.einstellung.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den Einstellungen gelesen werden.
 */
@Getter
@Setter
public final class BGRechnerParameterDTO {

	private BigDecimal maxVerguenstigungVorschuleBabyProTg;
	private BigDecimal maxVerguenstigungVorschuleKindProTg;
	private BigDecimal maxVerguenstigungKindergartenKindProTg;

	private BigDecimal maxVerguenstigungVorschuleBabyProStd;
	private BigDecimal maxVerguenstigungVorschuleKindProStd;
	private BigDecimal maxVerguenstigungKindergartenKindProStd;
	private BigDecimal maxVerguenstigungPrimarschuleKindProStd;

	private BigDecimal maxMassgebendesEinkommen;
	private BigDecimal minMassgebendesEinkommen;

	private BigDecimal oeffnungstageKita;
	private BigDecimal oeffnungstageTFO;
	private BigDecimal oeffnungsstundenTFO;

	private BigDecimal zuschlagBehinderungProTg;
	private BigDecimal zuschlagBehinderungProStd;

	private BigDecimal minVerguenstigungProTg;
	private BigDecimal minVerguenstigungProStd;

	private BigDecimal maxTarifTagesschuleMitPaedagogischerBetreuung;
	private BigDecimal maxTarifTagesschuleOhnePaedagogischerBetreuung;
	private BigDecimal minTarifTagesschule;

	private boolean texteForFKJV = false;

	private boolean schulergaenzendeBetreuung;

	private boolean besondereBeduerfnisseOnlyWhenAnspruch;

	private GeschwisterbonusTyp geschwisterbonusTyp;

	private BetreuungComparator betreuungComparator;

	private MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParameter =
		new MahlzeitenverguenstigungParameter();

	private BGRechnerParameterGemeindeDTO gemeindeParameter =
		new BGRechnerParameterGemeindeDTO();

	public BGRechnerParameterDTO(
		Map<EinstellungKey, Einstellung> paramMap,
		Gesuchsperiode gesuchsperiode,
		Gemeinde gemeinde
	) {
		this.setSchulergaenzendeBetreuung(
			asBoolean(
				paramMap,
				SCHULERGAENZENDE_BETREUUNGEN,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungVorschuleBabyProTg(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungVorschuleKindProTg(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungKindergartenKindProTg(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungVorschuleBabyProStd(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungVorschuleKindProStd(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungKindergartenKindProStd(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxVerguenstigungPrimarschuleKindProStd(
			asBigDecimal(
				paramMap,
				MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxMassgebendesEinkommen(
			asBigDecimal(
				paramMap,
				MAX_MASSGEBENDES_EINKOMMEN,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMinMassgebendesEinkommen(
			asBigDecimal(
				paramMap,
				MIN_MASSGEBENDES_EINKOMMEN,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setOeffnungstageKita(
			asBigDecimal(
				paramMap,
				OEFFNUNGSTAGE_KITA,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setOeffnungstageTFO(
			asBigDecimal(
				paramMap,
				OEFFNUNGSTAGE_TFO,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setOeffnungsstundenTFO(
			asBigDecimal(
				paramMap,
				OEFFNUNGSSTUNDEN_TFO,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setZuschlagBehinderungProTg(
			asBigDecimal(
				paramMap,
				ZUSCHLAG_BEHINDERUNG_PRO_TG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setZuschlagBehinderungProStd(
			asBigDecimal(
				paramMap,
				ZUSCHLAG_BEHINDERUNG_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMinVerguenstigungProTg(
			asBigDecimal(
				paramMap,
				MIN_VERGUENSTIGUNG_PRO_TG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMinVerguenstigungProStd(
			asBigDecimal(
				paramMap,
				MIN_VERGUENSTIGUNG_PRO_STD,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxTarifTagesschuleMitPaedagogischerBetreuung(
			asBigDecimal(
				paramMap,
				MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMaxTarifTagesschuleOhnePaedagogischerBetreuung(
			asBigDecimal(
				paramMap,
				MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG,
				gesuchsperiode,
				gemeinde
			)
		);
		this.setMinTarifTagesschule(
			asBigDecimal(paramMap, MIN_TARIF, gesuchsperiode, gemeinde)
		);
		mahlzeitenverguenstigungParameter =
			new MahlzeitenverguenstigungParameter(
				asBoolean(
					paramMap,
					EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
					gesuchsperiode,
					gemeinde
				),
				asBoolean(
					paramMap,
					EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT,
					gesuchsperiode,
					gemeinde
				),
				asBigDecimal(
					paramMap,
					GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT,
					gesuchsperiode,
					gemeinde
				)
			);
		this.setGemeindeParameter(
			new BGRechnerParameterGemeindeDTO(
				paramMap,
				gesuchsperiode,
				gemeinde
			)
		);
		this.texteForFKJV = asBoolean(
			paramMap,
			FKJV_TEXTE,
			gesuchsperiode,
			gemeinde
		);
		this.besondereBeduerfnisseOnlyWhenAnspruch = asBoolean(
			paramMap,
			FKJV_PAUSCHALE_BEI_ANSPRUCH,
			gesuchsperiode,
			gemeinde
		);
		this.geschwisterbonusTyp = GeschwisterbonusTyp.getEnumValue(
			paramMap.get(GESCHWISTERNBONUS_TYP)
		);

		this.betreuungComparator = BetreuungComparator.getEnumValue(
			paramMap.get(BETREUUNG_COMPARATOR)
		);
	}

	public BGRechnerParameterDTO() {

	}

	private BigDecimal asBigDecimal(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde
	) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '"
				+ paramKey
				+ "' could not be loaded for the given Gemeinde '"
				+ gemeinde.getName()
				+ "', Gesuchsperiode "
				+ '\''
				+ gesuchsperiode
				+ '\'';
			throw new EbeguEntityNotFoundException(
				"loadCalculatorParameters",
				message,
				ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND,
				paramKey
			);
		}
		return param.getValueAsBigDecimal();
	}

	private Boolean asBoolean(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde
	) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '"
				+ paramKey
				+ "' could not be loaded for the given Gemeinde '"
				+ gemeinde.getName()
				+ "', Gesuchsperiode "
				+ '\''
				+ gesuchsperiode
				+ '\'';
			throw new EbeguEntityNotFoundException(
				"loadCalculatorParameters",
				message,
				ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND,
				paramKey
			);
		}
		return param.getValueAsBoolean();
	}

	public BigDecimal getMaxMassgebendesEinkommenZurBerechnungDesGutscheinsProZeiteinheit() {
		if (this.gemeindeParameter.getGemeindePauschalbetragEnabled()) {
			return this.gemeindeParameter
				.getGemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung();
		}

		return maxMassgebendesEinkommen;
	}

	public Boolean getMahlzeitenverguenstigungEnabled() {
		return mahlzeitenverguenstigungParameter.isEnabled();
	}
}
