package ch.dvbern.ebegu.services.steuerabfrage.nesko;

import java.util.UUID;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class ZPVUpdateUtilTest {
	private static final String ZPV_BESITZER = "1111111";
	private static final String ZPV_GS2 = "2222222";
	private static final String ZPV_NEW = "9999999";
	private static final String GS1_ID = UUID.randomUUID().toString();
	private static final String GS2_ID = UUID.randomUUID().toString();

	@Nested
	class IsZPVAlreadyUsedInGesuch {

		@Test
		void isZPVAlreadyUsedInGesuch_shouldThrowIllegalStateException_whenBesitzerOfFallIsNull() {
			Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
			gesuch.getDossier().getFall().setBesitzer(null);

			IllegalStateException thrown = assertThrows(
				IllegalStateException.class,
				() -> ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
					ZPV_NEW,
					gesuch,
					GS1_ID
				)
			);

			assertThat(
				thrown.getMessage(),
				is("Besitzer of fall must not be null in zpv linking process")
			);
		}

		@Nested
		class OneGesuchstellende {
			@Test
			void isZPVAlreadyUsedInGesuch_shouldReturnTrue_whenZpvMatchesBesitzer() {
				Gesuch gesuch = createGesuchWithZPVNummer(ZPV_BESITZER);
				gesuch.setGesuchsteller1(createGesuchstellerContainer(GS1_ID));

				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					createGesuchstellerContainer(GS1_ID, ZPV_NEW)
				);
				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					createGesuchstellerContainer(GS2_ID, ZPV_GS2)
				);

				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					createGesuchstellerContainer(GS2_ID, ZPV_GS2)
				);

				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					createGesuchstellerContainer(GS2_ID, ZPV_GS2)
				);

				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
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
					createGesuchstellerContainer(GS2_ID, ZPV_NEW)
				);

				assertThat(
					ZPVUpdateUtil.isZPVAlreadyUsedInGesuch(
						ZPV_NEW,
						gesuch,
						GS2_ID
					),
					is(false)
				);
			}

		}

		private Gesuch createGesuchWithZPVNummer(String zpvBesitzer) {
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

		private GesuchstellerContainer createGesuchstellerContainer(
			String uuid,
			String zpvNummer
		) {
			Gesuchsteller gs = new Gesuchsteller();
			gs.setZpvNummer(zpvNummer);

			GesuchstellerContainer container = new GesuchstellerContainer();
			container.setId(uuid);
			container.setGesuchstellerJA(gs);
			return container;
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
	}
}
