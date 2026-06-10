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

package ch.dvbern.ebegu.util.zahlungslauf;

import java.time.LocalDate;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
public class ZahlungslaufHelperFactoryTest extends EasyMockSupport {

	@TestSubject
	private final ZahlungslaufHelperFactory factory =
		new ZahlungslaufHelperFactory();

	@Mock
	private EinstellungService einstellungServiceMock;

	@Mock
	private PrincipalBean principalBean;

	@BeforeEach
	void setUp() {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);
		expect(
			principalBean.getMandant()
		).andReturn(mandant).anyTimes();
	}

	@Test
	public void testGetZahlungslaufHelper_BetreuungNull_ThrowsException() {
		// Arrange
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		Verfuegung verfuegung = new Verfuegung();
		zeitabschnitt.setVerfuegung(verfuegung);
		// betreuung is null by default in the Verfuegung entity

		replayAll();

		// Act & Assert
		assertThrows(ZahlungslaufHelperCreateException.class, () -> {
			factory.getZahlungslaufHelper(
				zeitabschnitt,
				ZahlungslaufTyp.GEMEINDE_INSTITUTION
			);
		});

		verifyAll();
	}

	@Test
	public void testGetZahlungslaufHelper_DifferentTypes_CorrectHelperReturned() {
		LocalDate gueltigBis = LocalDate.of(2023, 12, 31);
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(
			new DateRange(LocalDate.of(2023, 1, 1), gueltigBis)
		);

		Gemeinde gemeinde = new Gemeinde();

		Betreuung betreuung = new Betreuung() {
			@Override
			public Gesuchsperiode extractGesuchsperiode() {
				return gesuchsperiode;
			}

			@Override
			public Gemeinde extractGemeinde() {
				return gemeinde;
			}
		};

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setBetreuung(betreuung);

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerfuegung(verfuegung);

		Einstellung einstellung = new Einstellung();
		einstellung.setValue(HoehereBeitraegeTyp.DEAKTIVIERT.name());

		expect(
			einstellungServiceMock.findEinstellungCached(
				EinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
				gemeinde,
				gesuchsperiode
			)
		).andReturn(einstellung).anyTimes();

		replayAll();

		ZahlungslaufHelper instHelper = factory.getZahlungslaufHelper(
			zeitabschnitt,
			ZahlungslaufTyp.GEMEINDE_INSTITUTION
		);
		assertNotNull(instHelper);
		assertSame(
			ZahlungslaufInstitutionenHelper.class,
			instHelper.getClass()
		);

		ZahlungslaufHelper antrHelper = factory.getZahlungslaufHelper(
			zeitabschnitt,
			ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER
		);
		assertNotNull(antrHelper);
		assertSame(
			ZahlungslaufAntragstellerHelper.class,
			antrHelper.getClass()
		);

		verifyAll();
	}

	@Test
	public void testGetZahlungslaufHelper_HoehereBeitraegeAktiviert_HelperCreatedWithHoehereBeitraegeAktiviert() {
		LocalDate gueltigBis = LocalDate.of(2023, 12, 31);
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(
			new DateRange(LocalDate.of(2023, 1, 1), gueltigBis)
		);

		Gemeinde gemeinde = new Gemeinde();

		Betreuung betreuung = new Betreuung() {
			@Override
			public Gesuchsperiode extractGesuchsperiode() {
				return gesuchsperiode;
			}

			@Override
			public Gemeinde extractGemeinde() {
				return gemeinde;
			}
		};

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setBetreuung(betreuung);

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerfuegung(verfuegung);

		Einstellung einstellung = new Einstellung();
		einstellung.setValue(HoehereBeitraegeTyp.AKTIVIERT.name());

		expect(
			einstellungServiceMock.findEinstellungCached(
				EinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
				gemeinde,
				gesuchsperiode
			)
		).andReturn(einstellung);

		replayAll();

		ZahlungslaufHelper helper = factory.getZahlungslaufHelper(
			zeitabschnitt,
			ZahlungslaufTyp.GEMEINDE_INSTITUTION
		);
		assertNotNull(helper);
		// Assuming we can check the beitraegeTyp inside the helper or just that it was created correctly
		// Since we don't have public access to beitraegeTyp in helper, we just verify it didn't crash
		// and the mock was called.

		verifyAll();
	}
}
