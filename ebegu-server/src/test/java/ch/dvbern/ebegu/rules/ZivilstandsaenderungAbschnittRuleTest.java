/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class ZivilstandsaenderungAbschnittRuleTest extends EasyMockSupport {
	@Mock
	private AbstractPlatz mockPlatz;
	@Mock
	private Gesuch mockGesuch;
	@Mock(MockType.NICE)
	private Familiensituation mockFamiliensituation;
	@Mock(MockType.NICE)
	private Familiensituation mockFamiliensituationErstgesuch;
	@Mock
	private Gesuchsperiode mockGesuchsperiode;
	@Mock(MockType.NICE)
	private Mandant mockMandant;
	@TestSubject
	private ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule =
		new ZivilstandsaenderungAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			1,
			Locale.GERMAN
		);

	private LocalDate gesuchsperiodeBis;
	private LocalDate stichtag;

	@BeforeEach
	void setUp() {
		// define the Gueltigkeit dynamically
		gesuchsperiodeBis = LocalDate.now().plusDays(30);
		stichtag = LocalDate.now();
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(
			LocalDate.now().withDayOfMonth(1).minusMonths(1)
		);
		gueltigkeit.setGueltigBis(gesuchsperiodeBis);

		// Setup common mock expectations
		expect(mockPlatz.extractGesuch()).andReturn(mockGesuch).anyTimes();
		expect(mockGesuch.extractFamiliensituation()).andReturn(
			mockFamiliensituation
		).anyTimes();
		expect(mockGesuch.extractFamiliensituationErstgesuch()).andReturn(
			mockFamiliensituationErstgesuch
		).anyTimes();
		expect(mockGesuch.getGesuchsperiode()).andReturn(mockGesuchsperiode)
			.anyTimes();
		expect(mockGesuchsperiode.getGueltigkeit()).andReturn(gueltigkeit)
			.anyTimes();
		expect(mockFamiliensituation.isSpezialFallAR()).andReturn(false)
			.anyTimes();
		expect(mockFamiliensituationErstgesuch.isSpezialFallAR()).andReturn(
			false
		).anyTimes();
		expect(mockGesuch.extractMandant()).andReturn(mockMandant).anyTimes();
		expect(mockMandant.getMandantIdentifier()).andReturn(
			MandantIdentifier.BERN
		).anyTimes();
		expect(
			mockFamiliensituation.hasSecondGesuchsteller(
				gueltigkeit.getGueltigBis()
			)
		).andReturn(true).anyTimes();
		expect(
			mockFamiliensituationErstgesuch.hasSecondGesuchsteller(
				gueltigkeit.getGueltigBis()
			)
		).andReturn(false).anyTimes();
		expect(mockFamiliensituation.getAenderungPer()).andReturn(stichtag)
			.anyTimes();
	}

	@ParameterizedTest
	@EnumSource(
		value = EnumFamilienstatus.class,
		names = { "SCHWYZ", },
		mode = Mode.EXCLUDE
	)
	void testCreateVerfuegungsZeitabschnitte_ChangeDetected(
		EnumFamilienstatus enumFamilienstatus
	) {
		expect(mockFamiliensituation.getFamilienstatus()).andReturn(
			enumFamilienstatus
		).once();

		replayAll();

		List<VerfuegungZeitabschnitt> result = zivilstandsaenderungAbschnittRule
			.createVerfuegungsZeitabschnitte(mockPlatz);

		// Assert that the new FamSit start the month after the AenderungPer Date until the end of the Gesuchsperiode
		assertThat(
			result.get(1).getGueltigkeit(),
			is(
				new DateRange(
					stichtag.plusMonths(1).withDayOfMonth(1),
					gesuchsperiodeBis
				)
			)
		);

		verifyAll();
	}

	@Test
	void testCreateVerfuegungsZeitabschnitte_Schwyz() {
		expect(mockFamiliensituation.getFamilienstatus()).andReturn(
			EnumFamilienstatus.SCHWYZ
		).once();
		expect(mockGesuch.getRegelStartDatum()).andReturn(
			stichtag.minusMonths(2)
		).anyTimes();

		replayAll();

		List<VerfuegungZeitabschnitt> result = zivilstandsaenderungAbschnittRule
			.createVerfuegungsZeitabschnitte(mockPlatz);

		// Assert that the new FamSit start at the beginning of the Gesuchsperiode until the end of the Gesuchsperiode
		assertThat(
			mockGesuchsperiode.getGueltigkeit(),
			is(result.get(1).getGueltigkeit())
		);

		verifyAll();
	}
}
