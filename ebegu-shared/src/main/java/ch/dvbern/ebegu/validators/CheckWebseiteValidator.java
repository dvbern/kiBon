package ch.dvbern.ebegu.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.util.Constants;

public class CheckWebseiteValidator implements
	ConstraintValidator<CheckWebseite, String> {

	@Override
	public boolean isValid(
		String webseite,
		ConstraintValidatorContext constraintValidatorContext
	) {
		if (webseite == null) {
			return true;
		}

		return webseite.matches(Constants.PATTERN_URL);
	}
}
