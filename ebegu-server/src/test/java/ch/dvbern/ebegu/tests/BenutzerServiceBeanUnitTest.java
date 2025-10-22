/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.BenutzerExistException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BenutzerServiceBean;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(EasyMockExtension.class)
public class BenutzerServiceBeanUnitTest extends EasyMockSupport {

	@TestSubject
	private final BenutzerService benutzerServiceBean =
		new BenutzerServiceBean();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private FallService fallServiceMock;

	@Mock
	private GesuchService gesuchServiceMock;

	@Mock
	private CriteriaQueryHelper criteriaQueryHelper;

	@Mock
	private PrincipalBean principalBean;

	@Mock
	private KibonJwt kibonJwt;

	@Test
	public void testNotGesuchstellerRoleShouldReturnNull() {
		var benutzer = createBenutzerWithRole(UserRole.ADMIN_BG);
		var fallId = benutzerServiceBean
			.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
				benutzer
			);
		Assertions.assertNull(fallId);
	}

	@Test
	public void testGesuchstellerWithoutFallShouldReturnNull() {
		var benutzer = createBenutzerWithRole(UserRole.GESUCHSTELLER);
		createFallServiceMock(benutzer, null);
		replayAll();
		var fallId = benutzerServiceBean
			.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
				benutzer
			);
		Assertions.assertNull(fallId);
	}

	@Test
	public void testGesuchstellerWithFallAndNichtFreigegebenemGesuchShouldReturnFall() {
		var benutzer = createBenutzerWithRole(UserRole.GESUCHSTELLER);
		var fall = new Fall();
		createFallServiceMock(benutzer, fall);
		var gesuchIds = new ArrayList<String>();
		gesuchIds.add("abce");
		var gesuch = new Gesuch();
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		createGesuchServiceMock(fall, gesuchIds, gesuch);
		replayAll();
		var fallId = benutzerServiceBean
			.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
				benutzer
			);
		Assertions.assertEquals(fall.getId(), fallId);
	}

	@Test()
	public void testGesuchstellerWithFallAndVerfuegtemGesuchShouldThrowError() {
		var benutzer = createBenutzerWithRole(UserRole.GESUCHSTELLER);
		var fall = new Fall();
		createFallServiceMock(benutzer, fall);
		var gesuchIds = new ArrayList<String>();
		gesuchIds.add("abce");
		var gesuch = new Gesuch();
		gesuch.setStatus(AntragStatus.VERFUEGT);
		createGesuchServiceMock(fall, gesuchIds, gesuch);
		replayAll();
		Assertions.assertThrows(BenutzerExistException.class, () -> {
			benutzerServiceBean
				.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
					benutzer
				);
		});
	}

	private void createFallServiceMock(
		@Nonnull Benutzer benutzer,
		@Nullable Fall fall
	) {
		expect(fallServiceMock.findFallByBesitzer(benutzer))
			.andReturn(Optional.ofNullable(fall))
			.anyTimes();
	}

	private void createGesuchServiceMock(
		@Nonnull Fall fall,
		@Nonnull List<String> gesuchIds,
		@Nonnull Gesuch gesuch
	) {
		expect(gesuchServiceMock.getAllGesuchIDsForFall(fall.getId()))
			.andReturn(gesuchIds)
			.anyTimes();

		expect(gesuchServiceMock.findGesuch("abce", false))
			.andReturn(Optional.of(gesuch))
			.anyTimes();
	}

	private Benutzer createBenutzerWithRole(UserRole role) {
		var berechtigung = new Berechtigung();
		berechtigung.setRole(role);
		var dateRange = new DateRange();
		dateRange.setGueltigAb(LocalDate.ofYearDay(1900, 1));
		dateRange.setGueltigBis(LocalDate.ofYearDay(3000, 1));
		berechtigung.setGueltigkeit(dateRange);
		var berechtigungSet = new HashSet<Berechtigung>();
		berechtigungSet.add(berechtigung);
		var benutzer = new Benutzer();
		benutzer.setBerechtigungen(berechtigungSet);
		benutzer.setTimestampErstellt(LocalDateTime.now());
		return benutzer;
	}

	@Nested
	class FindBenutzer {
		String beloginUUID = "51113a10-a035-11ef-b6b5-a3df84c8de58";
		String keycloakUUID = "54d70486-a035-11ef-99ba-1f6159a5bc4a";

		@Test
		void mustFindUserWithExternalUUId() {
			// given
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(
				Optional.of(
					beloginUUID
				)
			);
			expect(kibonJwt.getExternalUUID()).andReturn(keycloakUUID);
			mockMandantBern();

			Benutzer keycloakBenutzer = new Benutzer();
			keycloakBenutzer.getBerechtigungen().add(new Berechtigung());
			expect(
				criteriaQueryHelper.getEntityByUniqueAttribute(
					Benutzer.class,
					keycloakUUID,
					Benutzer_.externalUUID
				)
			).andReturn(Optional.of(keycloakBenutzer));

			replayAll();

			// when
			var result = benutzerServiceBean.findBenutzer(kibonJwt);

			// then
			assertThat(result.get(), Is.is(keycloakBenutzer));
		}

		@Test
		void mustFindUserWithBernPrimaryId() {
			// given
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(
				Optional.of(
					beloginUUID
				)
			);
			expect(kibonJwt.getExternalUUID()).andReturn(keycloakUUID);
			mockMandantBern();

			expect(
				criteriaQueryHelper.getEntityByUniqueAttribute(
					Benutzer.class,
					keycloakUUID,
					Benutzer_.externalUUID
				)
			).andReturn(Optional.empty());

			Benutzer beLoginBenutzer = new Benutzer();
			beLoginBenutzer.getBerechtigungen().add(new Berechtigung());
			expect(
				criteriaQueryHelper.getEntityByUniqueAttribute(
					Benutzer.class,
					beloginUUID,
					Benutzer_.externalUUID
				)
			).andReturn(Optional.of(beLoginBenutzer));

			replayAll();

			// when
			var result = benutzerServiceBean.findBenutzer(kibonJwt);

			// then
			assertThat(result.get(), Is.is(beLoginBenutzer));
		}

		private void mockMandantBern() {
			Mandant mandant = new Mandant();
			mandant.setMandantIdentifier(MandantIdentifier.BERN);
			expect(principalBean.getMandant()).andReturn(mandant).anyTimes();
		}
	}
}
