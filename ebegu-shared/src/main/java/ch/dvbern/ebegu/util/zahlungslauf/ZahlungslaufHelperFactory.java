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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating and managing instances of {@link ZahlungslaufHelper}.
 * This class provides various methods to retrieve specific implementations of the
 * {@link ZahlungslaufHelper} based on the provided criteria, such as payment run type
 * and additional configuration. It also maintains an internal cache to optimize
 * performance and avoid redundant instance creation.
 *
 * The factory supports handling different payment run types and scenarios, adapting
 * to the specific requirements of Gemeinde-Institution and Gemeinde-Antragsteller
 * payment processes. Furthermore, it integrates configuration retrieval via
 * {@link EinstellungService} for detailed setup of {@link ZahlungslaufHelper} when necessary.
 *
 * Note: The cache can be cleared using the provided method to force re-instantiation
 * of helper instances.
 */
@Stateless
public class ZahlungslaufHelperFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ZahlungslaufHelperFactory.class.getSimpleName()
	);

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private PrincipalBean principalBean;

	/**
	 * A map that stores instances of {@link ZahlungslaufHelper} keyed by their associated {@link Mandant} and
	 * {@link Gesuchsperiode}.
	 * This map functions as a cache to ensure efficient retrieval and reusability of helper instances
	 * specific to different payment run types (ZahlungslaufTyp).
	 *
	 * Each {@link ZahlungslaufHelper} is responsible for handling the logic and behavior linked to its designated
	 * payment run type. This includes determining payment statuses, calculating payment amounts, and managing
	 * addresses associated with payments, among other operations.
	 *
	 * The {@code helperMap} is intended to be utilized by the factory methods within the
	 * {@code ZahlungslaufHelperFactory}
	 * to dynamically resolve or create the appropriate {@link ZahlungslaufHelper} instance as required.
	 *
	 * This final field ensures thread safety for its internal map instance.
	 */
	private final Map<String, ZahlungslaufHelper> helperMap =
		new HashMap<>();

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
		if (ZahlungslaufTyp.GEMEINDE_INSTITUTION == zahlungslaufTyp) {
			if (null == beitraegeTyp) {
				return new ZahlungslaufInstitutionenHelper();
			}
			return new ZahlungslaufInstitutionenHelper(beitraegeTyp);
		}
		if (ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER == zahlungslaufTyp) {
			if (null == beitraegeTyp) {
				return new ZahlungslaufAntragstellerHelper();
			}
			return new ZahlungslaufAntragstellerHelper(beitraegeTyp);
		}
		// Unbekannter Typ: Exception werfen, wir koennen diesen Zahlungslauf nicht berechnen
		throw new EbeguRuntimeException(
			"getZahlungslaufHelper",
			"No Implementation defined for ZahlungslaufTyp "
				+ zahlungslaufTyp
		);
	}

	/**
	 * Retrieves a {@link ZahlungslaufHelper} instance for the specified time period and payment run type.
	 * The method dynamically determines the appropriate {@link ZahlungslaufHelper}, considering factors
	 * such as the existence of Betreuung and specific settings. If no suitable {@link ZahlungslaufHelper}
	 * is found in the cache, a new instance is created using the {@link ZahlungslaufHelperFactory}.
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

			Gesuchsperiode gesuchsperiode = betreuung.extractGesuchsperiode();

			// integrate the mandant with the map key, to avoid overwriting helpers over mandants.
			String key = String.format(
				"%s_%s",
				principalBean.getMandant().getMandantIdentifier(),
				gesuchsperiode.getGueltigkeit()
					.getGueltigBis()
					.format(DateTimeFormatter.ISO_DATE)
			);

			if (helperMap.containsKey(
				key
			)) {
				zahlungslaufHelper = helperMap.get(
					key
				);
			}

			if (null == zahlungslaufHelper) {

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
				helperMap.put(
					key,
					zahlungslaufHelper
				);
			}
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

	/**
	 * Clears the cache of {@link ZahlungslaufHelper} instances maintained within the factory.
	 * This method removes all existing entries from the internal cache, ensuring that any
	 * subsequent request for a {@link ZahlungslaufHelper} results in a new instance being created
	 * or retrieved. This can be useful in scenarios where cached instances may no longer reflect
	 * the current application state or when updates in settings or configurations require a full
	 * refresh of the cached data.
	 */
	public void clearCache() {
		helperMap.clear();
	}
}
