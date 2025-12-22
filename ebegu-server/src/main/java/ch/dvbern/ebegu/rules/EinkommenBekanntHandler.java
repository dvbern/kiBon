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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.MsgKey;

public class EinkommenBekanntHandler extends AbstractEinkommenHandler {

	protected EinkommenBekanntHandler(
		Locale locale,
		@Nullable BigDecimal maxEinkommenEKV,
		BigDecimal maxEinkommen
	) {
		super(locale, maxEinkommenEKV, maxEinkommen);
	}

	@Override
	protected void handleEinkommen(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		// Die Finanzdaten berechnen
		FinanzDatenDTO finanzDatenDTO;
		if (inputData.isHasSecondGesuchstellerForFinanzielleSituation()) {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_zuZweit();
			setMassgebendesEinkommen(
				inputData.isEkv1ZuZweit(),
				inputData.isEkv2ZuZweit(),
				finanzDatenDTO,
				inputData,
				platz,
				getLocale()
			);
		} else {
			finanzDatenDTO = platz.extractGesuch().getFinanzDatenDTO_alleine();
			setMassgebendesEinkommen(
				inputData.isEkv1Alleine(),
				inputData.isEkv2Alleine(),
				finanzDatenDTO,
				inputData,
				platz,
				getLocale()
			);
		}

		// Erst jetzt kann das Maximale Einkommen geprueft werden!
		if (inputData.getMassgebendesEinkommen().compareTo(getMaxEinkommen())
			>= 0) {
			//maximales einkommen wurde ueberschritten
			handleMaximalesEinkommenUeberschritten(inputData);
			inputData.addBemerkung(
				MsgKey.EINKOMMEN_MAX_MSG,
				getLocale(),
				NumberFormat.getInstance().format(getMaxEinkommen())
			);
		}
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private void setMassgebendesEinkommen(
		boolean isEkv1,
		boolean isEkv2,
		FinanzDatenDTO finanzDatenDTO,
		@Nonnull BGCalculationInput inputData,
		AbstractPlatz betreuung,
		@Nonnull Locale locale
	) {
		int basisjahr = betreuung.extractGesuchsperiode().getBasisJahr();
		int basisjahrPlus1 = betreuung.extractGesuchsperiode()
			.getBasisJahrPlus1();
		int basisjahrPlus2 = betreuung.extractGesuchsperiode()
			.getBasisJahrPlus2();

		if (isEkv1) {
			boolean isEkv1DifferenceMoreThan20 = finanzDatenDTO
				.isEkv1Accepted();
			boolean isEkv1Annuliert = finanzDatenDTO.isEkv1Annulliert();

			handleEKV(
				finanzDatenDTO,
				inputData,
				locale,
				basisjahr,
				basisjahrPlus1,
				isEkv1DifferenceMoreThan20,
				isEkv1Annuliert
			);

		} else if (isEkv2) {
			boolean isEkv2DifferenceMoreThan20 = finanzDatenDTO
				.isEkv2Accepted();
			boolean isEkv2Annuliert = finanzDatenDTO.isEkv2Annulliert();
			handleEKV(
				finanzDatenDTO,
				inputData,
				locale,
				basisjahr,
				basisjahrPlus2,
				isEkv2DifferenceMoreThan20,
				isEkv2Annuliert
			);

		} else {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(
				finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
			);
			inputData.setEinkommensjahr(basisjahr);
		}
	}

	private void handleEKV(
		FinanzDatenDTO finanzDatenDTO,
		@Nonnull BGCalculationInput inputData,
		@Nonnull Locale locale,
		int basisjahr,
		int ekvJahr,
		boolean isEkvDifferenceMoreThan20,
		boolean isEkvAnnuliert
	) {
		boolean isMassgebendesEinkommenTooHighForEKV =
			checkMassgebendesEinkommenNachAbzugTooHighForEKV(
				inputData,
				finanzDatenDTO
			);

		if (isEkvAnnuliert) {
			setEKVAnnuliertDataAndMessage(
				finanzDatenDTO,
				inputData,
				locale,
				basisjahr,
				ekvJahr
			);
			return;
		}

		if (!isEkvDifferenceMoreThan20) {
			setEKVDifferenceTooLowDataAndMessage(
				finanzDatenDTO,
				inputData,
				locale,
				basisjahr,
				ekvJahr
			);
			return;
		}

		if (isMassgebendesEinkommenTooHighForEKV) {
			ignoreEKVAndSetMessage(finanzDatenDTO, inputData, basisjahr);
		} else {
			acceptEKVAndSetMessage(
				finanzDatenDTO,
				inputData,
				locale,
				basisjahr,
				ekvJahr
			);
		}

	}

	private static void setEKVAnnuliertDataAndMessage(
		FinanzDatenDTO finanzDatenDTO,
		BGCalculationInput inputData,
		Locale locale,
		int basisjahr,
		int ekvJahr
	) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(
			finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
		);
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
			MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
			locale,
			String.valueOf(ekvJahr)
		);
	}

	private void setEKVDifferenceTooLowDataAndMessage(
		FinanzDatenDTO finanzDatenDTO,
		BGCalculationInput inputData,
		Locale locale,
		int basisjahr,
		int ekvJahr
	) {
		boolean isMassgebendesEinkommenTooHighForEKV =
			checkMassgebendesEinkommenNachAbzugTooHighForEKV(
				inputData,
				finanzDatenDTO
			);

		inputData.setMassgebendesEinkommenVorAbzugFamgr(
			finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
		);
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
			MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
			locale,
			String.valueOf(ekvJahr),
			String.valueOf(finanzDatenDTO.getMinEKV()),
			String.valueOf(basisjahr)
		);
		if (isMassgebendesEinkommenTooHighForEKV) {
			inputData.addBemerkung(
				MsgKey.EINKOMMEN_TOO_HIGH_FOR_EKV,
				getLocale(),
				String.valueOf(basisjahr),
				NumberFormat.getInstance().format(getMaxEinkommenEKV())
			);
		}
	}

	private void ignoreEKVAndSetMessage(
		@Nonnull FinanzDatenDTO finanzDatenDTO,
		@Nonnull BGCalculationInput inputData,
		int basisjahr
	) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(
			finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
		);
		inputData.setEinkommensjahr(basisjahr);
		inputData.addBemerkung(
			MsgKey.EINKOMMEN_TOO_HIGH_FOR_EKV,
			getLocale(),
			String.valueOf(basisjahr),
			NumberFormat.getInstance().format(getMaxEinkommenEKV())
		);
	}

	private static void acceptEKVAndSetMessage(
		FinanzDatenDTO finanzDatenDTO,
		BGCalculationInput inputData,
		Locale locale,
		int basisjahr,
		int ekvJahr
	) {
		if (ekvJahr == basisjahr + 1) {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(
				finanzDatenDTO.getMassgebendesEinkBjP1VorAbzFamGr()
			);
		} else {
			inputData.setMassgebendesEinkommenVorAbzugFamgr(
				finanzDatenDTO.getMassgebendesEinkBjP2VorAbzFamGr()
			);
		}
		inputData.setEkvAccepted(true);
		inputData.setEinkommensjahr(ekvJahr);
		inputData.addBemerkung(
			MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
			locale,
			String.valueOf(ekvJahr),
			String.valueOf(finanzDatenDTO.getMinEKV()),
			String.valueOf(basisjahr)
		);
	}

	private boolean checkMassgebendesEinkommenNachAbzugTooHighForEKV(
		@Nonnull BGCalculationInput inputData,
		@Nonnull FinanzDatenDTO finanzDatenDTO
	) {
		// rule not active
		if (getMaxEinkommenEKV() == null) {
			return false;
		}
		// abzug is null if familienAbzugAbschnittRule not active. In this case, there is no familienabzug
		var abzug = (inputData.getAbzugFamGroesseTotal() == null) ?
			BigDecimal.ZERO :
			inputData.getAbzugFamGroesseTotal();
		return finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr()
			.subtract(abzug)
			.compareTo(getMaxEinkommenEKV())
			> 0;
	}
}
