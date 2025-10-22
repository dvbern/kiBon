package ch.dvbern.ebegu.einstellung.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.einstellung.ApplicationProperty;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

public class ValidApplicationPropertyValueTypeConstraintValidator extends
	AbstractValueTypeConstraintValidator
	implements
	ConstraintValidator<ValidApplicationPropertyValueType, ApplicationProperty> {

	@Override
	public boolean isValid(
		ApplicationProperty applicationProperty,
		ConstraintValidatorContext context
	) {
		ApplicationPropertyKey key = applicationProperty.getName();
		final String methodName =
			"ValidApplicationPropertyValueTypeConstraintValidator.isValid";

		try {
			Field field = key.getDeclaringClass().getField(key.name());
			Annotation[] annotations = field.getAnnotations();
			if (annotations.length == 0) {
				throw new EbeguRuntimeException(
					methodName,
					"One annotation per ApplicationPropertyKey is required"
				);
			}
			if (annotations.length > 1) {
				throw new EbeguRuntimeException(
					methodName,
					"Only one annotation per ApplicationPropertyKey allowed"
				);
			}

			Annotation annotation = annotations[0];

			return isAnnotatedFieldValid(
				annotation,
				key.name(),
				applicationProperty.getValue(),
				context
			);
		} catch (NoSuchFieldException e) {
			throw new EbeguRuntimeException(
				methodName,
				"No such field: " + key.name(),
				e
			);
		}
	}

}
