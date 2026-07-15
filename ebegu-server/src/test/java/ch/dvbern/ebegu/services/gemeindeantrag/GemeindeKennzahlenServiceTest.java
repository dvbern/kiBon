package ch.dvbern.ebegu.services.gemeindeantrag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GemeindeKennzahlenServiceTest {

	@Mock
	GesuchsperiodeService gesuchsperiodeService;

	@Mock
	GemeindeService gemeindeService;

	@Spy
	@InjectMocks
	GemeindeKennzahlenService gemeindeKennzahlenService;

	@Nested
	class createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden {

		@Test
		void createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden_shouldReturnEmpty_whenActiveGemeindeHasNoBGAngebot() {
			Mandant mandant = new Mandant();
			Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
			Gemeinde gemeindeWithoutBG = new Gemeinde();
			gemeindeWithoutBG.setAngebotBG(false);

			when(
				gesuchsperiodeService.getGesuchsperiodeAm(
					any(LocalDate.class),
					eq(mandant)
				)
			)
				.thenReturn(Optional.of(gesuchsperiode));
			when(
				gemeindeService.getAktiveGemeindenGueltigAm(
					any(LocalDate.class),
					eq(mandant)
				)
			)
				.thenReturn(List.of(gemeindeWithoutBG));

			var result = gemeindeKennzahlenService
				.createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden(
					mandant
				);

			assertThat(result.isEmpty(), is(true));
		}

		@Test
		void createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden_shouldCreateForGemeinde_whenActiveGemeindeHasBGAngebot() {
			Mandant mandant = new Mandant();
			Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
			Gemeinde gemeindeWithBG = new Gemeinde();
			gemeindeWithBG.setAngebotBG(true);

			when(
				gesuchsperiodeService.getGesuchsperiodeAm(
					any(LocalDate.class),
					eq(mandant)
				)
			)
				.thenReturn(Optional.of(gesuchsperiode));
			when(
				gemeindeService.getAktiveGemeindenGueltigAm(
					any(LocalDate.class),
					eq(mandant)
				)
			)
				.thenReturn(List.of(gemeindeWithBG));

			doReturn(null).when(gemeindeKennzahlenService)
				.createGemeindeKennzahlen(
					gesuchsperiode,
					List.of(gemeindeWithBG)
				);

			gemeindeKennzahlenService
				.createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden(
					mandant
				);

			verify(gemeindeKennzahlenService).createGemeindeKennzahlen(
				gesuchsperiode,
				List.of(gemeindeWithBG)
			);

		}
	}
}
