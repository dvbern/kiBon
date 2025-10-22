package ch.dvbern.ebegu.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.util.Constants;

public class CheckEmailValidator implements
	ConstraintValidator<CheckEmail, String> {

	@Override
	public boolean isValid(
		String email,
		ConstraintValidatorContext constraintValidatorContext
	) {
		if (email == null) {
			return true;
		}

		return email.matches(Constants.PATTERN_EMAIL);
	}
}
