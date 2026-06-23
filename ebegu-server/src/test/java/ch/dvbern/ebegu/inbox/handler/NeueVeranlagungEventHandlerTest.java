/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.persistence.EntityManager;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.OIDCServiceException;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;
import org.easymock.Capture;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.failed;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(EasyMockExtension.class)
public class NeueVeranlagungEventHandlerTest extends EasyMockSupport {

	private NeueVeranlagungEventDTO dto;
	private Gesuch gesuch_1GS = null;
	private String zpvNummer = "1000001";
	private SteuerdatenResponse steuerdatenResponse;
	private KibonAnfrageContext kibonAnfrageContext;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private MitteilungService mitteilungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private KibonAnfrageHandler kibonAnfrageHandler;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private FinanzielleSituationService finanzielleSituationService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private EinstellungService einstellungService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Persistence persistence;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private WizardStepService wizardStepService;

	@TestSubject
	private final NeueVeranlagungEventHandler handler =
		new NeueVeranlagungEventHandler();

	@BeforeEach
	void setUp() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(
			2020,
			2021
		);
		Gemeinde gemeinde = TestDataUtil.createGemeindeParis();
		List<InstitutionStammdaten> institutionStammdatenList =
			new ArrayList<>();
		institutionStammdatenList.add(
			TestDataUtil.createInstitutionStammdatenKitaWeissenstein()
		);
		institutionStammdatenList.add(
			TestDataUtil.createInstitutionStammdatenKitaBruennen()
		);
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
		gesuch_1GS.setEingangsart(Eingangsart.ONLINE);
		gesuch_1GS.getDossier().getFall().setBesitzer(new Benutzer());
		Objects.requireNonNull(gesuch_1GS.getDossier().getFall().getBesitzer());
		gesuch_1GS.getDossier().getFall().getBesitzer().setZpvNummer(zpvNummer);
		dto = new NeueVeranlagungEventDTO();
		dto.setZpvNummer(1000001);
		Objects.requireNonNull(gesuch_1GS.getGesuchsteller1());
		dto.setGeburtsdatum(
			gesuch_1GS.getGesuchsteller1()
				.getGesuchstellerJA()
				.getGeburtsdatum()
		);
		gesuch_1GS.getGesuchsteller1()
			.getGesuchstellerJA()
			.setZpvNummer(zpvNummer);
		dto.setGesuchsperiodeBeginnJahr(gesuchsperiode.getBasisJahrPlus1());
		dto.setKibonAntragId(gesuch_1GS.getId());
		steuerdatenResponse = NeueVeranlagungTestUtil
			.createSteuerdatenResponseAleine(dto);
		Objects.requireNonNull(
			gesuch_1GS.getGesuchsteller1()
				.getFinanzielleSituationContainer()
		);
		gesuch_1GS.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		gesuch_1GS.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);

		kibonAnfrageContext = new KibonAnfrageContext(
			gesuch_1GS,
			GesuchstellerTyp.GESUCHSTELLER_1,
			zpvNummer,
			null
		);
	}

	@Test
	void gesuchIdUnbekannt() {
		expectGesuchNotFound();

		testIgnored("Kein Gesuch für Key gefunden. Key: ");
	}

	@Test
	void gesuchNochNichtFreigegeben() {
		gesuch_1GS.setStatus(AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
		expectGesuchFound();
		testIgnored("Gesuch ist in Bearbeitung bei der Sozialdienst");
	}

	@Test
	void steuerAbfrageNichtErfolgreich() throws OIDCServiceException {
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(new FinanzielleSituationResultateDTO());
		expect(
			kibonAnfrageHandler.handleKibonAnfrage(
				gesuch_1GS,
				GesuchstellerTyp.GESUCHSTELLER_1
			)
		).andReturn(kibonAnfrageContext);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		testIgnored("Keine neue Veranlagung gefunden");
	}

	@Test
	void shouldNotCreateMitteilungIfNewestVerfuegtesGesuchHasNoPermission() {
		Gesuch mockMut = TestDataUtil.createDefaultGesuch(
			AntragStatus.VERFUEGT
		);
		mockMut.setGesuchsteller1(new GesuchstellerContainer());
		mockMut.getGesuchsteller1()
			.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(false);
		expectGesuchFound();
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.of(mockMut));
		replayAll();
	}

	@Test
	void shouldReturnFailedProcessingIfNewestVerfuegtesGesuchHasNoPermission() {
		Gesuch mockMut = TestDataUtil.createDefaultGesuch(
			AntragStatus.VERFUEGT
		);
		mockMut.setGesuchsteller1(new GesuchstellerContainer());
		mockMut.getGesuchsteller1()
			.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(false);
		expectGesuchFound();
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.of(mockMut));
		replayAll();
		Processing processing = handler.attemptProcessing(
			gesuch_1GS.getId(),
			handler.convertNeueVeranlagungEventDTOToDomainDTO(dto)
		);
		assertThat(processing.getState(), is(ProcessingState.FAILURE));
	}

	@Test
	void shouldReturnProcessingWithLogMessageThatPermissionWasNotGivenIfNewestVerfuegtesGesuchHasNoPermission() {
		Gesuch mockMut = TestDataUtil.createDefaultGesuch(
			AntragStatus.VERFUEGT
		);
		mockMut.setGesuchsteller1(new GesuchstellerContainer());
		mockMut.getGesuchsteller1()
			.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		mockMut.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(false);
		expectGesuchFound();
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.of(mockMut));
		replayAll();
		Processing processing = handler.attemptProcessing(
			gesuch_1GS.getId(),
			handler.convertNeueVeranlagungEventDTOToDomainDTO(dto)
		);
		assertThat(
			processing.getMessage(),
			stringContainsInOrder(
				"weil die Zustimmung der Erziehungsberechtigten im aktuellen Gesuch/Mutation",
				"nicht gegeben ist"
			)
		);
	}

	@Test
	void steuerdatenResponseNichtRechtskraeftig() throws OIDCServiceException {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(
			SteuerdatenAnfrageStatus.PROVISORISCH
		);
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(new FinanzielleSituationResultateDTO());
		expect(
			kibonAnfrageHandler.handleKibonAnfrage(
				gesuch_1GS,
				GesuchstellerTyp.GESUCHSTELLER_1
			)
		).andReturn(kibonAnfrageContext);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		testIgnored("Die neue Veranlagung ist noch nicht Rechtskraeftig");
	}

	@Test
	void finSitUnterschiedGleich() throws OIDCServiceException {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(
			SteuerdatenAnfrageStatus.RECHTSKRAEFTIG
		);
		expectGesuchFound();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(50)
		);
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(new FinanzielleSituationResultateDTO());
		expect(
			kibonAnfrageHandler.handleKibonAnfrage(
				gesuch_1GS,
				GesuchstellerTyp.GESUCHSTELLER_1
			)
		).andReturn(kibonAnfrageContext);
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(new FinanzielleSituationResultateDTO());
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		testIgnored(
			"Keine Meldung erstellt. Das massgebende Einkommen hat sich um 0 Franken verändert. Der konfigurierte Schwellenwert "
				+ "zur Benachrichtigung liegt bei 50 Franken"
		);
	}

	@Test
	void finSitUnterschiedMehrAberNichtGenuegen() throws OIDCServiceException {
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(60)
		);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		testIgnored(
			"Keine Meldung erstellt. Das massgebende Einkommen hat sich um 60 Franken verändert."
				+ " Der konfigurierte Schwellenwert zur Benachrichtigung liegt bei 60 Franken"
		);
	}

	@Test
	void createsNeueVeranlagungMitteilung() throws OIDCServiceException {
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(50)
		);
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expectMitteilungIsSend(gemeindeStammdaten);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		testProcessingSuccess();
	}

	@Test
	void getGesuchstellendenAlsString() throws OIDCServiceException {
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(50)
		);
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		Capture<NeueVeranlagungsMitteilung> subjectCapture =
			expectMitteilungIsSendWithCapture(gemeindeStammdaten);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));

		testProcessingSuccess();
		assertThat(
			subjectCapture.getValue().getSubject(),
			is("Steuerveranlagung 2019 Dagmar Wälti, Periode 2020/2021")
		);
	}

	@Test
	void getGesuchstellendenGemeinsamAlsString() throws OIDCServiceException {
		gesuch_1GS.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setGemeinsameSteuererklaerung(true);
		gesuch_1GS.setGesuchsteller2(new GesuchstellerContainer());
		gesuch_1GS.getGesuchsteller2().setGesuchstellerJA(new Gesuchsteller());
		gesuch_1GS.getGesuchsteller2().getGesuchstellerJA().setNachname("Wiki");
		gesuch_1GS.getGesuchsteller2().getGesuchstellerJA().setVorname("John");
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(50)
		);
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		Capture<NeueVeranlagungsMitteilung> subjectCapture =
			expectMitteilungIsSendWithCapture(gemeindeStammdaten);
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));

		testProcessingSuccess();
		assertThat(
			subjectCapture.getValue().getSubject(),
			is(
				"Steuerveranlagung 2019 Dagmar Wälti und John Wiki, Periode 2020/2021"
			)
		);
	}

	@Test
	void createsNeueVeranlagungMitteilungWhenZugunstenAntragsteller()
		throws OIDCServiceException {
		//Einkommen sinkt um 1 CHF
		expectEverythingUntilCompare(BigDecimal.valueOf(99999));
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(70)
		);
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		expectMitteilungIsSend(gemeindeStammdaten);
		testProcessingSuccess();
	}

	@Test
	void createsNeueVeranalgungMitteilungWhenGesuchMarkiert()
		throws OIDCServiceException {
		//Einkommen bleibt gleich
		expectEverythingUntilCompare(BigDecimal.valueOf(100000));
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		// gesuch ist markiert
		kibonAnfrageContext.getGesuch().setMarkiertFuerKontroll(true);
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(70)
		);
		GemeindeStammdaten gemeindeStammdaten = new GemeindeStammdaten();
		expectMitteilungIsSend(gemeindeStammdaten);
		expectLastCall();
		testProcessingSuccess();
	}

	@ParameterizedTest
	@EnumSource(
		value = AntragStatus.class,
		names = { "IN_BEARBEITUNG_GS",
			"FREIGABEQUITTUNG", "FREIGEGEBEN" },
		mode = Mode.INCLUDE
	)
	void updateFinSitSteuerabragestatusWennGesuchNochNichtBekanntBeiGemeinde(
		AntragStatus antragStatus
	)
		throws OIDCServiceException {
		gesuch_1GS.setStatus(antragStatus);
		expectEverythingUntilCompare();
		Einstellung einstellung = findEinstellungMinUnterschied();
		expect(einstellung.getValueAsBigDecimal()).andReturn(
			new BigDecimal(50)
		);
		WizardStep wizardStep = new WizardStep();
		wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		//Expect Wizardstep + neue veranlagungsstatus
		expect(
			wizardStepService.findWizardStepFromGesuch(
				gesuch_1GS.getId(),
				WizardStepName.FINANZIELLE_SITUATION
			)
		).andReturn(
			wizardStep
		);

		Capture<WizardStep> wizardStepCapture = newCapture();
		expect(wizardStepService.saveWizardStep(capture(wizardStepCapture)))
			.andReturn(new WizardStep());
		Capture<FinanzielleSituation> finanzielleSituationCapture =
			newCapture();
		expect(persistence.merge(capture(finanzielleSituationCapture)))
			.andReturn(new FinanzielleSituation());
		expect(gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch_1GS))
			.andReturn(Optional.ofNullable(gesuch_1GS));
		expect(persistence.find(Gesuch.class, gesuch_1GS.getId())).andReturn(
			gesuch_1GS
		);
		EntityManager em = createMock(EntityManager.class);
		expect(persistence.getEntityManager()).andReturn(em);
		em.refresh(gesuch_1GS);
		expectLastCall();
		testProcessingSuccess();
		assertThat(
			wizardStepCapture.getValue().getWizardStepStatus(),
			is(WizardStepStatus.IN_BEARBEITUNG)
		);
		assertThat(
			finanzielleSituationCapture.getValue()
				.getSteuerdatenAbfrageStatus(),
			is(SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG)
		);
	}

	private void testIgnored(@Nonnull String message) {
		replayAll();

		Processing result = handler.attemptProcessing(
			gesuch_1GS.getId(),
			handler.convertNeueVeranlagungEventDTOToDomainDTO(dto)
		);
		assertThat(result, failed(stringContainsInOrder(message)));
		verifyAll();
	}

	private void expectGesuchNotFound() {
		expect(gesuchService.findGesuch(gesuch_1GS.getId())).andReturn(
			Optional.empty()
		);
	}

	private void expectGesuchFound() {
		expect(gesuchService.findGesuch(gesuch_1GS.getId())).andReturn(
			Optional.of(gesuch_1GS)
		);
	}

	private void expectEverythingUntilCompare() throws OIDCServiceException {
		expectEverythingUntilCompare(BigDecimal.valueOf(100060));
	}

	private void expectEverythingUntilCompare(BigDecimal einkommenNeu)
		throws OIDCServiceException {
		kibonAnfrageContext.setSteuerdatenAnfrageStatus(
			SteuerdatenAnfrageStatus.RECHTSKRAEFTIG
		);
		kibonAnfrageContext.setSteuerdatenResponse(steuerdatenResponse);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOOrig =
			new FinanzielleSituationResultateDTO();
		finanzielleSituationResultateDTOOrig.setMassgebendesEinkVorAbzFamGr(
			new BigDecimal(100000)
		);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTONeu =
			new FinanzielleSituationResultateDTO();
		finanzielleSituationResultateDTONeu.setMassgebendesEinkVorAbzFamGr(
			einkommenNeu
		);
		expectGesuchFound();
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(
				finanzielleSituationResultateDTOOrig
			);
		expect(
			kibonAnfrageHandler.handleKibonAnfrage(
				gesuch_1GS,
				GesuchstellerTyp.GESUCHSTELLER_1
			)
		).andReturn(
			kibonAnfrageContext
		);
		expect(finanzielleSituationService.calculateResultate(anyObject()))
			.andReturn(
				finanzielleSituationResultateDTONeu
			);
	}

	private void expectMitteilungIsSend(GemeindeStammdaten gemeindeStammdaten) {
		expectVorSendenMitteilung(gemeindeStammdaten);
		expect(mitteilungService.sendNeueVeranlagungsmitteilung(anyObject()))
			.andReturn(new NeueVeranlagungsMitteilung());
	}

	private Capture<NeueVeranlagungsMitteilung> expectMitteilungIsSendWithCapture(
		GemeindeStammdaten gemeindeStammdaten
	) {
		expectVorSendenMitteilung(gemeindeStammdaten);
		Capture<NeueVeranlagungsMitteilung> capturedMitteilung = newCapture();
		//noinspection ConstantConditions
		expect(
			mitteilungService.sendNeueVeranlagungsmitteilung(
				capture(capturedMitteilung)
			)
		).andReturn(new NeueVeranlagungsMitteilung());

		return capturedMitteilung;
	}

	private void expectVorSendenMitteilung(
		GemeindeStammdaten gemeindeStammdaten
	) {
		expect(gemeindeService.getGemeindeStammdatenByGemeindeId(anyObject()))
			.andReturn(Optional.of(gemeindeStammdaten));
		expect(
			gesuchService.getAllGesucheIdsForDossierAndPeriod(
				kibonAnfrageContext.getGesuch().getDossier(),
				kibonAnfrageContext.getGesuch().getGesuchsperiode()
			)
		).andReturn(new ArrayList<>());
		expect(
			mitteilungService
				.findOffeneNeueVeranlagungsmitteilungenForGesuch(
					new ArrayList<>()
				)
		).andReturn(new ArrayList<>());
		expect(persistence.find(Gesuch.class, gesuch_1GS.getId())).andReturn(
			gesuch_1GS
		);
		EntityManager em = createMock(EntityManager.class);
		expect(persistence.getEntityManager()).andReturn(em);
		em.refresh(gesuch_1GS);
		expectLastCall();
	}

	private Einstellung findEinstellungMinUnterschied() {
		Einstellung einstellung = mock(Einstellung.class);
		List<Einstellung> einstellungs = new ArrayList<>();
		einstellungs.add(einstellung);
		expect(
			einstellungService.findEinstellungen(
				EinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK,
				gesuch_1GS.getGesuchsperiode()
			)
		)
			.andReturn(einstellungs);
		return einstellung;
	}

	private void testProcessingSuccess() {
		replayAll();
		Processing result = handler.attemptProcessing(
			gesuch_1GS.getId(),
			handler.convertNeueVeranlagungEventDTOToDomainDTO(dto)
		);
		assertThat(result.isProcessingSuccess(), is(true));
		verifyAll();
	}
}
