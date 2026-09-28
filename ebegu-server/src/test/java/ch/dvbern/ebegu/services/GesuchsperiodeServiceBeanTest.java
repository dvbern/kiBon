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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class GesuchsperiodeServiceBeanTest extends EasyMockSupport {

	public static final String EXCLUDED = "excluded";

	@TestSubject
	private GesuchsperiodeServiceBean service =
		new GesuchsperiodeServiceBean();

	@Mock
	private PrincipalBean principalBean;

	@Mock
	private Persistence persistence;

	@Mock
	private GesuchsperiodeStatuswechselService gesuchsperiodeStatuswechselService;

	@Test
	void saveGesuchsperiode_delegatesStatusChangeHandlingToStatuswechselService() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);

		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		expect(principalBean.getBenutzer()).andReturn(benutzer).anyTimes();
		expect(principalBean.isCallerInRole(UserRole.SUPER_ADMIN))
			.andReturn(true)
			.anyTimes();
		gesuchsperiodeStatuswechselService.handleStatusChange(
			gp,
			GesuchsperiodeStatus.AKTIV
		);
		expectLastCall();
		expect(persistence.merge(gp)).andReturn(gp);
		replayAll();

		service.saveGesuchsperiode(gp, GesuchsperiodeStatus.AKTIV);

		verifyAll();
	}

	@Test
	void findEarliestOtherAktivGesuchsperiodeStart_shouldFindEarliestOtherActiveGesuchsperiodeStart_excludingGivenIdAndInactiveGesuchsperioden() {
		mockGetAllGesuchsperioden();

		Gesuchsperiode excluded = createGesuchsperiode(
			EXCLUDED,
			GesuchsperiodeStatus.AKTIV,
			LocalDate.of(2023, 1, 1)
		);
		Gesuchsperiode inactive = createGesuchsperiode(
			"inactive",
			GesuchsperiodeStatus.INAKTIV,
			LocalDate.of(2022, 1, 1)
		);
		Gesuchsperiode later = createGesuchsperiode(
			"later",
			GesuchsperiodeStatus.AKTIV,
			LocalDate.of(2025, 1, 1)
		);
		Gesuchsperiode earliest = createGesuchsperiode(
			"earliest",
			GesuchsperiodeStatus.AKTIV,
			LocalDate.of(2024, 1, 1)
		);

		expect(service.getAllGesuchsperioden(excluded.getMandant()))
			.andReturn(List.of(excluded, inactive, later, earliest));

		replayAll();

		assertThat(
			service.findEarliestOtherAktivGesuchsperiodeStart(
				excluded.getMandant(),
				EXCLUDED
			),
			is(Optional.of(LocalDate.of(2024, 1, 1)))
		);

		verifyAll();
	}

	@Test
	void findEarliestOtherAktivGesuchsperiodeStart_shouldReturnEmpty_whenThereIsNoOtherActiveGesuchsperiode() {
		mockGetAllGesuchsperioden();

		Gesuchsperiode gesuchsperiode = createGesuchsperiode(
			EXCLUDED,
			GesuchsperiodeStatus.AKTIV,
			LocalDate.of(2024, 1, 1)
		);
		expect(service.getAllGesuchsperioden(gesuchsperiode.getMandant()))
			.andReturn(
				List.of(
					gesuchsperiode
				)
			);

		replayAll();

		assertThat(
			service.findEarliestOtherAktivGesuchsperiodeStart(
				gesuchsperiode.getMandant(),
				EXCLUDED
			),
			is(Optional.empty())
		);

		verifyAll();
	}

	private static Gesuchsperiode existingAktivGesuchsperiode() {
		Gesuchsperiode gp = TestDataUtil.createGesuchsperiode1718();
		// Simulate so isNew() returns false and the
		// new-period copy in saveGesuchsperiode is skipped
		gp.setTimestampErstellt(LocalDateTime.now());
		gp.setStatus(GesuchsperiodeStatus.AKTIV);
		return gp;
	}

	private static Gesuchsperiode createGesuchsperiode(
		String id,
		GesuchsperiodeStatus status,
		LocalDate gueltigAb
	) {
		Gesuchsperiode gp = TestDataUtil.createGesuchsperiode1718();
		gp.setId(id);
		gp.setStatus(status);
		gp.getGueltigkeit().setGueltigAb(gueltigAb);
		return gp;
	}

	private void mockGetAllGesuchsperioden() {
		service = partialMockBuilder(GesuchsperiodeServiceBean.class)
			.addMockedMethod("getAllGesuchsperioden", Mandant.class)
			.createMock();
	}
}
