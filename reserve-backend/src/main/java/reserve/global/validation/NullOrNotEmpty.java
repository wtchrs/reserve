package reserve.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NullOrNotEmptyValidator.class)
public @interface NullOrNotEmpty {

    String message() default "must not be empty string.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
