package ch.dvbern.ebegu.einstellung.validation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidEinstellungValueTypeConstraintValidator.class)
public @interface ValidEinstellungValueType {
	String message() default "{invalid_einstellung}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
