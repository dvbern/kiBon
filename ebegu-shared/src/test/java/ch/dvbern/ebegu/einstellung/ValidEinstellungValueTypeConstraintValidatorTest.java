package ch.dvbern.ebegu.einstellung;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import ch.dvbern.ebegu.einstellung.validation.ValidEinstellungValueTypeConstraintValidator;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(EasyMockRunner.class)
class ValidEinstellungValueTypeConstraintValidatorTest extends EasyMockSupport {

	ConstraintValidatorContext constraintValidatorContext;
	ConstraintViolationBuilder constraintViolationBuilder;

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

	ValidEinstellungValueTypeConstraintValidator validator =
		new ValidEinstellungValueTypeConstraintValidator();

	@Nested
	class BooleanEinstellungTest {

		@ParameterizedTest
		@ValueSource(strings = { "true", "TRUE", "tRuE", "false", "FALSE",
			"fAlSe" })
		void booleanStringShouldBeValidBooleanEinstellungValue(
			String booleanString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
					booleanString,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertTrue(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "nope", "nei" })
		void nonBooleanStringShouldNotBeValidBooleanEinstellungValue(
			String nonBooleanString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
					nonBooleanString,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@Test
		void emptyStringShouldNotBeValidBooleanEinstellungValue() {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
					"",
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}
	}

	@Nested
	class NumberEinstellungTest {

		@ParameterizedTest
		@ValueSource(strings = { "1", "0", "-1", "1.5", "1.5", "0.00000000" })
		void numberStringShouldBeValidNumberEinstellungValue(
			String numberString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
					numberString,
					new Gesuchsperiode()
				);
			assertTrue(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "1a", "a1", "0,00", "1/5" })
		void nonNumberStringShouldNotBeValidNumberEinstellungValue(
			String nonNumberString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
					nonNumberString,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@Test
		void emptyStringShouldNotBeValidNumberEinstellungValue() {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA,
					"",
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}
	}

	@Nested
	class EnumEinstellungTest {

		@ParameterizedTest
		@EnumSource(FinanzielleSituationTyp.class)
		void numberStringShouldBeValidNumberEinstellungValue(
			FinanzielleSituationTyp enumValue
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.FINANZIELLE_SITUATION_TYP,
					enumValue.toString(),
					new Gesuchsperiode()
				);
			assertTrue(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "GWUESS", "0,00", "1/5", "TRUE", "false" })
		void nonEnumStringShouldNotBeValidEnumEinstellungValue(
			String nonEnumValue
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.FINANZIELLE_SITUATION_TYP,
					nonEnumValue,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@EnumSource(AntragCopyType.class)
		void wrongEnumStringShouldNotBeValidEnumEinstellungValue(
			AntragCopyType wrongEnumValue
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.FINANZIELLE_SITUATION_TYP,
					wrongEnumValue.toString(),
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@Test
		void emptyStringShouldNotBeValidNumberEinstellungValue() {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.FINANZIELLE_SITUATION_TYP,
					"",
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}
	}

	@Nested
	class DateEinstellungTest {

		@ParameterizedTest
		@ValueSource(strings = "2020-11-30")
		void dateStringShouldBeValidDateEinstellungValue(String dateString) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
					dateString,
					new Gesuchsperiode()
				);
			assertTrue(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "gwuess", "1a", "a1", "0,00", "1/5" })
		void nonDateStringShouldNotBeValidDateEinstellungValue(
			String nonDateString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
					nonDateString,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@ParameterizedTest
		@ValueSource(strings = { "60.02.2020", "01.13.2020", "01-12-2020",
			"2020-30-12", "2020.12.01", "01/01/2020", "1.01.2020", "1.1.2020",
			"01.1.2020" })
		void wrongFormatDateStringShouldNotBeValidDateEinstellungValue(
			String dateString
		) {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
					dateString,
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
			);
		}

		@Test
		void emptyStringShouldNotBeValidNumberEinstellungValue() {
			Einstellung einstellung =
				new Einstellung(
					EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
					"",
					new Gesuchsperiode()
				);
			mockContextMessageBuilding();
			assertFalse(
				validator.isValid(einstellung, constraintValidatorContext)
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
}
