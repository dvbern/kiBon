/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer_;
import ch.dvbern.ebegu.entities.Gesuchsteller_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchServiceBean;
import ch.dvbern.ebegu.services.SuperAdminService;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

@ExtendWith(EasyMockExtension.class)
class GesuchServiceBeanTest extends EasyMockSupport {

	private static final String GS2_MAIL = "gesuchsteller2@example.com";

	@TestSubject
	private GesuchServiceBean gesuchService;

	@Mock
	private Persistence persistence;

	@Mock
	private PrincipalBean principalBean;
	@Mock
	private DossierService dossierService;
	@Mock
	private CriteriaQueryHelper criteriaQueryHelper;
	@Mock
	private SuperAdminService superAdminService;
	@Mock
	private Authorizer authorizer;

	@Test
	void removeAntragAsGesuchstellerPapiergesuchNotAllowed() {
		// Als GS einloggen
		loginAs(GESUCHSTELLER);
		// Papier-Erstgesuch
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setTyp(AntragTyp.ERSTGESUCH);
		replayAll();

		try {
			gesuchService.removeAntrag(papierErstgesuch);
			Assertions.fail(
				"Exception erwartet. Gesuchsteller darf kein Papiergesuch löschen"
			);
		} catch (EbeguRuntimeException e) {
			Assertions.assertEquals(
				ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_GS,
				e.getErrorCodeEnum()
			);
		}
	}

	@Test
	void removeAntragAsGesuchstellerFreigegebenNotAllowed() {
		// Als GS einloggen
		loginAs(GESUCHSTELLER);
		// Online Gesuch, freigegeben
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);
		onlineGesuch.setStatus(AntragStatus.FREIGABEQUITTUNG);
		replayAll();

		try {
			gesuchService.removeAntrag(onlineGesuch);
			Assertions.fail(
				"Exception erwartet. Gesuchsteller darf ein freigegebenes Gesuch nicht mehr löschen"
			);
		} catch (EbeguRuntimeException e) {
			Assertions.assertEquals(
				ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
				e.getErrorCodeEnum()
			);
		}
	}

	@Test
	void removeAntragAsGesuchstellerAllowed() {
		// Als GS einloggen
		loginAs(GESUCHSTELLER);
		// Online Gesuch, freigegeben
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);
		onlineGesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		superAdminService.removeGesuch(onlineGesuch.getId());
		expectLastCall().andVoid();

		replayAll();

		gesuchService.removeAntrag(onlineGesuch);
	}

	@Test
	void removeAntragAsAdminOnlineNotAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Online Gesuch
		Gesuch onlineGesuch = TestDataUtil.createDefaultGesuch();
		onlineGesuch.setEingangsart(Eingangsart.ONLINE);

		expect(principalBean.isCallerInRole(GESUCHSTELLER)).andReturn(false);

		replayAll();

		try {
			gesuchService.removeAntrag(onlineGesuch);
			Assertions.fail(
				"Exception erwartet. Gemeinde darf ein online Gesuch nicht löschen"
			);
		} catch (EbeguRuntimeException e) {
			Assertions.assertEquals(
				ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_JA,
				e.getErrorCodeEnum()
			);
		}
	}

	@Test
	void removeAntragAsAdminVerfuegtNotAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Papier-Erstgesuch
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setStatus(AntragStatus.VERFUEGEN);

		expect(principalBean.isCallerInRole(GESUCHSTELLER)).andReturn(false);

		replayAll();

		try {
			gesuchService.removeAntrag(papierErstgesuch);
			Assertions.fail(
				"Gemeinde darf kein Gesuch löschen, das bereits im Status VERFUEGEN oder verfügt ist"
			);
		} catch (EbeguRuntimeException e) {
			Assertions.assertEquals(
				ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
				e.getErrorCodeEnum()
			);
		}
	}

	@Test
	void removeAntragAsAdminAllowed() {
		// Als Admin einloggen
		loginAs(UserRole.ADMIN_GEMEINDE);
		// Online Gesuch, freigegeben
		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);
		papierErstgesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);

		expect(principalBean.isCallerInRole(GESUCHSTELLER)).andReturn(false);

		expect(
			dossierService.findDossier(papierErstgesuch.getDossier().getId())
		).andReturn(
			Optional.ofNullable(
				papierErstgesuch.getDossier()
			)
		);

		expect(
			criteriaQueryHelper.getEntitiesByAttribute(
				Gesuch.class,
				papierErstgesuch.getDossier(),
				Gesuch_.dossier
			)
		).andReturn(List.of(papierErstgesuch));

		expect(
			dossierService.findDossiersByFall(
				papierErstgesuch.getFall().getId()
			)
		).andReturn(List.of());

		superAdminService.removeFall(papierErstgesuch.getFall());
		expectLastCall().andVoid();

		replayAll();

		gesuchService.removeAntrag(papierErstgesuch);
	}

	@Test
	void isFirstGesuchOnline_true() {
		GesuchService gesuchServicePartialMock =
			EasyMock.partialMockBuilder(GesuchServiceBean.class)
				.addMockedMethod("findErstgesuchForGesuch")
				.createMock();

		Gesuch onlineErstgesuch = TestDataUtil.createDefaultGesuch();
		onlineErstgesuch.setEingangsart(Eingangsart.ONLINE);

		Gesuch papierGesuch = TestDataUtil.createDefaultGesuch();
		papierGesuch.setEingangsart(Eingangsart.PAPIER);

		expect(gesuchServicePartialMock.findErstgesuchForGesuch(papierGesuch))
			.andReturn(onlineErstgesuch);
		replay(gesuchServicePartialMock);

		Assertions.assertEquals(
			gesuchServicePartialMock.isFirstGesuchOnline(papierGesuch),
			true
		);
	}

	@Test
	void isFirstGesuchOnline_false() {
		GesuchService gesuchServicePartialMock =
			EasyMock.partialMockBuilder(GesuchServiceBean.class)
				.addMockedMethod("findErstgesuchForGesuch")
				.createMock();

		Gesuch papierErstgesuch = TestDataUtil.createDefaultGesuch();
		papierErstgesuch.setEingangsart(Eingangsart.PAPIER);

		Gesuch papierGesuch = TestDataUtil.createDefaultGesuch();
		papierGesuch.setEingangsart(Eingangsart.PAPIER);

		expect(gesuchServicePartialMock.findErstgesuchForGesuch(papierGesuch))
			.andReturn(papierErstgesuch);
		replay(gesuchServicePartialMock);

		Assertions.assertEquals(
			gesuchServicePartialMock.isFirstGesuchOnline(papierGesuch),
			false
		);
	}

	private void loginAs(UserRole role) {
		Benutzer gesuchsteller = new Benutzer();
		gesuchsteller.setBerechtigungen(new HashSet<>());
		gesuchsteller.getBerechtigungen().add(new Berechtigung());
		gesuchsteller.setUsername("testuser");
		gesuchsteller.setRole(role);

		expect(principalBean.getBenutzer()).andReturn(gesuchsteller);
		expect(principalBean.isCallerInRole(anyString())).andReturn(false);
		expect(principalBean.isCallerInRole(role)).andReturn(true);
		expect(principalBean.isCallerInAnyOfRole(role)).andReturn(true);
		expect(principalBean.isCallerInAnyOfRole(anyObject(List.class)))
			.andReturn(false);
	}

	@Nested
	class GetMailOfGesuchForDossierWithLatestMutationOfGS2Test {

		@Test
		void getMailOfGesuchForDossierWithLatestMutationOfGS2_shouldReturnEmptyOptional_ifCriteriaResultIsEmpty() {
			Dossier dossier = TestDataUtil.createDefaultDossier();
			TypedQuery<String> typedQuery = mockMailOfGS2Query(dossier);
			expect(typedQuery.getResultList()).andReturn(List.of());

			replayAll();

			Assertions.assertEquals(
				Optional.empty(),
				gesuchService.getMailOfGesuchForDossierWithLatestMutationOfGS2(
					dossier
				)
			);
			verifyAll();
		}

		@Test
		void getMailOfGesuchForDossierWithLatestMutationOfGS2_shouldReturnEmail_ifCriteriaResultIsNotEmpty() {
			Dossier dossier = TestDataUtil.createDefaultDossier();
			TypedQuery<String> typedQuery = mockMailOfGS2Query(dossier);
			expect(typedQuery.getResultList()).andReturn(List.of(GS2_MAIL));

			replayAll();

			Assertions.assertEquals(
				Optional.of(GS2_MAIL),
				gesuchService.getMailOfGesuchForDossierWithLatestMutationOfGS2(
					dossier
				)
			);
			verifyAll();
		}

		@Test
		void getMailOfGesuchForDossierWithLatestMutationOfGS2_shouldReturnEmail_ifCriteriaResultIsNotEmptyButFirstElementIsNull() {
			Dossier dossier = TestDataUtil.createDefaultDossier();
			TypedQuery<String> typedQuery = mockMailOfGS2Query(dossier);
			expect(typedQuery.getResultList()).andReturn(
				Collections.singletonList(null)
			);
			replayAll();

			Assertions.assertEquals(
				Optional.empty(),
				gesuchService.getMailOfGesuchForDossierWithLatestMutationOfGS2(
					dossier
				)
			);
			verifyAll();
		}

		@Nonnull
		private TypedQuery<String> mockMailOfGS2Query(
			@Nonnull Dossier dossier
		) {
			CriteriaBuilder cb = mock(CriteriaBuilder.class);
			CriteriaQuery<String> query = mock(CriteriaQuery.class);
			Root<Gesuch> root = mock(Root.class);
			Join<Gesuch, GesuchstellerContainer> gesuchstellerJoin =
				mock(Join.class);
			Join<GesuchstellerContainer, Gesuchsteller> gesDataJoin =
				mock(Join.class);
			Path<Dossier> dossierPath = mock(Path.class);
			Path<String> mailPath = mock(Path.class);
			Path<LocalDateTime> timestampMutiertPath = mock(Path.class);
			Predicate gesuchOfDossier = mock(Predicate.class);
			Order orderByTimestampMutiert = mock(Order.class);
			EntityManager entityManager = mock(EntityManager.class);
			TypedQuery<String> typedQuery = mock(TypedQuery.class);

			authorizer.checkReadAuthorizationDossier(dossier);
			expectLastCall().andVoid();

			expect(persistence.getCriteriaBuilder()).andReturn(cb);
			expect(cb.createQuery(String.class)).andReturn(query);
			expect(query.from(Gesuch.class)).andReturn(root);
			expect(root.join(Gesuch_.gesuchsteller2, JoinType.INNER))
				.andReturn(gesuchstellerJoin);
			expect(
				gesuchstellerJoin.join(
					GesuchstellerContainer_.gesuchstellerJA,
					JoinType.INNER
				)
			).andReturn(gesDataJoin);
			expect(root.get(Gesuch_.dossier)).andReturn(dossierPath);
			expect(cb.equal(dossierPath, dossier)).andReturn(gesuchOfDossier);
			expect(gesDataJoin.get(Gesuchsteller_.mail)).andReturn(mailPath);
			expect(query.select(mailPath)).andReturn(query);
			expect(query.where(gesuchOfDossier)).andReturn(query);
			expect(gesDataJoin.get(Gesuchsteller_.timestampMutiert))
				.andReturn(timestampMutiertPath);
			expect(cb.desc(timestampMutiertPath))
				.andReturn(orderByTimestampMutiert);
			expect(query.orderBy(orderByTimestampMutiert)).andReturn(query);
			expect(persistence.getEntityManager()).andReturn(entityManager);
			expect(entityManager.createQuery(query)).andReturn(typedQuery);
			expect(typedQuery.setMaxResults(1)).andReturn(typedQuery);

			return typedQuery;
		}

	}
}
