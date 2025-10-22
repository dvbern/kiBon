package ch.dvbern.ebegu.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckWebseiteValidator.class)
public @interface CheckWebseite {

	String message() default "{validator.constraints.url.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
