package ch.dvbern.ebegu.validators.params;

import java.lang.annotation.Annotation;

import jakarta.validation.Payload;

import ch.dvbern.ebegu.entities.VersendeteMail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FieldOfClassValidatorTest {

	@ParameterizedTest
	@ValueSource(strings = { "empfaengerAdresse", "zeitpunktVersand",
		"betreff" })
	void isValid_shouldReturnTrue_whenFieldExistsOnTargetClass(String name) {
		FieldOfClassValidator validator = createValidator(true);

		boolean result = validator.isValid(name, null);

		assertThat(result, is(true));
	}

	@Test
	void isValid_shouldReturnFalse_whenFieldDoesNotExistOnTargetClass() {
		FieldOfClassValidator validator = createValidator(true);

		boolean result = validator.isValid("nonExistentField", null);

		assertThat(result, is(false));
	}

	@Test
	void isValid_shouldReturnTrue_whenValueIsNull() {
		FieldOfClassValidator validator = createValidator(true);

		boolean result = validator.isValid(null, null);

		assertThat(result, is(true));
	}

	@ParameterizedTest
	@ValueSource(strings = { "id", "userErstellt", "userMutiert",
		"timestampErstellt", "timestampMutiert" })
	void isValid_shouldReturnTrue_whenFieldExistsOnSuperclassAndInheritedIncluded() {
		FieldOfClassValidator validator = createValidator(true);

		boolean result = validator.isValid("id", null);

		assertThat(result, is(true));
	}

	@ParameterizedTest
	@ValueSource(strings = { "id", "userErstellt", "userMutiert",
		"timestampErstellt", "timestampMutiert" })
	void isValid_shouldReturnFalse_whenFieldExistsOnSuperclassAndInheritedExcluded() {
		FieldOfClassValidator validator = createValidator(false);

		boolean result = validator.isValid("inheritedField", null);

		assertThat(result, is(false));
	}

	// The validator requires the target class to be specified in the annotation.
	// Therefore, we have to run the initialize method ourselves and provide the target class.
	private FieldOfClassValidator createValidator(boolean includeInherited) {
		FieldOfClassValidator validator = new FieldOfClassValidator();
		validator.initialize(prepareAnnotation(includeInherited));
		return validator;
	}

	private FieldOfClass prepareAnnotation(boolean includeInherited) {
		return new FieldOfClass() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public Class<?> targetClass() {
				return VersendeteMail.class;
			}

			@Override
			public boolean includeInherited() {
				return includeInherited;
			}

			@Override
			public String message() {
				return "{field.notfound}";
			}

			@Override
			public Class<?>[] groups() {
				return new Class<?>[0];
			}

			@Override
			public Class<? extends Payload>[] payload() {
				return new Class[0];
			}

		};
	}

}
