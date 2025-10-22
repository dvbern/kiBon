package ch.dvbern.ebegu.einstellung;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import ch.dvbern.ebegu.einstellung.validation.ValidApplicationPropertyValueTypeConstraintValidator;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(EasyMockRunner.class)
class ValidApplicationPropertyValueTypeConstraintValidatorTest extends
	EasyMockSupport {

	private ConstraintValidatorContext constraintValidatorContext;
	private ConstraintViolationBuilder constraintViolationBuilder;

	@BeforeEach
	public void setUp() {
		constraintValidatorContext = createMock(
			ConstraintValidatorContext.class
		);
		constraintViolationBuilder = createMock(
			ConstraintViolationBuilder.class
		);
		assertNotNull(constraintValidatorContext);
		assertNotNull(constraintViolationBuilder);
	}

	ValidApplicationPropertyValueTypeConstraintValidator validator =
		new ValidApplicationPropertyValueTypeConstraintValidator();

	@Nested
	class BooleanApplicationProperyTest {

		@ParameterizedTest
		@ValueSource(strings = { "true", "TRUE", "tRuE", "false", "FALSE",
			"fAlSe" })
		void booleanStringShouldBeValidBooleanApplicationProperyValue(
			String booleanString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
					booleanString
				);
			assertTrue(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "nope", "nei" })
		void nonBooleanStringShouldNotBeValidBooleanApplicationProperyValue(
			String nonBooleanString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
					nonBooleanString
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@Test
		void emptyStringShouldNotBeValidBooleanApplicationProperyValue() {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
					""
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}
	}

	private void mockContextMessageBuilding() {
		constraintValidatorContext.disableDefaultConstraintViolation();
		expect(
			constraintValidatorContext.buildConstraintViolationWithTemplate(
				anyString()
			)
		).andReturn(constraintViolationBuilder);
		expect(constraintViolationBuilder.addConstraintViolation()).andReturn(
			constraintValidatorContext
		);
		replayAll();
	}

	@Nested
	class NumberApplicationProperyTest {

		@ParameterizedTest
		@ValueSource(strings = { "1", "0", "-1", "1.5", "1.5", "0.00000000" })
		void numberStringShouldBeValidNumberApplicationProperyValue(
			String numberString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE,
					numberString
				);
			assertTrue(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "1a", "a1", "0,00", "1/5" })
		void nonNumberStringShouldNotBeValidNumberApplicationProperyValue(
			String nonNumberString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE,
					nonNumberString
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@Test
		void emptyStringShouldNotBeValidNumberApplicationProperyValue() {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE,
					""
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}
	}

	@Nested
	class DateApplicationProperyTest {

		@ParameterizedTest
		@ValueSource(strings = "2020-01-01")
		void dateStringShouldBeValidDateApplicationProperyValue(
			String dateString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM,
					dateString
				);
			mockContextMessageBuilding();
			assertTrue(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "1a", "a1", "0,00", "1/5" })
		void nonDateStringShouldNotBeValidDateApplicationProperyValue(
			String nonDateString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM,
					nonDateString
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "60.02.2020", "01.13.2020", "2020-30-01",
			"01-12-2020", "2020.12.01", "01/01/2020", "1.01.2020", "1.1.2020",
			"01.1.2020" })
		void wrongFormatDateStringShouldNotBeValidDateApplicationProperyValue(
			String dateString
		) {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM,
					dateString
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}

		@Test
		void emptyStringShouldNotBeValidNumberApplicationProperyValue() {
			ApplicationProperty applicationPropery =
				new ApplicationProperty(
					ApplicationPropertyKey.STADT_BERN_ASIV_START_DATUM,
					""
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(
					applicationPropery,
					constraintValidatorContext
				)
			);
		}
	}

}
