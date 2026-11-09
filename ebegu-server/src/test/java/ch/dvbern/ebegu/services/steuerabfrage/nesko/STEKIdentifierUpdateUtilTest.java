package ch.dvbern.ebegu.services.steuerabfrage.nesko;

import java.util.Objects;
import java.util.UUID;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class STEKIdentifierUpdateUtilTest {
	private static final String GS1_ID = UUID.randomUUID().toString();
	private static final String GS2_ID = UUID.randomUUID().toString();

	@Nested
	class IsZPVAlreadyUsedInGesuch {

		private static final String ZPV_BESITZER = "1111111";
		private static final String ZPV_GS2 = "2222222";
		private static final String ZPV_NEW = "9999999";

		@Test
		void isZPVAlreadyUsedInGesuch_shouldThrowIllegalStateException_whenBesitzerOfFallIsNull() {
			Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
			gesuch.getDossier().getFall().setBesitzer(null);

			IllegalStateException thrown = assertThrows(
				IllegalStateException.class,
				() -> STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
					ZPV_NEW,
					gesuch,
					GS1_ID
				)
			);

			assertThat(
				thrown.getMessage(),
				is("Besitzer of fall must not be null in stek linking process")
			);
		}

		@Nested
		class OneGesuchstellende {
			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnTrue_whenZpvMatchesBesitzer() {
				Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));

				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_BESITZER,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnFalse_whenZpvDoesNotMatchBesitzerButMatchesGS1ZPV() {
				var gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(
					createGesuchstellerContainerWithZPV(GS1_ID, ZPV_NEW)
				);
				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}

			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnFalse_whenZpvDoesNotMatchBesitzerAndHasNoGS1ZPV() {
				var gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}
		}

		@Nested
		class TwoGesuchstellende {
			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnTrue_whenZpvMatchesBesitzerAndSecondGsIsPresent() {
				var gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithZPV(GS2_ID, ZPV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_BESITZER,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnTrue_whenZpvMatchesOtherGesuchsteller() {
				Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithZPV(GS2_ID, ZPV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_GS2,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnFalse_whenZpvMatchesNeitherBesitzerNorOtherGs() {
				Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithZPV(GS2_ID, ZPV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}

			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnFalse_whenZpvMatchesOnlyTheEditedGsItself() {
				Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithZPV(GS2_ID, ZPV_NEW)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_NEW,
						gesuch,
						GS2_ID
					),
					is(false)
				);
			}

		}

		private Gesuch createGesuchWithZPVNummer(String zpvBesitzer) {
			Gesuch gesuch = createGesuch();
			Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer())
				.setZpvNummer(zpvBesitzer);
			return gesuch;
		}

		private GesuchstellerContainer createGesuchstellerContainerWithZPV(
			String uuid,
			String zpvNummer
		) {
			var container = createGesuchstellerContainer(uuid);
			container.getGesuchstellerJA().setZpvNummer(zpvNummer);
			return container;
		}
	}

	@Nested
	class IsAHVAlreadyUsedInGesuch {

		private static final String AHV_BESITZER = "1111111";
		private static final String AHV_GS2 = "2222222";
		private static final String AHV_NEW = "9999999";

		@Test
		void isAHVAlreadyUsedInGesuch_shouldThrowIllegalStateException_whenBesitzerOfFallIsNull() {
			Gesuch gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
			gesuch.getDossier().getFall().setBesitzer(null);

			IllegalStateException thrown = assertThrows(
				IllegalStateException.class,
				() -> STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
					AHV_NEW,
					gesuch,
					GS1_ID
				)
			);

			assertThat(
				thrown.getMessage(),
				is("Besitzer of fall must not be null in stek linking process")
			);
		}

		@Nested
		class OneGesuchstellende {
			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnTrue_whenAhvMatchesBesitzer() {
				Gesuch gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));

				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_BESITZER,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnFalse_whenAhvDoesNotMatchBesitzerButMatchesGS1Ahv() {
				var gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(
					createGesuchstellerContainerWithAHV(GS1_ID, AHV_NEW)
				);
				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}

			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnFalse_whenAhvDoesNotMatchBesitzerAndHasNoGS1Ahv() {
				var gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}
		}

		@Nested
		class TwoGesuchstellende {
			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnTrue_whenAhvMatchesBesitzerAndSecondGsIsPresent() {
				var gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithAHV(GS2_ID, AHV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_BESITZER,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnTrue_whenAhvMatchesOtherGesuchsteller() {
				Gesuch gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithAHV(GS2_ID, AHV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_GS2,
						gesuch,
						GS1_ID
					),
					is(true)
				);
			}

			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnFalse_whenAhvMatchesNeitherBesitzerNorOtherGs() {
				Gesuch gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithAHV(GS2_ID, AHV_GS2)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_NEW,
						gesuch,
						GS1_ID
					),
					is(false)
				);
			}

			@Test
			void isAHVAlreadyUsedInGesuch_shouldReturnFalse_whenAhvMatchesOnlyTheEditedGsItself() {
				Gesuch gesuch = createGesuchWithAHVNummer(AHV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));
				gesuch.setGesuchsteller2(
					createGesuchstellerContainerWithAHV(GS2_ID, AHV_NEW)
				);

				assertThat(
					STEKIdentifierUpdateUtil.isAHVAlreadyUsedInGesuch(
						AHV_NEW,
						gesuch,
						GS2_ID
					),
					is(false)
				);
			}

		}

		private Gesuch createGesuchWithAHVNummer(String ahvNummer) {
			Gesuch gesuch = createGesuch();
			Objects.requireNonNull(gesuch.getDossier().getFall().getBesitzer())
				.setAhvNummer(ahvNummer);
			return gesuch;
		}

		private GesuchstellerContainer createGesuchstellerContainerWithAHV(
			String uuid,
			String ahvNummer
		) {
			var container = createGesuchstellerContainer(uuid);
			container.getGesuchstellerJA().setAhvNummer(ahvNummer);
			return container;
		}
	}

	private static GesuchstellerContainer createGesuchstellerContainer(
		String uuid
	) {
		Gesuchsteller gs = new Gesuchsteller();
		GesuchstellerContainer container = new GesuchstellerContainer();
		container.setId(uuid);
		container.setGesuchstellerJA(gs);
		return container;
	}

	private static @NonNull Gesuch createGesuch() {
		Benutzer besitzer = new Benutzer();

		Fall fall = new Fall();
		fall.setBesitzer(besitzer);

		Dossier dossier = new Dossier();
		dossier.setFall(fall);

		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		return gesuch;
	}
}
