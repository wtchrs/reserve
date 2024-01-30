package reserve.signup.infrastructure.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConfirmationValidator.class)
public @interface PasswordConfirmationCheck {

    String message() default "Not matched password and password confirmation.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<?> targetClass();

}
