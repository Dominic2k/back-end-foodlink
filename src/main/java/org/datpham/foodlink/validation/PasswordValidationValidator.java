package org.datpham.foodlink.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidationValidator implements ConstraintValidator<PasswordValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // TODO: Replace with your real password policy.
        if (value == null || value.isBlank()) {
            return false;
        }

        boolean hasLetter = value.chars().anyMatch(Character::isLetter);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        return value.length() >= 8 && hasLetter && hasDigit;
    }
}
