package ch.dvbern.ebegu.gesuchsteller;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GesuchstellerUtilTest {

	@Test
	void isSecondGSPresent_shouldBeFalse_WhenGesuchsteller2IsNull() {
		var gesuch = new Gesuch();
		gesuch.setGesuchsteller2(null);

		assertThat(GesuchstellerUtil.isSecondGSPresent(gesuch), is(false));
	}

	@Test
	void isSecondGSPresent_shouldBeTrue_WhenGesuchsteller2IsNotNull() {
		var gesuch = new Gesuch();
		gesuch.setGesuchsteller2(new GesuchstellerContainer());

		assertThat(GesuchstellerUtil.isSecondGSPresent(gesuch), is(true));
	}
}
