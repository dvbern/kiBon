/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.REF_NUMMER;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
public class BetreuungStornierenEventHandlerTest extends EasyMockSupport {

	private static final String CLIENT_NAME = "foo";
	private static final LocalDateTime EVENT_TIME = LocalDateTime.now();

	@TestSubject
	private final BetreuungStornierenEventHandler handler =
		new BetreuungStornierenEventHandler();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungService betreuungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private MitteilungService mitteilungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock(MockType.NICE)
	private BetreuungMonitoringService betreuungMonitoringService;

	private Gesuch gesuch_1GS = null;
	private Gemeinde gemeinde = null;
	private EventMonitor eventMonitor = null;

	@BeforeEach
	void setUp() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(
			2020,
			2021
		);
		gemeinde = TestDataUtil.createGemeindeParis();
		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(
				gesuchsperiode,
				false,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode)
			);
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
		eventMonitor = new EventMonitor(
			betreuungMonitoringService,
			EVENT_TIME,
			REF_NUMMER,
			CLIENT_NAME
		);
	}

	@ParameterizedTest
	@EnumSource(value = Betreuungsstatus.class,
		names = { "VERFUEGT", "BESTAETIGT", "GESCHLOSSEN_OHNE_VERFUEGUNG" },
		mode = Mode.INCLUDE)
	void isMutationsMitteilungStatus(@Nonnull Betreuungsstatus status) {
		assertThat(handler.isMutationsMitteilungStatus(status), is(true));
	}

	@Nested
	class IgnoreEventTest {

		@Test
		void ignoreEventWhenNoBetreuungFound() {
			expect(
				betreuungService.findBetreuungByReferenzNummer(
					REF_NUMMER,
					false
				)
			)
				.andReturn(Optional.empty());

			testIgnored("Betreuung nicht gefunden.");
		}

		@ParameterizedTest
		@EnumSource(value = GesuchsperiodeStatus.class,
			names = "AKTIV",
			mode = Mode.EXCLUDE)
		void ignoreEventWhenPeriodeNotAktiv(
			@Nonnull GesuchsperiodeStatus status
		) {
			Betreuung betreuung = betreuungWithSingleContainer();
			betreuung.extractGesuchsperiode().setStatus(status);

			expectBetreuungFound(betreuung);

			testIgnored("Die Gesuchsperiode ist nicht aktiv.");
		}

		@Test
		void ignoreEventWhenBetreuungMutiertAfterEventTimestamp() {
			Betreuung betreuung = betreuungWithSingleContainer();

			LocalDateTime betreuungMutiertTime = EVENT_TIME.plusSeconds(1);
			betreuung.setTimestampMutiert(betreuungMutiertTime);

			expectBetreuungFound(betreuung);

			testIgnored(
				"Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde."
			);
		}

		@Test
		void ignoreEventWhenNoExternalClient() {
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			expect(
				betreuungEventHelper.getExternalClients(
					CLIENT_NAME,
					betreuung
				)
			)
				.andReturn(new InstitutionExternalClients());
			expect(
				betreuungEventHelper.clientNotFoundFailure(
					CLIENT_NAME,
					betreuung
				)
			)
				.andReturn(
					Processing.failure(
						"Kein InstitutionExternalClient Namens ist der Institution zugewiesen"
					)
				);

			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(
				result,
				failed(
					stringContainsInOrder(
						"Kein InstitutionExternalClient Namens",
						"ist der Institution",
						"zugewiesen"
					)
				)
			);
			verifyAll();
		}

		@Test
		void ignoreEventWhenClientGueltigkeitOutsidePeriode() {
			Betreuung betreuung = betreuungWithSingleContainer();

			expectBetreuungFound(betreuung);
			mockClient(new DateRange(2022));

			testIgnored(
				"Der Client hat innerhalb der Periode keine Berechtigung."
			);
		}

		private void testIgnored(@Nonnull String message) {
			replayAll();

			Processing result = handler.attemptProcessing(eventMonitor);
			assertThat(result, failed(message));
			verifyAll();
		}
	}

	@SuppressWarnings("MethodOnlyUsedFromInnerClass")
	private void expectBetreuungFound(@Nonnull Betreuung foundBetreuung) {
		expect(
			betreuungService.findBetreuungByReferenzNummer(
				REF_NUMMER,
				false
			)
		)
			.andReturn(Optional.of(foundBetreuung));
	}

	@SuppressWarnings("MethodOnlyUsedFromInnerClass")
	private void mockClient(@Nonnull DateRange clientGueltigkeit) {
		InstitutionExternalClient institutionExternalClient = mock(
			InstitutionExternalClient.class
		);

		expect(
			betreuungEventHelper.getExternalClients(
				eq(CLIENT_NAME),
				EasyMock.<Betreuung>anyObject()
			)
		)
			.andReturn(
				new InstitutionExternalClients(
					institutionExternalClient,
					Collections.emptyList()
				)
			);

		expect(institutionExternalClient.getGueltigkeit())
			.andReturn(clientGueltigkeit);
	}

	@SuppressWarnings("MethodOnlyUsedFromInnerClass")
	@Nonnull
	private Betreuung betreuungWithSingleContainer() {
		return PlatzbestaetigungTestUtil.betreuungWithSingleContainer(
			gesuch_1GS
		);
	}
}
