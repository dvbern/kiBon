package ch.dvbern.ebegu.validators.gemeindeantraege.lats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AngabenTagesschulePlausibilisierungValidator.class)
public @interface AngabenTagesschulePlausibilisierung {
	String message() default "{lats_angaben_tagesschule_plausibilisierung_failed}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
