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
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.berechtigung.Berechtigung;
import ch.dvbern.ebegu.entities.berechtigung.FutureBerechtigung;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class BerechtigungExpirationRoutineTest extends EasyMockSupport {

	@TestSubject
	private final BerechtigungExpirationRoutine routine =
		new BerechtigungExpirationRoutine();

	@Mock
	private Persistence persistence;

	@Mock
	private FutureBerechtigungServiceBean futureBerechtigungService;

	@Test
	@DisplayName("Eine abgelaufene Berechtigung wird durch die aktive "
		+ "FutureBerechtigung ersetzt")
	void shouldReplaceAbgelaufeneBerechtigungWithFutureBerechtigung() {
		Berechtigung abgelaufene = createBerechtigung(
			UserRole.SACHBEARBEITER_BG,
			new DateRange(
				LocalDate.now().minusYears(1),
				LocalDate.now().minusDays(1)
			)
		);
		Benutzer benutzer = createBenutzer(abgelaufene);
		FutureBerechtigung future = createFutureBerechtigung(
			UserRole.ADMIN_BG,
			new DateRange(LocalDate.now(), Constants.END_OF_TIME)
		);
		benutzer.setFutureBerechtigung(future);

		futureBerechtigungService.removeFutureBerechtigung(future);
		expectLastCall();
		persistence.remove(abgelaufene);
		expectLastCall();
		expect(persistence.merge(benutzer)).andReturn(benutzer);
		replayAll();

		routine.runExpirationRoutine(benutzer);

		verifyAll();
		assertThat(benutzer.getBerechtigungen(), hasSize(1));
		Berechtigung neue = benutzer.getCurrentBerechtigung();
		assertThat(neue.getRole(), is(UserRole.ADMIN_BG));
		assertThat(neue.getBenutzer(), is(benutzer));
		assertThat(neue.getGueltigkeit(), is(future.getGueltigkeit()));
		assertThat(benutzer.getFutureBerechtigung(), nullValue());
	}

	@Test
	@DisplayName("Bei einer befristeten FutureBerechtigung wird eine "
		+ "unbefristete Nachfolge-FutureBerechtigung erstellt")
	void shouldCreateUnlimitedSuccessorForLimitedFutureBerechtigung() {
		DateRange gueltigkeitAbgelaufene = new DateRange(
			LocalDate.now().minusYears(1),
			LocalDate.now().minusDays(1)
		);
		Berechtigung abgelaufene = createBerechtigung(
			UserRole.SACHBEARBEITER_BG,
			gueltigkeitAbgelaufene
		);
		Benutzer benutzer = createBenutzer(abgelaufene);
		LocalDate endeFuture = LocalDate.now().plusMonths(1);
		FutureBerechtigung future = createFutureBerechtigung(
			UserRole.ADMIN_BG,
			new DateRange(LocalDate.now(), endeFuture)
		);
		benutzer.setFutureBerechtigung(future);

		futureBerechtigungService.removeFutureBerechtigung(future);
		expectLastCall();
		persistence.remove(abgelaufene);
		expectLastCall();
		expect(persistence.merge(benutzer)).andReturn(benutzer);
		replayAll();

		routine.runExpirationRoutine(benutzer);

		verifyAll();
		FutureBerechtigung successor = benutzer.getFutureBerechtigung();
		assertThat(successor, notNullValue());
		assertThat(successor.getRole(), is(UserRole.SACHBEARBEITER_BG));
		assertThat(
			successor.getGueltigkeit().getGueltigAb(),
			is(endeFuture.plusDays(1))
		);
		assertThat(
			successor.getGueltigkeit().getGueltigBis(),
			is(Constants.END_OF_TIME)
		);
	}

	@Test
	@DisplayName("Ohne FutureBerechtigung wird eine Gesuchsteller-Berechtigung "
		+ "erstellt")
	void shouldCreateGesuchstellerBerechtigungWhenNoFutureBerechtigung() {
		Berechtigung abgelaufene = createBerechtigung(
			UserRole.SACHBEARBEITER_BG,
			new DateRange(
				LocalDate.now().minusYears(1),
				LocalDate.now().minusDays(1)
			)
		);
		Benutzer benutzer = createBenutzer(abgelaufene);

		persistence.remove(abgelaufene);
		expectLastCall();
		expect(persistence.merge(benutzer)).andReturn(benutzer);
		replayAll();

		routine.runExpirationRoutine(benutzer);

		verifyAll();
		assertThat(benutzer.getBerechtigungen(), hasSize(1));
		Berechtigung neue = benutzer.getCurrentBerechtigung();
		assertThat(neue.getRole(), is(UserRole.GESUCHSTELLER));
		assertThat(neue.getBenutzer(), is(benutzer));
		assertThat(neue.getGueltigkeit().getGueltigAb(), is(LocalDate.now()));
		assertThat(
			neue.getGueltigkeit().getGueltigBis(),
			is(Constants.END_OF_TIME)
		);
	}

	@Test
	@DisplayName("Eine noch nicht aktive FutureBerechtigung wird nicht "
		+ "verwendet")
	void shouldIgnoreFutureBerechtigungStartingInTheFuture() {
		Berechtigung abgelaufene = createBerechtigung(
			UserRole.SACHBEARBEITER_BG,
			new DateRange(
				LocalDate.now().minusYears(1),
				LocalDate.now().minusDays(1)
			)
		);
		Benutzer benutzer = createBenutzer(abgelaufene);
		FutureBerechtigung future = createFutureBerechtigung(
			UserRole.ADMIN_BG,
			new DateRange(LocalDate.now().plusDays(2), Constants.END_OF_TIME)
		);
		benutzer.setFutureBerechtigung(future);

		persistence.remove(abgelaufene);
		expectLastCall();
		expect(persistence.merge(benutzer)).andReturn(benutzer);
		replayAll();

		routine.runExpirationRoutine(benutzer);

		verifyAll();
		assertThat(benutzer.getFutureBerechtigung(), is(future));
		assertThat(benutzer.getBerechtigungen(), hasSize(1));
		assertThat(
			benutzer.getCurrentBerechtigung().getRole(),
			is(UserRole.GESUCHSTELLER)
		);
	}

	@Test
	@DisplayName("Eine noch gueltige Berechtigung wird nicht geloescht")
	void shouldNotRemoveGueltigeBerechtigung() {
		Berechtigung gueltige = createBerechtigung(
			UserRole.SACHBEARBEITER_BG,
			new DateRange(
				LocalDate.now().minusYears(1),
				LocalDate.now().plusYears(1)
			)
		);
		Benutzer benutzer = createBenutzer(gueltige);
		FutureBerechtigung future = createFutureBerechtigung(
			UserRole.ADMIN_BG,
			new DateRange(LocalDate.now(), Constants.END_OF_TIME)
		);
		benutzer.setFutureBerechtigung(future);

		expect(persistence.merge(benutzer)).andReturn(benutzer);
		replayAll();

		routine.runExpirationRoutine(benutzer);

		verifyAll();
		assertThat(benutzer.getBerechtigungen(), hasSize(1));
	}

	@Nonnull
	private Benutzer createBenutzer(@Nullable Berechtigung berechtigung) {
		Benutzer benutzer = new Benutzer();
		benutzer.setUsername("testuser");
		if (berechtigung != null) {
			berechtigung.setBenutzer(benutzer);
			TreeSet<Berechtigung> berechtigungen = new TreeSet<>();
			berechtigungen.add(berechtigung);
			benutzer.setBerechtigungen(berechtigungen);
		}

		return benutzer;
	}

	@Nonnull
	private Berechtigung createBerechtigung(
		@Nonnull UserRole role,
		@Nonnull DateRange gueltigkeit
	) {
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(role);
		berechtigung.setGueltigkeit(gueltigkeit);

		return berechtigung;
	}

	@Nonnull
	private FutureBerechtigung createFutureBerechtigung(
		@Nonnull UserRole role,
		@Nonnull DateRange gueltigkeit
	) {
		FutureBerechtigung futureBerechtigung = new FutureBerechtigung();
		futureBerechtigung.setRole(role);
		futureBerechtigung.setGueltigkeit(gueltigkeit);

		return futureBerechtigung;
	}
}
