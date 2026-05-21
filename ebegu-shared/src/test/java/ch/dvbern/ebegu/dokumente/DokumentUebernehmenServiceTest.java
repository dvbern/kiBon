package ch.dvbern.ebegu.dokumente;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EasyMockExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DokumentUebernehmenServiceTest extends EasyMockSupport {

	private static final Gesuchsperiode GP_2324 = createGesuchsperiodeStatic(
		LocalDate.of(2023, 8, 1),
		LocalDate.of(2024, 7, 31)
	);
	private static final Gesuchsperiode GP_2425 = createGesuchsperiodeStatic(
		LocalDate.of(2024, 8, 1),
		LocalDate.of(2025, 7, 31)
	);
	private static final Gesuchsperiode GP_2526 = createGesuchsperiodeStatic(
		LocalDate.of(2025, 8, 1),
		LocalDate.of(2026, 7, 31)
	);

	@Mock
	private GesuchService gesuchService;

	@Mock
	private GesuchsperiodeService gesuchsperiodeService;

	@TestSubject
	private DokumentUebernehmenService service =
		new DokumentUebernehmenService();

	@Nested
	class findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode {

		@Test
		void shouldFindMutationWithHighestLaufnummer_whenVorgaengerExistsInVorperiode() {
			// Arrange
			Fall fall = createFall();
			Dossier dossierParis = createDossier(fall);

			Gesuch currentGesuch = createGesuch(
				dossierParis,
				GP_2526,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2526.getGueltigkeit().getGueltigAb()
			);

			Gesuch vorperiodeFirstLondon = createGesuch(
				dossierParis,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb()
			);
			Gesuch vorperiodeMutationLondon = createGesuch(
				dossierParis,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.MUTATION,
				2,
				GP_2425.getGueltigkeit().getGueltigAb().plusMonths(1)
			);
			Gesuch vorperiodeFirstParis = createGesuch(
				dossierParis,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb().plusMonths(2)
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2526)
			)
				.andReturn(Optional.of(GP_2425));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(
					List.of(
						vorperiodeFirstLondon,
						vorperiodeMutationLondon,
						vorperiodeFirstParis
					)
				);

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isPresent());
			assertEquals(
				vorperiodeMutationLondon.getId(),
				result.get().getId()
			);
		}

		@Test
		void shouldReturnLatestEingereichtGesuchInVorperiode_whenOtherDossierHasLaterVerfuegtMutationButEarlierEingereichtErstgesuch() {
			// Arrange
			Fall fall = createFall();
			Dossier dossierParis = createDossier(fall);
			Dossier dossierLondon = createDossier(fall);

			Gesuch currentGesuch = createGesuch(
				dossierParis,
				GP_2526,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2526.getGueltigkeit().getGueltigAb()
			);

			Gesuch vorperiodeFirstParis = createGesuch(
				dossierParis,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb().plusMonths(2)
			);
			Gesuch vorperiodeFirstLondon = createGesuch(
				dossierLondon,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb()
			);
			Gesuch vorperiodeMutationLondon = createGesuch(
				dossierLondon,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.MUTATION,
				2,
				GP_2425.getGueltigkeit().getGueltigAb().plusMonths(1)
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2526)
			)
				.andReturn(Optional.of(GP_2425));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(
					List.of(
						vorperiodeFirstParis,
						vorperiodeFirstLondon,
						vorperiodeMutationLondon
					)
				);

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isPresent());
			// Dossier Paris has the latest first application (plusMonths(2)).
			// The logic picks the latest decreed application (max entry date) of the previous period regardless of dossier/type.
			assertEquals(vorperiodeFirstParis.getId(), result.get().getId());
		}

		@Test
		void shouldFindVerfuegtGesuch_whenOtherDossierHasEarlierErstgesuchButNotInVerfuegtStatus() {
			// Arrange
			Fall fall = createFall();
			Dossier dossierParis = createDossier(fall);
			Dossier dossierLondon = createDossier(fall);

			Gesuch currentGesuch = createGesuch(
				dossierParis,
				GP_2526,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2526.getGueltigkeit().getGueltigAb()
			);

			// Dossier London has earlier first application, but not decreed
			Gesuch vorperiodeFirstLondon = createGesuch(
				dossierLondon,
				GP_2425,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb().minusMonths(1)
			);

			// Dossier Paris has later first application, and decreed
			Gesuch vorperiodeFirstParis = createGesuch(
				dossierParis,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb()
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2526)
			)
				.andReturn(Optional.of(GP_2425));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(
					List.of(vorperiodeFirstLondon, vorperiodeFirstParis)
				);

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isPresent());
			assertEquals(vorperiodeFirstParis.getId(), result.get().getId());
		}

		@Test
		void shouldFindHighestVerfuegtLaufnummer_whenMutationExists() {
			// Arrange
			Fall fall = createFall();
			Dossier dossier = createDossier(fall);

			Gesuch currentGesuch = createGesuch(
				dossier,
				GP_2425,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb()
			);

			Gesuch first = createGesuch(
				dossier,
				GP_2324,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2324.getGueltigkeit().getGueltigAb()
			);
			Gesuch mutationVerfuegt = createGesuch(
				dossier,
				GP_2324,
				AntragStatus.VERFUEGT,
				AntragTyp.MUTATION,
				2,
				GP_2324.getGueltigkeit().getGueltigAb().plusMonths(1)
			);
			Gesuch mutationPending = createGesuch(
				dossier,
				GP_2324,
				AntragStatus.IN_BEARBEITUNG_JA,
				AntragTyp.MUTATION,
				3,
				GP_2324.getGueltigkeit().getGueltigAb().plusMonths(2)
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2425)
			)
				.andReturn(Optional.of(GP_2324));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(List.of(first, mutationVerfuegt, mutationPending));

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isPresent());
			assertEquals(mutationVerfuegt.getId(), result.get().getId());
		}

		@Test
		void shouldReturnEmpty_whenVerfuegtGesuchExistsOnlyInEarlierPeriod() {
			// Arrange
			Fall fall = createFall();
			Dossier dossier = createDossier(fall);

			Gesuch currentGesuch = createGesuch(
				dossier,
				GP_2526,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2526.getGueltigkeit().getGueltigAb()
			);

			Gesuch decreedInFarPast = createGesuch(
				dossier,
				GP_2324,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2324.getGueltigkeit().getGueltigAb()
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2526)
			)
				.andReturn(Optional.of(GP_2425));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(List.of(decreedInFarPast));

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isEmpty());
		}

		@Test
		void shouldReturnOtherGemeinde_whenVerfuegtGesuchExistsOnlyInEarlierPeriodFromOtherGemeinde() {
			// Arrange
			Fall fall = createFall();
			Dossier dossier = createDossier(fall);
			Dossier dossierLondon = createDossier(fall);
			Gesuch currentGesuch = createGesuch(
				dossier,
				GP_2526,
				AntragStatus.IN_BEARBEITUNG_GS,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2526.getGueltigkeit().getGueltigAb()
			);

			Gesuch gesuchOtherDossierVorPeriode = createGesuch(
				dossierLondon,
				GP_2425,
				AntragStatus.VERFUEGT,
				AntragTyp.ERSTGESUCH,
				1,
				GP_2425.getGueltigkeit().getGueltigAb()
			);

			EasyMock.expect(
				gesuchsperiodeService.getVorjahrGesuchsperiode(GP_2526)
			)
				.andReturn(Optional.of(GP_2425));

			EasyMock.expect(gesuchService.getAllGesuchsForFall(anyObject()))
				.andReturn(List.of(gesuchOtherDossierVorPeriode));

			replayAll();

			// Act
			Optional<Gesuch> result = service
				.findLatestVerfuegtesGesuchOfLatestEingereichtErstgesuchOfVorperiode(
					currentGesuch
				);

			// Assert
			verifyAll();
			assertTrue(result.isPresent());
			assertEquals(
				gesuchOtherDossierVorPeriode.getId(),
				result.get().getId()
			);
		}
	}

	private static Gesuchsperiode createGesuchsperiodeStatic(
		LocalDate von,
		LocalDate bis
	) {
		Gesuchsperiode gp = new Gesuchsperiode();
		gp.getGueltigkeit().setGueltigAb(von);
		gp.getGueltigkeit().setGueltigBis(bis);
		gp.setStatus(GesuchsperiodeStatus.AKTIV);
		return gp;
	}

	private Fall createFall() {
		return new Fall();
	}

	private Dossier createDossier(Fall fall) {
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		return dossier;
	}

	private Gesuch createGesuch(
		Dossier dossier,
		Gesuchsperiode gp,
		AntragStatus status,
		AntragTyp typ,
		int laufnummer,
		LocalDate eingangsdatum
	) {
		Gesuch g = new Gesuch();
		g.setDossier(dossier);
		g.setGesuchsperiode(gp);
		g.setStatus(status);
		g.setTyp(typ);
		g.setLaufnummer(laufnummer);
		g.setEingangsdatum(eingangsdatum);
		return g;
	}
}
