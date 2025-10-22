package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BGCalculationResultTest {

	@Test
	void massgebendesEinkommen_whenAbzugFamGroessLargerThanMassgebendesEinkomenBeforeabzug_shouldBeZero() {
		BGCalculationResult result = new BGCalculationResult();
		result.setAbzugFamGroesse(BigDecimal.TEN);
		result.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ONE);

		assertThat(result.getMassgebendesEinkommen(), is(BigDecimal.ZERO));
	}

	@Test
	void massgebendesEinkommen_whenAbzugFamGroessSmallerThanMassgebendesEinkomenBeforeabzug_shouldBeDifference() {
		BGCalculationResult result = new BGCalculationResult();
		result.setAbzugFamGroesse(BigDecimal.ONE);
		result.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.TEN);

		assertThat(
			result.getMassgebendesEinkommen(),
			is(new BigDecimal("9.00"))
		);
	}

}
