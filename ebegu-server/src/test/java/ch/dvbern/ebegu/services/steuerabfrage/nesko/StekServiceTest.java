package ch.dvbern.ebegu.services.steuerabfrage.nesko;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.services.GesuchstellerService;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class StekServiceTest {

	private static final String ZPV_NEW = "9999999";
	private static final String ZPV_BESITZER = "1111111";
	private static final String AHV_NEW = "7561234567897";
	private static final String AHV_NEW_DOTTED = "756.1234.5678.97";
	private static final String AHV_BESITZER = "7569999999994";

	@TestSubject
	StekService stekService = new StekService();

	@Mock
	GesuchstellerService gesuchstellerService;

	@Test
	void checkAvailable_shouldReturnErrorBadGesuchsteller_whenContainerIdIsNull() {
		replay(gesuchstellerService);

		assertThat(
			stekService.checkZpvNummer(null, ZPV_NEW),
			is(STEKUpdateResult.ERROR_BAD_GESUCHSTELLER)
		);
		assertThat(
			stekService.checkAhvNummer(null, AHV_NEW),
			is(STEKUpdateResult.ERROR_BAD_GESUCHSTELLER)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorBadGesuchsteller_whenContainerIdIsNull() {
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			null,
			ZPV_NEW,
			null
		);

		assertThat(result, is(STEKUpdateResult.ERROR_BAD_GESUCHSTELLER));
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorZpvAlreadyInGesuch_whenZpvMatchesBesitzer() {
		String containerId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_NEW);
		gesuch.setGesuchsteller1(
			createGesuchstellerContainer(containerId, null)
		);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.checkZpvNummer(
			containerId,
			ZPV_NEW
		);

		assertThat(
			result,
			is(STEKUpdateResult.ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorZpvAlreadyInGesuch_whenZpvMatchesOtherGesuchsteller() {
		String editedId = UUID.randomUUID().toString();
		String otherId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(createGesuchstellerContainer(editedId, null));
		gesuch.setGesuchsteller2(
			createGesuchstellerContainer(otherId, ZPV_NEW)
		);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(List.of(editedId))
		)
			.andReturn(List.of(gesuch));
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.checkZpvNummer(editedId, ZPV_NEW);

		assertThat(
			result,
			is(STEKUpdateResult.ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH)
		);
		verify(gesuchstellerService);
	}

	@Test
	void checkZpvNummerAvailable_shouldReturnSuccess_whenZpvIsNotUsed() {
		String containerId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(
			createGesuchstellerContainer(containerId, null)
		);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		replay(gesuchstellerService);

		assertThat(
			stekService.checkZpvNummer(containerId, ZPV_NEW),
			is(STEKUpdateResult.SUCCESS)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorBadGesuchsteller_whenContainerCannotBeFound() {
		String containerId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(
			createGesuchstellerContainer(containerId, null)
		);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId)).andReturn(
			Optional.empty()
		);
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			containerId,
			ZPV_NEW,
			null
		);

		assertThat(result, is(STEKUpdateResult.ERROR_BAD_GESUCHSTELLER));
		verify(gesuchstellerService);
	}

	@ParameterizedTest
	@EnumSource(value = AntragStatus.class,
		mode = Mode.EXCLUDE,
		names = { "IN_BEARBEITUNG_GS" })
	void updateGesuchstellerZPVNr_shouldReturnGesuchNotInStateForZpvUpdate_whenStatusIsNotInBearbeitungGs() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(
				containerId,
				null,
				SteuerdatenAnfrageStatus.FAILED
			);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.VERFUEGT);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId)).andReturn(
			Optional.of(container)
		);
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			containerId,
			ZPV_NEW,
			null
		);

		assertThat(
			result,
			is(STEKUpdateResult.GESUCH_NOT_IN_STATE_FOR_STEK_UPDATE)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnGesuchNotInStateForZpvUpdate_whenSteuerdatenAbfrageStatusIsNull() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(containerId, null, null);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId)).andReturn(
			Optional.of(container)
		);
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			containerId,
			ZPV_NEW,
			null
		);

		assertThat(
			result,
			is(STEKUpdateResult.GESUCH_NOT_IN_STATE_FOR_STEK_UPDATE)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnGesuchNotInStateForZpvUpdate_whenSteuerdatenAbfrageAlreadySuccessful() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(
				containerId,
				null,
				SteuerdatenAnfrageStatus.PROVISORISCH
			);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId)).andReturn(
			Optional.of(container)
		);
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			containerId,
			ZPV_NEW,
			null
		);

		assertThat(
			result,
			is(STEKUpdateResult.GESUCH_NOT_IN_STATE_FOR_STEK_UPDATE)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnSuccess_whenStatusIsInBearbeitungGsAndSteuerabfrageFailed() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(
				containerId,
				null,
				SteuerdatenAnfrageStatus.FAILED
			);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId)).andReturn(
			Optional.of(container)
		);
		expect(gesuchstellerService.updateGesuchsteller(container)).andReturn(
			container
		);
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.updateGesuchstellerStekIdentifier(
			containerId,
			ZPV_NEW,
			null
		);

		assertThat(result, is(STEKUpdateResult.SUCCESS));
		assertThat(container.getGesuchstellerJA().getZpvNummer(), is(ZPV_NEW));
		assertThat(
			Objects.requireNonNull(container.getFinanzielleSituationContainer())
				.getFinanzielleSituationJA()
				.getSteuerdatenAbfrageStatus(),
			is(SteuerdatenAnfrageStatus.RETRY)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorStekIdentifierAlreadyInGesuch_whenAhvMatchesBesitzer() {
		String containerId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer())
			.setAhvNummer(AHV_NEW_DOTTED);
		gesuch.setGesuchsteller1(
			createGesuchstellerContainer(containerId, null)
		);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.checkAhvNummer(
			containerId,
			AHV_NEW
		);

		assertThat(
			result,
			is(STEKUpdateResult.ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldReturnErrorStekIdentifierAlreadyInGesuch_whenAhvMatchesOtherGesuchsteller() {
		String editedId = UUID.randomUUID().toString();
		String otherId = UUID.randomUUID().toString();
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(createGesuchstellerContainer(editedId, null));
		GesuchstellerContainer other = createGesuchstellerContainer(
			otherId,
			null
		);
		other.getGesuchstellerJA().setAhvNummer(AHV_NEW);
		gesuch.setGesuchsteller2(other);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(List.of(editedId))
		)
			.andReturn(List.of(gesuch));
		replay(gesuchstellerService);

		STEKUpdateResult result = stekService.checkAhvNummer(
			editedId,
			AHV_NEW_DOTTED
		);

		assertThat(
			result,
			is(STEKUpdateResult.ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldSetOnlyAhvAndKeepZpv_whenOnlyAhvIsGiven() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(
				containerId,
				ZPV_BESITZER,
				SteuerdatenAnfrageStatus.FAILED
			);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.getDossier().getFall().getBesitzer().setAhvNummer(AHV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId))
			.andReturn(Optional.of(container));
		expect(gesuchstellerService.updateGesuchsteller(container)).andReturn(
			container
		);
		replay(gesuchstellerService);

		STEKUpdateResult result =
			stekService.updateGesuchstellerStekIdentifier(
				containerId,
				null,
				AHV_NEW
			);

		assertThat(result, is(STEKUpdateResult.SUCCESS));
		assertThat(container.getGesuchstellerJA().getAhvNummer(), is(AHV_NEW));
		assertThat(
			container.getGesuchstellerJA().getZpvNummer(),
			is(ZPV_BESITZER)
		);
		verify(gesuchstellerService);
	}

	@Test
	void updateGesuchstellerZPVNr_shouldSetBothNumbers_whenBothAreGiven() {
		String containerId = UUID.randomUUID().toString();
		GesuchstellerContainer container =
			createGesuchstellerContainerWithFinSit(
				containerId,
				null,
				SteuerdatenAnfrageStatus.FAILED
			);
		Gesuch gesuch = createGesuchWithBesitzerZpv(ZPV_BESITZER);
		gesuch.setGesuchsteller1(container);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);

		expect(
			gesuchstellerService.findGesuchOfGesuchstellende(
				List.of(containerId)
			)
		)
			.andReturn(List.of(gesuch));
		expect(gesuchstellerService.findGesuchsteller(containerId))
			.andReturn(Optional.of(container));
		expect(gesuchstellerService.updateGesuchsteller(container)).andReturn(
			container
		);
		replay(gesuchstellerService);

		STEKUpdateResult result =
			stekService.updateGesuchstellerStekIdentifier(
				containerId,
				ZPV_NEW,
				AHV_NEW
			);

		assertThat(result, is(STEKUpdateResult.SUCCESS));
		assertThat(container.getGesuchstellerJA().getZpvNummer(), is(ZPV_NEW));
		assertThat(container.getGesuchstellerJA().getAhvNummer(), is(AHV_NEW));
		verify(gesuchstellerService);
	}

	private static Gesuch createGesuchWithBesitzerZpv(
		@Nonnull String zpvBesitzer
	) {
		Benutzer besitzer = new Benutzer();
		besitzer.setZpvNummer(zpvBesitzer);

		Fall fall = new Fall();
		fall.setBesitzer(besitzer);

		Dossier dossier = new Dossier();
		dossier.setFall(fall);

		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		return gesuch;
	}

	private static GesuchstellerContainer createGesuchstellerContainer(
		@Nonnull String containerId,
		String zpvNummer
	) {
		Gesuchsteller gs = new Gesuchsteller();
		gs.setZpvNummer(zpvNummer);

		GesuchstellerContainer container = new GesuchstellerContainer();
		container.setId(containerId);
		container.setGesuchstellerJA(gs);
		return container;
	}

	private static GesuchstellerContainer createGesuchstellerContainerWithFinSit(
		@Nonnull String containerId,
		String zpvNummer,
		SteuerdatenAnfrageStatus abfrageStatus
	) {
		GesuchstellerContainer container = createGesuchstellerContainer(
			containerId,
			zpvNummer
		);
		container.setFinanzielleSituationContainer(
			createFinSitContainer(abfrageStatus)
		);
		return container;
	}

	private static FinanzielleSituationContainer createFinSitContainer(
		SteuerdatenAnfrageStatus abfrageStatus
	) {
		FinanzielleSituation finSit = new FinanzielleSituation();
		finSit.setSteuerdatenAbfrageStatus(abfrageStatus);

		FinanzielleSituationContainer finSitContainer =
			new FinanzielleSituationContainer();
		finSitContainer.setFinanzielleSituationJA(finSit);
		return finSitContainer;
	}
}
