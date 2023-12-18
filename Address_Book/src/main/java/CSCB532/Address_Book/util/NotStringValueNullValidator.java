package CSCB532.Address_Book.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotStringValueNullValidator implements ConstraintValidator<NotStringValueNull, String> {

    @Override
    public void initialize(NotStringValueNull constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !"null".equals(value);
    }
}
