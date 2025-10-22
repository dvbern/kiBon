package ch.dvbern.ebegu.validators.bicswift;

import java.util.regex.Pattern;

import javax.annotation.Nullable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CheckBicSwiftValidator implements
	ConstraintValidator<CheckBicSwift, String> {

	private static final Pattern BICSWIFT_PATTERN = Pattern.compile(
		"^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$"
	);

	@Override
	public boolean isValid(
		@Nullable String bic,
		@Nullable ConstraintValidatorContext constraintValidatorContext
	) {
		if (bic == null) {
			return true;
		}
		return BICSWIFT_PATTERN.matcher(bic.replaceAll("/\\s+/", "")).matches();
	}
}
