/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util.zahlungslauf;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating and managing instances of {@link ZahlungslaufHelper}.
 * This class provides various methods to retrieve specific implementations of the
 * {@link ZahlungslaufHelper} based on the provided criteria, such as payment run type
 * and additional configuration.
 *
 * The factory supports handling different payment run types and scenarios, adapting
 * to the specific requirements of Gemeinde-Institution and Gemeinde-Antragsteller
 * payment processes. Furthermore, it integrates configuration retrieval via
 * {@link EinstellungService} for detailed setup of {@link ZahlungslaufHelper} when necessary.
 *
 */
@Stateless
public class ZahlungslaufHelperFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ZahlungslaufHelperFactory.class.getSimpleName()
	);

	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	public static ZahlungslaufHelper getZahlungslaufHelper(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp
	) {
		return getZahlungslaufHelper(zahlungslaufTyp, null);
	}

	@Nonnull
	public static ZahlungslaufHelper getZahlungslaufHelper(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nullable HoehereBeitraegeTyp beitraegeTyp
	) {
		return switch (zahlungslaufTyp) {
		case GEMEINDE_INSTITUTION -> new ZahlungslaufInstitutionenHelper(
			beitraegeTyp
		);
		case GEMEINDE_ANTRAGSTELLER -> new ZahlungslaufAntragstellerHelper(
			beitraegeTyp
		);
		};
	}

	/**
	 * Retrieves a {@link ZahlungslaufHelper} instance for the specified time period and payment run type.
	 * The method dynamically determines the appropriate {@link ZahlungslaufHelper}, considering factors
	 * such as the existence of Betreuung and specific settings.
	 *
	 * @param zeitabschnitt The time period of the payment run, represented by a {@link VerfuegungZeitabschnitt}.
	 * This contains details about the management and related settings.
	 * @param zahlungslaufTyp The type of payment run, represented by a {@link ZahlungslaufTyp}. This
	 * dictates the specific behavior and logic for the payment process.
	 * @return A {@link ZahlungslaufHelper} instance corresponding to the provided time period and payment
	 * run type. If specific conditions cannot be met, a default implementation with fallback behavior is returned.
	 */
	public ZahlungslaufHelper getZahlungslaufHelper(
		VerfuegungZeitabschnitt zeitabschnitt,
		ZahlungslaufTyp zahlungslaufTyp
	) {
		Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		ZahlungslaufHelper zahlungslaufHelper = null;

		if (null != betreuung) {
			Einstellung e = einstellungService.findEinstellung(
				EinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
				betreuung.extractGemeinde(),
				betreuung.extractGesuchsperiode()
			);

			HoehereBeitraegeTyp beitraegeTyp = HoehereBeitraegeTyp.valueOf(
				e.getValue()
			);

			zahlungslaufHelper = ZahlungslaufHelperFactory
				.getZahlungslaufHelper(zahlungslaufTyp, beitraegeTyp);

		} else {
			LOGGER.error(
				"Verfügung {} in Zeitabschnitt {} has no Betreuung. Without a Betreuung HoehereBeitraegeTyp can not be "
					+ "determined. Using ZahlungslaufHelper with default behavior now.",
				zeitabschnitt.getVerfuegung().getId(),
				zeitabschnitt
			);
			throw new ZahlungslaufHelperCreateException(
				"Could not create ZahlungslaufHelper for \"Zeitabschnitt\" with ID "
					+ zeitabschnitt.getId()
					+ " because the linked Gesuch has no Betreuung."
			);
		}

		return zahlungslaufHelper;
	}
}
