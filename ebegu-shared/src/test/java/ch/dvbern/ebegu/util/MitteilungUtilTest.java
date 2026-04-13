package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import org.junit.jupiter.api.Test;

import static shadow.org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MitteilungUtilTest {

	@Test
	void shouldReturnFalse_whenPlainMitteilung() {
		Mitteilung mitteilung = new Mitteilung();

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}

	@Test
	void shouldReturnFalse_whenBetreuungsmitteilungAndFlagNotSet() {
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		mitteilung.setSchliessungMitteilung(false);

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}

	@Test
	void shouldReturnTrue_whenBetreuungsmitteilungAndFlagSet() {
		Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		mitteilung.setSchliessungMitteilung(true);

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isTrue();
	}

	@Test
	void shouldReturnFalse_whenSubclassOfMitteilungButNotBetreuungsmitteilung() {
		// e.g. NeueVeranlagungsMitteilung or any other Mitteilung subtype
		Mitteilung mitteilung = new NeueVeranlagungsMitteilung();

		assertThat(MitteilungUtil.isSchliessungsmitteilung(mitteilung))
			.isFalse();
	}
}
