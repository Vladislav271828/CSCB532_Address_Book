package CSCB532.Address_Book.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotStringValueNullValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotStringValueNull {
    String message() default "Field cannot be the string 'null'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

