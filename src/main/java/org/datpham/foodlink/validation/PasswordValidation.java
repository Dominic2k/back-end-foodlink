package org.datpham.foodlink.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PasswordValidationValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface PasswordValidation {
    String message() default "Password must be at least 8 characters and contain both letters and digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
