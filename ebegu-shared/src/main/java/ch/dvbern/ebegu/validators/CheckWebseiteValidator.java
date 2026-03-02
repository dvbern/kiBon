package ch.dvbern.ebegu.validators;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.util.Constants;

public class CheckWebseiteValidator implements
	ConstraintValidator<CheckWebseite, String> {

	private static final Pattern PATTERN = Pattern.compile(
		Constants.PATTERN_URL
	);

	@Override
	public boolean isValid(
		String webseite,
		ConstraintValidatorContext constraintValidatorContext
	) {
		if (webseite == null || webseite.trim().isEmpty()) {
			return true;
		}

		return PATTERN.matcher(webseite).matches();
	}
}
