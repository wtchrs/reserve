package reserve.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullOrNotEmptyValidator implements ConstraintValidator<NullOrNotEmpty, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        return value == null || !value.toString().trim().isEmpty();
    }

}
