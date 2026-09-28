/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kapselt alle Regeln und Nebenwirkungen beim Statuswechsel einer
 * {@link Gesuchsperiode}: Logging, Validierung des Übergangs, Mail-Kandidaten
 * bei ENTWURF → AKTIV, Löschen der VersendeteMails bei AKTIV → INAKTIV und
 * Verfügbarkeitsprüfung bei Übergang auf GESCHLOSSEN.
 * <p>
 * {@link #handleStatusChange} mutiert die übergebene Gesuchsperiode
 * (setzt bei ENTWURF → AKTIV das {@code datumAktiviert}); der Aufrufer ist
 * für das anschliessende Persistieren zuständig.
 */
@Stateless
public class GesuchsperiodeStatuswechselService {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		GesuchsperiodeStatuswechselService.class
	);

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GesuchsperiodeEmailService gesuchsperiodeEmailService;

	@Inject
	private VersendeteMailsService versendeteMailsService;

	public void handleStatusChange(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GesuchsperiodeStatus statusBisher
	) {
		// Alle Statusuebergaenge werden geloggt
		logStatusChange(gesuchsperiode, statusBisher);
		// Superadmin darf alles
		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)
			&& !isStatusUebergangValid(
				statusBisher,
				gesuchsperiode.getStatus()
			)) {
			throw new EbeguRuntimeException(
				"handleStatusChange",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_INVALID_STATUSUEBERGANG,
				statusBisher,
				gesuchsperiode.getStatus()
			);
		}
		// Falls es ein Statuswechsel war, und der neue Status ist AKTIV -> Mail an alle Gesuchsteller schicken
		// Nur, wenn die Gesuchsperiode noch nie auf aktiv geschaltet war.
		if (GesuchsperiodeStatus.AKTIV == gesuchsperiode.getStatus()
			&& gesuchsperiode.getDatumAktiviert() == null) {
			Optional<Gesuchsperiode> lastGesuchsperiodeOptional =
				gesuchsperiodeService.getGesuchsperiodeAm(
					gesuchsperiode.getGueltigkeit()
						.getGueltigAb()
						.minusDays(1),
					gesuchsperiode.getMandant()
				);
			if (lastGesuchsperiodeOptional.isPresent()) {
				gesuchsperiodeEmailService
					.getAndSaveGesuchsperiodeEmailCandidates(
						lastGesuchsperiodeOptional.get(),
						gesuchsperiode
					);
				gesuchsperiode.setDatumAktiviert(LocalDate.now());
			}
		}
		// Beim Wechsel von AKTIV -> INAKTIV: alle versendeten Mails dieses Mandanten,
		// die bis und mit dem Periodenende (gueltigBis) verschickt wurden, werden gelöscht.
		// Cutoff ist der Beginn des Folgetages, damit die Delete-Bedingung `<` true ist.
		// Double Check: nur löschen, wenn keine andere noch-AKTIVE Periode desselben
		// Mandanten früher beginnt als der Cutoff
		if (GesuchsperiodeStatus.INAKTIV == gesuchsperiode.getStatus()
			&& GesuchsperiodeStatus.AKTIV == statusBisher) {
			LocalDate cutoffDate = gesuchsperiode.getGueltigkeit()
				.getGueltigBis()
				.plusDays(1);
			Optional<LocalDate> earliestRemainingAktivStart =
				gesuchsperiodeService.findEarliestOtherAktivGesuchsperiodeStart(
					gesuchsperiode.getMandant(),
					gesuchsperiode.getId()
				);
			boolean cutoffIsSafe = earliestRemainingAktivStart
				.map(earliest -> !cutoffDate.isAfter(earliest))
				.orElse(true);
			if (cutoffIsSafe) {
				MandantIdentifier mandantIdentifier =
					gesuchsperiode.getMandant().getMandantIdentifier();
				versendeteMailsService.deleteVersendeteMailsBeforeAsync(
					cutoffDate.atStartOfDay(),
					mandantIdentifier
				);
			} else {
				LOGGER.warn(
					"Skipping versendeteMails cleanup for gesuchsperiode {} "
						+ "because a still-aktive periode starts earlier than the cutoff. "
						+ "cutoff={} earliestRemainingAktivStart={}",
					gesuchsperiode.getId(),
					cutoffDate,
					earliestRemainingAktivStart.get()
				);
			}
		}
		// Prüfen, dass ALLE Gesuche dieser Periode im Status "Verfügt" oder "Schulamt" sind. Sind noch
		// Gesuche in Bearbeitung, oder in Beschwerde etc. darf nicht geschlossen werden!
		if (GesuchsperiodeStatus.GESCHLOSSEN == gesuchsperiode.getStatus()
			&& !gesuchService.canGesuchsperiodeBeClosed(gesuchsperiode)) {
			throw new EbeguRuntimeException(
				"handleStatusChange",
				ErrorCodeEnum.ERROR_GESUCHSPERIODE_CANNOT_BE_CLOSED
			);
		}
	}

	private boolean isStatusUebergangValid(
		GesuchsperiodeStatus statusBefore,
		GesuchsperiodeStatus statusAfter
	) {
		if (GesuchsperiodeStatus.ENTWURF == statusBefore) {
			return GesuchsperiodeStatus.AKTIV == statusAfter;
		}
		if (GesuchsperiodeStatus.AKTIV == statusBefore) {
			return GesuchsperiodeStatus.INAKTIV == statusAfter;
		}
		if (GesuchsperiodeStatus.INAKTIV == statusBefore) {
			return GesuchsperiodeStatus.GESCHLOSSEN == statusAfter;
		}
		return false;
	}

	private void logStatusChange(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GesuchsperiodeStatus statusBisher
	) {
		LOGGER.info("****************************************************");
		LOGGER.info("Status Gesuchsperiode wurde geändert:");
		LOGGER.info("Benutzer: {}", principalBean.getBenutzer().getUsername());
		LOGGER.info(
			"Gesuchsperiode: {} ({}" + ')',
			gesuchsperiode.getGesuchsperiodeString(),
			gesuchsperiode.getId()
		);
		LOGGER.info("Neuer Status: {}", gesuchsperiode.getStatus());
		LOGGER.info("Bisheriger Status: {}", statusBisher);
		LOGGER.info("****************************************************");
	}
}
