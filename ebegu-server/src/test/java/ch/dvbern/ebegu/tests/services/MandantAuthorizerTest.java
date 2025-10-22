/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests.services;

import java.time.LocalDate;
import java.util.Set;

import jakarta.ejb.EJBAccessException;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.RollenAbhaengigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.authentication.MandantAuthorizer;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
class MandantAuthorizerTest extends EasyMockSupport {

	@TestSubject
	private final MandantAuthorizer authorizer = new MandantAuthorizer();

	@Mock
	private PrincipalBean principalMock;

	@Test
	public void readFallAllowedForMandant() {
		var fall = createFall();
		addMocksForFallAndReplay(fall.getMandant());
		authorizer.checkMandantMatches(fall);
	}

	@Test()
	public void readFallNotAllowedForMandant() {
		var fall = createFallLuzern();
		addMocksForFallAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(
			EJBAccessException.class,
			() -> authorizer.checkMandantMatches(fall)
		);
	}

	@Test
	public void writeFallAllowedForMandant() {
		var fall = createFall();
		addMocksForFallAndReplay(fall.getMandant());
		authorizer.checkMandantMatches(fall);
	}

	@Test()
	public void writeFallNotAllowedForMandant() {
		var fall = createFallLuzern();
		addMocksForFallAndReplay(TestDataUtil.getMandantKantonBern());
		Assertions.assertThrows(
			EJBAccessException.class,
			() -> authorizer.checkMandantMatches(fall)
		);
	}

	private Fall createFall() {
		return TestDataUtil.createDefaultFall();
	}

	private void addMocksForFallAndReplay(Mandant mandant) {
		expect(principalMock.isKibonBenutzer()).andReturn(false);
		expect(principalMock.getPrincipal()).andReturn(null);
		expect(principalMock.discoverRoles()).andReturn(null);
		expect(principalMock.isKibonServiceAccount()).andReturn(false);
		expect(principalMock.getMandant()).andReturn(mandant);
		expect(
			principalMock.isCallerInAnyOfRole(
				UserRole.ADMIN_MANDANT,
				UserRole.SACHBEARBEITER_MANDANT
			)
		).andReturn(true);
		expect(
			principalMock.isCallerInAnyOfRole(
				UserRole.SUPER_ADMIN,
				UserRole.ADMIN_BG,
				UserRole.SACHBEARBEITER_BG,
				UserRole.ADMIN_GEMEINDE,
				UserRole.SACHBEARBEITER_GEMEINDE,
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				UserRole.ADMIN_INSTITUTION,
				UserRole.SACHBEARBEITER_INSTITUTION,
				UserRole.ADMIN_TS,
				UserRole.SACHBEARBEITER_TS,
				UserRole.STEUERAMT,
				UserRole.JURIST,
				UserRole.REVISOR,
				UserRole.ADMIN_MANDANT,
				UserRole.SACHBEARBEITER_MANDANT,
				UserRole.ADMIN_SOZIALDIENST,
				UserRole.SACHBEARBEITER_SOZIALDIENST
			)
		).andReturn(true);
		replayAll();
	}

	private void addMocksForZahlungenAndReplay(
		Mandant mandant,
		Benutzer benutzer
	) {
		expect(principalMock.isKibonBenutzer()).andReturn(false);
		expect(principalMock.discoverRoles()).andReturn(null);
		expect(principalMock.getPrincipal()).andReturn(null);
		expect(principalMock.isKibonServiceAccount()).andReturn(false);
		expect(principalMock.getMandant()).andReturn(mandant);
		expect(principalMock.getBenutzer()).andReturn(benutzer);
		expect(
			principalMock.isCallerInAnyOfRole(
				UserRole.getRolesWithoutAbhaengigkeit(
					RollenAbhaengigkeit.GEMEINDE
				)
			)
		).andReturn(true);
		replayAll();
	}

	private Fall createFallLuzern() {
		var fall = createFall();
		fall.setMandant(TestDataUtil.getMandantLuzern());
		return fall;
	}

	@Test
	public void readZahlungsauftragAllowedForMandant() {
		addMocksForZahlungenAndReplay(
			TestDataUtil.getMandantLuzern(),
			getMandantBenutzer()
		);
		authorizer.checkMandantMatches(
			createZahlungsauftragLuzern()
		);
	}

	@Test
	public void readZahlungsauftragNotAllowedForMandant() {
		addMocksForZahlungenAndReplay(
			TestDataUtil.getMandantKantonBern(),
			getMandantBenutzer()
		);
		Zahlungsauftrag zahlungsauftragLuzern = createZahlungsauftragLuzern();
		Assertions.assertThrows(
			EJBAccessException.class,
			() -> authorizer.checkMandantMatches(
				zahlungsauftragLuzern
			)
		);
	}

	@Test
	public void readZahlungAllowedForMandant() {
		addMocksForZahlungenAndReplay(
			TestDataUtil.getMandantLuzern(),
			getMandantBenutzer()
		);
		authorizer.checkMandantMatches(
			createZahlungLuzern().getZahlungsauftrag()
		);
	}

	@Test
	public void readZahlungNotAllowedForMandant() {
		addMocksForZahlungenAndReplay(
			TestDataUtil.getMandantKantonBern(),
			getMandantBenutzer()
		);
		Zahlungsauftrag zahlungsauftrag = createZahlungLuzern()
			.getZahlungsauftrag();
		Assertions.assertThrows(
			EJBAccessException.class,
			() -> authorizer.checkMandantMatches(
				zahlungsauftrag
			)
		);
	}

	private Zahlung createZahlungLuzern() {
		var zahlung = new Zahlung();
		zahlung.setZahlungsauftrag(createZahlungsauftragLuzern());
		return zahlung;
	}

	private Zahlungsauftrag createZahlungsauftragLuzern() {
		var zahlungsauftrag = new Zahlungsauftrag();
		var mandant = TestDataUtil.getMandantLuzern();
		var gemeinde = new Gemeinde();
		gemeinde.setMandant(mandant);
		zahlungsauftrag.setGemeinde(gemeinde);
		zahlungsauftrag.setMandant(mandant);
		return zahlungsauftrag;
	}

	private Benutzer getMandantBenutzer() {
		var benutzer = new Benutzer();
		var berechtigung = new Berechtigung();
		berechtigung.setRole(UserRole.ADMIN_MANDANT);
		berechtigung.setGueltigkeit(
			new DateRange(
				LocalDate.of(2019, 1, 1),
				LocalDate.of(2100, 1, 1)
			)
		);
		benutzer.setBerechtigungen(Set.of(berechtigung));
		return benutzer;
	}

}
