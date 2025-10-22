package ch.dvbern.ebegu.einstellung.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

public class ValidEinstellungValueTypeConstraintValidator extends
	AbstractValueTypeConstraintValidator implements
	ConstraintValidator<ValidEinstellungValueType, Einstellung> {

	@Override
	public boolean isValid(
		Einstellung einstellung,
		ConstraintValidatorContext context
	) {
		EinstellungKey key = einstellung.getKey();
		final String methodName =
			"ValidEinstellungValueTypeConstraintValidator.isValid";

		try {
			Field field = key.getDeclaringClass().getField(key.name());
			Annotation[] annotations = field.getAnnotations();
			if (annotations.length == 0) {
				throw new EbeguRuntimeException(
					methodName,
					"One annotation per EinstellungKey is required"
				);
			}
			if (annotations.length > 1) {
				throw new EbeguRuntimeException(
					methodName,
					"Only one annotation per EinstellungKey allowed"
				);
			}

			Annotation annotation = annotations[0];

			return isAnnotatedFieldValid(
				annotation,
				einstellung.getKey().name(),
				einstellung.getValue(),
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
