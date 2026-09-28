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
import java.util.Optional;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
class GesuchsperiodeStatuswechselServiceTest extends EasyMockSupport {

	@TestSubject
	private final GesuchsperiodeStatuswechselService service =
		new GesuchsperiodeStatuswechselService();

	@Mock
	private PrincipalBean principalBean;

	@Mock
	private GesuchsperiodeService gesuchsperiodeService;

	@Mock
	private GesuchService gesuchService;

	@Mock
	private GesuchsperiodeEmailService gesuchsperiodeEmailService;

	@Mock
	private VersendeteMailsService versendeteMailsService;

	@Test
	void aktivToInaktiv_triggersAsyncMailDelete_withGueltigBisAndMandant() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);

		LocalDateTime expectedCutoff = gp.getGueltigkeit()
			.getGueltigBis()
			.plusDays(1)
			.atStartOfDay();
		MandantIdentifier expectedMandant = gp.getMandant()
			.getMandantIdentifier();

		expectLogAndValidCallerRole();
		expect(
			gesuchsperiodeService.findEarliestOtherAktivGesuchsperiodeStart(
				eq(gp.getMandant()),
				eq(gp.getId())
			)
		).andReturn(Optional.empty());
		versendeteMailsService.deleteVersendeteMailsBeforeAsync(
			expectedCutoff,
			expectedMandant
		);
		expectLastCall();
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.AKTIV);

		verifyAll();
	}

	@Test
	void inaktivToGeschlossen_doesNotTriggerMailDelete() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.GESCHLOSSEN);

		expectLogAndValidCallerRole();
		expect(gesuchService.canGesuchsperiodeBeClosed(gp)).andReturn(true);
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.INAKTIV);

		verifyAll();
	}

	@Test
	void aktivToInaktiv_skipsMailDelete_whenAnotherAktivPeriodeStartsBeforeCutoff() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);
		// The still-aktiv periode starts before the (deactivated newer periode's) cutoff.
		LocalDate earlierAktivStart = gp.getGueltigkeit()
			.getGueltigAb()
			.minusYears(1);

		expectLogAndValidCallerRole();
		expect(
			gesuchsperiodeService.findEarliestOtherAktivGesuchsperiodeStart(
				eq(gp.getMandant()),
				eq(gp.getId())
			)
		).andReturn(Optional.of(earlierAktivStart));
		// No expectation on versendeteMailsService.deleteVersendeteMailsBeforeAsync,
		// so the strict mock will fail if the guard doesn't short-circuit.
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.AKTIV);

		verifyAll();
	}

	@Test
	void aktivToInaktiv_triggersMailDelete_whenDeactivatedPeriodeIsOldestAktiv() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);
		// Another aktiv periode exists but starts AFTER this periode's cutoff.
		LocalDate laterAktivStart = gp.getGueltigkeit()
			.getGueltigBis()
			.plusDays(1);

		LocalDateTime expectedCutoff = gp.getGueltigkeit()
			.getGueltigBis()
			.plusDays(1)
			.atStartOfDay();
		MandantIdentifier expectedMandant = gp.getMandant()
			.getMandantIdentifier();

		expectLogAndValidCallerRole();
		expect(
			gesuchsperiodeService.findEarliestOtherAktivGesuchsperiodeStart(
				eq(gp.getMandant()),
				eq(gp.getId())
			)
		).andReturn(Optional.of(laterAktivStart));
		versendeteMailsService.deleteVersendeteMailsBeforeAsync(
			expectedCutoff,
			expectedMandant
		);
		expectLastCall();
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.AKTIV);

		verifyAll();
	}

	@Test
	void invalidStatusUebergang_throws_whenCallerIsNotSuperAdmin() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);
		// ENTWURF -> INAKTIV is not a valid transition
		expectLog();
		expect(principalBean.isCallerInRole(UserRole.SUPER_ADMIN))
			.andReturn(false);
		replayAll();

		assertThrows(
			EbeguRuntimeException.class,
			() -> service.handleStatusChange(gp, GesuchsperiodeStatus.ENTWURF)
		);

		verifyAll();
	}

	@Test
	void invalidStatusUebergang_isAllowed_whenCallerIsSuperAdmin() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.INAKTIV);
		// Statusbisher ENTWURF, neuer Status INAKTIV — invalid state-machine, but
		// SUPER_ADMIN can bypass. Since neither AKTIV nor GESCHLOSSEN branches
		// fire, no downstream mocks should be touched.
		expectLogAndValidCallerRole();
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.ENTWURF);

		verifyAll();
	}

	@Test
	void entwurfToAktiv_triggersEmailCandidates_andSetsDatumAktiviert_whenPreviousPeriodeExists() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setDatumAktiviert(null);
		Gesuchsperiode previousGp = TestDataUtil.createGesuchsperiodeXXYY(
			2016,
			2017
		);
		LocalDate expectedStichtag = gp.getGueltigkeit()
			.getGueltigAb()
			.minusDays(1);

		expectLogAndValidCallerRole();
		expect(
			gesuchsperiodeService.getGesuchsperiodeAm(
				eq(expectedStichtag),
				eq(gp.getMandant())
			)
		).andReturn(Optional.of(previousGp));
		gesuchsperiodeEmailService.getAndSaveGesuchsperiodeEmailCandidates(
			previousGp,
			gp
		);
		expectLastCall();
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.ENTWURF);

		verifyAll();
		assertNotNull(gp.getDatumAktiviert());
	}

	@Test
	void entwurfToAktiv_skipsEmailAndDatumAktiviert_whenNoPreviousPeriode() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setDatumAktiviert(null);
		LocalDate expectedStichtag = gp.getGueltigkeit()
			.getGueltigAb()
			.minusDays(1);

		expectLogAndValidCallerRole();
		expect(
			gesuchsperiodeService.getGesuchsperiodeAm(
				eq(expectedStichtag),
				eq(gp.getMandant())
			)
		).andReturn(Optional.empty());
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.ENTWURF);

		verifyAll();
		assertNull(gp.getDatumAktiviert());
	}

	@Test
	void geschlossen_throws_whenGesuchsperiodeCannotBeClosed() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.GESCHLOSSEN);

		expectLogAndValidCallerRole();
		expect(gesuchService.canGesuchsperiodeBeClosed(gp)).andReturn(false);
		replayAll();

		assertThrows(
			EbeguRuntimeException.class,
			() -> service.handleStatusChange(gp, GesuchsperiodeStatus.INAKTIV)
		);

		verifyAll();
	}

	@Test
	void geschlossen_passes_whenGesuchsperiodeCanBeClosed() {
		Gesuchsperiode gp = existingAktivGesuchsperiode();
		gp.setStatus(GesuchsperiodeStatus.GESCHLOSSEN);

		expectLogAndValidCallerRole();
		expect(gesuchService.canGesuchsperiodeBeClosed(gp)).andReturn(true);
		replayAll();

		service.handleStatusChange(gp, GesuchsperiodeStatus.INAKTIV);

		verifyAll();
	}

	private void expectLogAndValidCallerRole() {
		expectLog();
		expect(principalBean.isCallerInRole(UserRole.SUPER_ADMIN))
			.andReturn(true);
	}

	private void expectLog() {
		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		expect(principalBean.getBenutzer()).andReturn(benutzer);
	}

	private static Gesuchsperiode existingAktivGesuchsperiode() {
		Gesuchsperiode gp = TestDataUtil.createGesuchsperiode1718();
		gp.setStatus(GesuchsperiodeStatus.AKTIV);
		return gp;
	}
}
