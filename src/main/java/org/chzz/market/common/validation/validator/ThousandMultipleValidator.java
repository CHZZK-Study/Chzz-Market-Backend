package org.chzz.market.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

public class ThousandMultipleValidator implements ConstraintValidator<ThousandMultiple, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && value % 1000 == 0 && value > 0;
    }
}
