package reserve.signup.infrastructure.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class PasswordConfirmationValidator implements ConstraintValidator<PasswordConfirmationCheck, Object> {

    private String message;
    private Field passwordField;
    private Field confirmationField;

    @Override
    public void initialize(PasswordConfirmationCheck constraintAnnotation) {
        message = constraintAnnotation.message();
        Class<?> target = constraintAnnotation.targetClass();
        passwordField = getAnnotatedField(target, Password.class);
        confirmationField = getAnnotatedField(target, Confirmation.class);
    }

    private Field getAnnotatedField(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(annotationClass) != null) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String passwordValue = getFieldValue(value, passwordField);
        String confirmationValue = getFieldValue(value, confirmationField);
        if (passwordValue == null || !passwordValue.equals(confirmationValue)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(confirmationField.getName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private String getFieldValue(Object value, Field field) {
        if (field == null) {
            return null;
        }

        try {
            return (String) field.get(value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}