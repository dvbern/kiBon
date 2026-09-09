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

package ch.dvbern.ebegu.services.berechtigung;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.berechtigung.Berechtigung;
import ch.dvbern.ebegu.entities.berechtigung.FutureBerechtigung;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BenutzerServiceBean;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static java.util.Objects.requireNonNull;

/**
 * The {@code BerechtigungExpirationRoutine} class is responsible for managing
 * the lifecycle of {@code Berechtigung} (permissions) entities associated with
 * a {@code Benutzer} (user). This includes handling the expiration of current
 * permissions, transitioning to future permissions if available, and managing
 * the creation or removal of associated permissions.
 *
 * This class performs the following operations:
 * - Identifies expired permissions for a user.
 * - Replaces expired permissions with future permissions if they exist.
 * - Creates new permissions when no future permissions are available.
 * - Ensures consistency of permissions by managing the validity date range.
 * - Handles the removal of expired permissions.
 *
 * The routine operates on the principle that permissions have a defined
 * validity period, and transitions should adhere to specific business rules:
 * - If a future permission becomes active, it replaces the current permission.
 * - If the new permission is time-limited, a succeeding unlimited future
 * permission is created.
 * - If no future permission exists, a default permission is created.
 *
 * Logging is used extensively to provide visibility into the actions performed
 * during the routine, such as when permissions are expired or replaced.
 */
@Stateless
public class BerechtigungExpirationRoutine {

	private static final Logger LOG = LoggerFactory.getLogger(
		BenutzerServiceBean.class.getSimpleName()
	);

	@Inject
	private Persistence persistence;

	@Inject
	private FutureBerechtigungServiceBean futureBerechtigungService;

	/**
	 * Handles the expiration of roles (Berechtigungen) for a specific user (Benutzer).
	 * Removes expired roles and processes future roles, replacing them with current ones
	 * based on defined logic.
	 *
	 * @param benutzer The Benutzer object for whom the expiration routine is executed.
	 * This includes checking the expiration of current roles, transitioning
	 * to future roles if present, or creating default roles if no future
	 * roles exist.
	 */
	public void runExpirationRoutine(Benutzer benutzer) {

		List<Berechtigung> abgelaufeneBerechtigungen = new ArrayList<>();
		for (Berechtigung berechtigung : benutzer.getBerechtigungen()) {
			if (berechtigung.isAbgelaufen()) {
				abgelaufeneBerechtigungen.add(berechtigung);
			}
		}

		Optional<FutureBerechtigung> optionalFutureBerechtigung =
			getOptionalFutureBerechtigung(benutzer);

		Berechtigung currentBerechtigung = benutzer
			.getCurrentBerechtigung();

		if (currentBerechtigung.isAbgelaufen()) {

			replaceExpiredCurrentBerechtigungForBenutzer(
				benutzer,
				optionalFutureBerechtigung,
				currentBerechtigung
			);
		}

		// Die abgelaufene Rolle löschen
		for (Berechtigung berechtigung : abgelaufeneBerechtigungen) {

			if (berechtigung.getGueltigkeit()
				.getGueltigBis()
				.isBefore(LocalDate.now().plusDays(1))) {
				// only remove Berechtigungen that are not a predecessor of a future Berechtigung
				benutzer.getBerechtigungen()
					.remove(berechtigung);
				removeBerechtigung(berechtigung);
			}

			LOG.info(
				"... Benutzerrolle ist abgelaufen: {}, war: {}, abgelaufen: {}",
				benutzer.getUsername(),
				berechtigung.getRole(),
				berechtigung.getGueltigkeit().getGueltigBis()
			);
		}

		persistence.merge(benutzer);
	}

	private void replaceExpiredCurrentBerechtigungForBenutzer(
		Benutzer benutzer,
		Optional<FutureBerechtigung> futureBerechtigung,
		Berechtigung abgelaufeneBerechtigung
	) {

		if (futureBerechtigung.isPresent()) {

			// if a future Berechtigung exists, replace the current Berechtigung with it
			Berechtigung newBerechtigungFromFuture =
				createBerechtigungFromFutureBerechtigungForBenutzer(
					benutzer,
					futureBerechtigung.get()
				);
			benutzer.getBerechtigungen().add(newBerechtigungFromFuture);

			// if the new Berechtigung is limited, create a new, unlimited future Berechtigung equal to the current Berechtigung.
			// This is per definition what happens when a future Berechtigung is limited (see KIBON-4547).
			LocalDate endOfNewBerechtigung = newBerechtigungFromFuture
				.getGueltigkeit()
				.getGueltigBis();
			if (endOfNewBerechtigung.isBefore(Constants.END_OF_TIME)) {

				// unlimit the successor
				DateRange successorGueltigkeit = new DateRange();
				successorGueltigkeit.setGueltigAb(
					endOfNewBerechtigung.plusDays(1)
				);
				successorGueltigkeit.setGueltigBis(Constants.END_OF_TIME);

				FutureBerechtigung successorBerechtigung =
					createFutureBerechtigungFromBerechtigungForBenutzer(
						abgelaufeneBerechtigung,
						successorGueltigkeit
					);

				benutzer.setFutureBerechtigung(successorBerechtigung);
			} else {
				benutzer.setFutureBerechtigung(null);
			}

			futureBerechtigungService.removeFutureBerechtigung(
				futureBerechtigung.get()
			);
		} else {
			// If no future Berechtigung exists, create a new one defaulting to Gesuchsteller
			Berechtigung berechtigung = new Berechtigung();
			berechtigung.setBenutzer(benutzer);
			berechtigung.setRole(GESUCHSTELLER);
			DateRange gueltigkeit = new DateRange();
			gueltigkeit.setGueltigAb(
				abgelaufeneBerechtigung.getGueltigkeit()
					.getGueltigBis()
					.plusDays(1)
			);
			berechtigung.setGueltigkeit(new DateRange());
			benutzer.getBerechtigungen().add(berechtigung);
		}
	}

	private Optional<FutureBerechtigung> getOptionalFutureBerechtigung(
		Benutzer benutzer
	) {
		if (benutzer.hasFutureBerechtigung()) {
			FutureBerechtigung futureBerechtigung = benutzer
				.getFutureBerechtigung();
			assert futureBerechtigung != null;
			LocalDate futureStart = futureBerechtigung.getGueltigkeit()
				.getGueltigAb();
			if (futureStart.isBefore(LocalDate.now().plusDays(1))) {
				return Optional.of(futureBerechtigung);
			}
		}
		return Optional.empty();
	}

	private static @NonNull Berechtigung createBerechtigungFromFutureBerechtigungForBenutzer(
		Benutzer benutzer,
		FutureBerechtigung futureBerechtigung
	) {
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setGueltigkeit(futureBerechtigung.getGueltigkeit());
		berechtigung.setRole(futureBerechtigung.getRole());
		berechtigung.setBenutzer(benutzer);
		berechtigung.getGemeindeList()
			.addAll(futureBerechtigung.getGemeindeList());
		berechtigung.setInstitution(futureBerechtigung.getInstitution());
		berechtigung.setSozialdienst(futureBerechtigung.getSozialdienst());
		berechtigung.setTraegerschaft(futureBerechtigung.getTraegerschaft());

		return berechtigung;
	}

	private static @NonNull FutureBerechtigung createFutureBerechtigungFromBerechtigungForBenutzer(
		Berechtigung berechtigung,
		DateRange newGueltigkeit
	) {
		if (null == newGueltigkeit) {
			newGueltigkeit = berechtigung.getGueltigkeit();
		}
		FutureBerechtigung futureBerechtigung = new FutureBerechtigung();
		futureBerechtigung.setGueltigkeit(newGueltigkeit);
		futureBerechtigung.setRole(berechtigung.getRole());
		futureBerechtigung.setBenutzer(berechtigung.getBenutzer());
		futureBerechtigung.getGemeindeList()
			.addAll(berechtigung.getGemeindeList());
		futureBerechtigung.setInstitution(berechtigung.getInstitution());
		futureBerechtigung.setSozialdienst(berechtigung.getSozialdienst());
		futureBerechtigung.setTraegerschaft(berechtigung.getTraegerschaft());

		return futureBerechtigung;
	}

	private void removeBerechtigung(@Nonnull Berechtigung berechtigung) {
		requireNonNull(berechtigung);
		persistence.remove(berechtigung);
	}
}
