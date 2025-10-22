package ch.dvbern.ebegu.validators.bicswift;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = CheckBicSwiftValidator.class)
public @interface CheckBicSwift {

	String message() default "{invalid_bic_swift}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
