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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchServiceBean;
import ch.dvbern.ebegu.services.SuperAdminService;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

@ExtendWith(EasyMockExtension.class)
class GesuchServiceBeanTest extends EasyMockSupport {

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
	private FallService fallService;
	@Mock
	private SuperAdminService superAdminService;

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
}
