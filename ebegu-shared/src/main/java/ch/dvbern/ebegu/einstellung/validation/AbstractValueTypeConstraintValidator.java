package ch.dvbern.ebegu.einstellung.validation;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.einstellung.BooleanEinstellung;
import ch.dvbern.ebegu.einstellung.DateEinstellung;
import ch.dvbern.ebegu.einstellung.EnumEinstellung;
import ch.dvbern.ebegu.einstellung.EnumMultiSelectionEinstellung;
import ch.dvbern.ebegu.einstellung.NumberEinstellung;
import ch.dvbern.ebegu.einstellung.StringEinstellung;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.ValidationMessageUtil;
import org.apache.commons.lang3.math.NumberUtils;

public abstract class AbstractValueTypeConstraintValidator {

	protected boolean isAnnotatedFieldValid(
		Annotation annotation,
		String keyName,
		String value,
		ConstraintValidatorContext context
	) {
		if (annotation instanceof BooleanEinstellung) {
			final boolean valid = isValidBoolean(value);
			if (!valid) {
				createConstraintViolation(value, keyName, context);
			}
			return valid;
		}

		if (annotation instanceof DateEinstellung) {
			final boolean valid = hasValidDateValue(value);
			if (!valid) {
				createConstraintViolation(value, keyName, context);
			}
			return valid;
		}

		if (annotation instanceof EnumEinstellung) {
			final boolean valid = isValueOfEnum(
				value,
				(EnumEinstellung) annotation
			);
			if (!valid) {
				createConstraintViolation(value, keyName, context);
			}
			return valid;
		}

		if (annotation instanceof EnumMultiSelectionEinstellung) {
			final boolean valid = isValueMultiSelectionOfEnum(
				value,
				(EnumMultiSelectionEinstellung) annotation
			);
			if (!valid) {
				createConstraintViolation(value, keyName, context);
			}
			return valid;
		}

		if (annotation instanceof NumberEinstellung) {
			final boolean valid = NumberUtils.isCreatable(value);
			if (!valid) {
				createConstraintViolation(value, keyName, context);
			}
			return valid;
		}

		if (annotation instanceof StringEinstellung) {
			return true;
		}

		throw new EbeguRuntimeException(
			"ValidValueTypeConstraintValidator.isValid",
			"Unknown Annotation Type: " + annotation.getClass().getName()
		);
	}

	private boolean isValueMultiSelectionOfEnum(
		String value,
		EnumMultiSelectionEinstellung annotation
	) {
		return Arrays.stream(value.split(annotation.separator()))
			.allMatch(
				substring -> isValueOfEnum(annotation.enumClass(), substring)
			);
	}

	private static boolean isValidBoolean(String value) {
		return value.compareToIgnoreCase("false") == 0
			|| value.compareToIgnoreCase("true") == 0;
	}

	private static boolean hasValidDateValue(String einstellung) {
		try {
			DateUtil.parseStringToDate(einstellung);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private static boolean isValueOfEnum(
		String einstellung,
		EnumEinstellung annotation
	) {
		return isValueOfEnum(annotation.value(), einstellung);
	}

	private static boolean isValueOfEnum(
		Class<? extends Enum> enumClass,
		String substring
	) {
		try {
			Enum.valueOf(
				enumClass.asSubclass(Enum.class),
				substring
			);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private void createConstraintViolation(
		String value,
		String keyName,
		ConstraintValidatorContext context
	) {
		String message = ValidationMessageUtil.getMessage(
			"invalid_einstellung"
		);
		message = MessageFormat.format(message, value, keyName);

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addConstraintViolation();
	}
}
