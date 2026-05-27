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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.mitteilung.MitteilungServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Es gibt schon eine MitteilungServiceBeanTest
 * Diese ist eine leichtere Version mit Mock anstatt Arquillian die betrifft nur die bearbeitung von neue Veranlagung
 * Mitteilungen
 */
@ExtendWith(EasyMockExtension.class)
@SuppressWarnings("ConstantConditions")
public class NeueVeranlagungMitteilungAHVTest extends EasyMockSupport {

	private final String versicherungsNummer1 = "7569989812071";
	private final Long versicherungsNummerPartner = 7566319697445L;

	private Gesuchsperiode gesuchsperiode;
	private Dossier dossier;
	private Gemeinde gemeinde;
	private NeueVeranlagungsMitteilung neueVeranlagungsMitteilung;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Authorizer authorizer;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private FinanzielleSituationService finanzielleSituationService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Persistence persistence;

	@TestSubject
	private final MitteilungServiceBean mitteilungServiceBean =
		new MitteilungServiceBean();

	@BeforeEach
	public void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		dossier = TestDataUtil.createDefaultDossier();
		gemeinde = TestDataUtil.createGemeindeParis();
		neueVeranlagungsMitteilung = new NeueVeranlagungsMitteilung();

	}

	@Test
	public void neueVeranlaungsMitteilung1GSSteuerresponseGemeinsamRejected() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setSozialversicherungsNrPartner(
			versicherungsNummerPartner
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);
		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		replayAll();
		testExceptionWithErrorCode(
			ErrorCodeEnum.ERROR_FIN_SIT_ALLEIN_NEUE_VERANLAGUNG_GEMEINSAM
		);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung2GSSteuerresponseGemeinsamRejected() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);
		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		replayAll();
		testExceptionWithErrorCode(
			ErrorCodeEnum.ERROR_FIN_SIT_GEMEINSAM_NEUE_VERANLAGUNG_ALLEIN
		);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung1GSOk() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);
		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		expect(
			finanzielleSituationService.saveFinanzielleSituation(
				anyObject(),
				anyObject()
			)
		).andReturn(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
		);
		expect(persistence.merge(neueVeranlagungsMitteilung)).andReturn(
			neueVeranlagungsMitteilung
		);
		replayAll();
		mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
			neueVeranlagungsMitteilung
		);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung2GSOK() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setSozialversicherungsNrPartner(
			versicherungsNummerPartner
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			Long.parseLong(versicherungsNummer1)
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setGeburtsdatumDossiertraeger(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);
		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		expectEverythingBisBearbeitung(gesuch);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller2());
		expect(
			finanzielleSituationService.saveFinanzielleSituation(
				anyObject(),
				anyObject()
			)
		).andReturn(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
		);
		expect(
			finanzielleSituationService.saveFinanzielleSituation(
				anyObject(),
				anyObject()
			)
		).andReturn(
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer()
		);
		expect(persistence.merge(neueVeranlagungsMitteilung)).andReturn(
			neueVeranlagungsMitteilung
		);
		replayAll();
		mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
			neueVeranlagungsMitteilung
		);
		verifyAll();
	}

	@Test
	public void neueVeranlaungsMitteilung_gemeinsam_GS1NotDossiertrager() {
		long ahvGS1Partner = Long.parseLong(versicherungsNummer1);
		BigDecimal nettoLohnGS1Partner = BigDecimal.valueOf(10000);
		long ahvGS2Dossiertraeger = versicherungsNummerPartner;
		BigDecimal nettoLohnGS2Dossiertraeger = BigDecimal.valueOf(20000);

		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			ahvGS2Dossiertraeger
		);
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitDossiertraeger(
			nettoLohnGS2Dossiertraeger
		);
		steuerdatenResponse.setGeburtsdatumDossiertraeger(
			LocalDate.of(1985, 03, 25)
		);
		steuerdatenResponse.setGeburtsdatumPartner(LocalDate.of(1980, 03, 25));
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setSozialversicherungsNrPartner(ahvGS1Partner);
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitPartner(
			nettoLohnGS1Partner
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			ahvGS1Partner
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);

		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);

		mockCalls(gesuch, true);

		Gesuch gesuchMitVerandlagungsmitteilung =
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);

		Assertions.assertEquals(
			nettoLohnGS1Partner,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getNettolohn()
		);
		Assertions.assertEquals(
			nettoLohnGS2Dossiertraeger,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getNettolohn()
		);
	}

	@Test
	public void neueVeranlaungsMitteilung_gemeinsam_GS1Dossiertrager() {
		long ahvGS1Dossiertraeger = Long.parseLong(versicherungsNummer1);
		BigDecimal nettoLohnGS1Dossiertraeger = BigDecimal.valueOf(10000);
		long ahvGS2Partner = versicherungsNummerPartner;
		BigDecimal nettoLohnGS2Partner = BigDecimal.valueOf(20000);

		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setGeburtsdatumDossiertraeger(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitDossiertraeger(
			nettoLohnGS1Dossiertraeger
		);
		steuerdatenResponse.setSozialversicherungsNrPartner(ahvGS2Partner);
		steuerdatenResponse.setErwerbseinkommenUnselbstaendigkeitPartner(
			nettoLohnGS2Partner
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);

		Gesuch gesuch = prepareGemeinsamFall(steuerdatenResponse);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(String.valueOf(ahvGS1Dossiertraeger));

		mockCalls(gesuch, true);

		Gesuch gesuchMitVerandlagungsmitteilung =
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);

		Assertions.assertEquals(
			nettoLohnGS1Dossiertraeger,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getNettolohn()
		);
		Assertions.assertEquals(
			nettoLohnGS2Partner,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getNettolohn()
		);
	}

	@Test
	public void neueVeranlaungsMitteilung_selbststaendigToSelbststaendig() {
		long ahvGS1Dossiertraeger = Long.parseLong(versicherungsNummer1);
		BigDecimal ausgewiesenerGeschaeftsertrag = BigDecimal.valueOf(10000);
		BigDecimal ausgewiesenerGeschaeftsertragVeranlagung = BigDecimal
			.valueOf(20000);

		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setGeburtsdatumDossiertraeger(LocalDate.now());
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragDossiertraeger(
			ausgewiesenerGeschaeftsertragVeranlagung
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);

		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setGeschaeftsgewinnBasisjahr(ausgewiesenerGeschaeftsertrag);

		mockCalls(gesuch, false);

		Gesuch gesuchMitVerandlagungsmitteilung =
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);

		Assertions.assertEquals(
			ausgewiesenerGeschaeftsertragVeranlagung,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getGeschaeftsgewinnBasisjahr()
		);
	}

	@Test
	public void neueVeranlaungsMitteilung_selbststaendigToNotSelbststaendig() {
		long ahvGS1Dossiertraeger = Long.parseLong(versicherungsNummer1);
		BigDecimal ausgewiesenerGeschaeftsertrag = BigDecimal.valueOf(10000);

		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragDossiertraeger(
			null
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);

		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setGeschaeftsgewinnBasisjahr(ausgewiesenerGeschaeftsertrag);

		mockCalls(gesuch, false);

		Gesuch gesuchMitVerandlagungsmitteilung =
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);

		Assertions.assertNull(
			gesuchMitVerandlagungsmitteilung.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getGeschaeftsgewinnBasisjahr()
		);
	}

	@Test
	public void neueVeranlaungsMitteilung_notSelbststaendigToSelbststaendig() {
		Long ahvGS1Dossiertraeger = Long.parseLong(versicherungsNummer1);
		BigDecimal ausgewiesenerGeschaeftsertrag = BigDecimal.valueOf(10000);

		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		steuerdatenResponse.setSozialversicherungsNrDossiertraeger(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setSozialversicherungsNrAntragsteller(
			ahvGS1Dossiertraeger
		);
		steuerdatenResponse.setAusgewiesenerGeschaeftsertragDossiertraeger(
			ausgewiesenerGeschaeftsertrag
		);
		steuerdatenResponse.setGeburtsdatumAntragsteller(
			LocalDate.of(1980, 03, 25)
		);
		steuerdatenResponse.setVeranlagungsstand(
			Veranlagungsstand.RECHTSKRAEFTIG
		);

		Gesuch gesuch = prepareGS1Fall(steuerdatenResponse);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setGeschaeftsgewinnBasisjahr(null);

		mockCalls(gesuch, false);

		Gesuch gesuchMitVerandlagungsmitteilung =
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);

		Assertions.assertEquals(
			ausgewiesenerGeschaeftsertrag,
			gesuchMitVerandlagungsmitteilung.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getGeschaeftsgewinnBasisjahr()
		);
	}

	private void mockCalls(Gesuch gesuch, boolean hasGS2) {
		expectEverythingBisBearbeitung(gesuch);

		expect(
			finanzielleSituationService.saveFinanzielleSituation(
				anyObject(),
				anyObject()
			)
		)
			.andReturn(
				gesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
			);

		if (hasGS2) {
			expect(
				finanzielleSituationService.saveFinanzielleSituation(
					anyObject(),
					anyObject()
				)
			)
				.andReturn(
					gesuch.getGesuchsteller2()
						.getFinanzielleSituationContainer()
				);
		}
		expect(persistence.merge(neueVeranlagungsMitteilung)).andReturn(
			neueVeranlagungsMitteilung
		);
		replayAll();
	}

	private void expectGesuchFound(Gesuch gesuch) {
		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(
			Optional.of(gesuch)
		);
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
	}

	private void expectEverythingBisBearbeitung(Gesuch gesuch) {
		expectGesuchFound(gesuch);
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(
			Optional.of(gesuch)
		);
		expect(gesuchService.createGesuch(anyObject())).andReturn(gesuch);
		authorizer.checkWriteAuthorization(anyObject(Gesuch.class));
		expectLastCall();
		authorizer.checkWriteAuthorization(gesuch);
		expectLastCall();
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
	}

	private void testExceptionWithErrorCode(ErrorCodeEnum errorCodeEnum) {
		try {
			mitteilungServiceBean.neueVeranlagungssmitteilungBearbeiten(
				neueVeranlagungsMitteilung
			);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(errorCodeEnum));
		}
	}

	private Gesuch prepareGS1Fall(SteuerdatenResponse steuerdatenResponse) {
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
		Gesuch gesuch = testfall_1GS.fillInGesuch();
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
		);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.getDossier().getFall().setBesitzer(new Benutzer());
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer());
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
		);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		return gesuch;
	}

	private Gesuch prepareGemeinsamFall(
		SteuerdatenResponse steuerdatenResponse
	) {
		Testfall04_WaltherLaura testfall_2GS =
			new Testfall04_WaltherLaura(
				gesuchsperiode,
				false,
				gemeinde,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode)
			);
		testfall_2GS.createFall();
		testfall_2GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));

		Gesuch gesuch = testfall_2GS.fillInGesuch();
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.getDossier().getFall().setBesitzer(new Benutzer());
		gesuch.setStatus(AntragStatus.VERFUEGT);
		gesuch.getDossier()
			.getFall()
			.getBesitzer()
			.setAhvNummer(versicherungsNummer1);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		gesuch.getGesuchsteller2()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenResponse(steuerdatenResponse);
		gesuch.getGesuchsteller2()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenZugriff(true);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setGemeinsameSteuererklaerung(true);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);
		return gesuch;
	}

}
