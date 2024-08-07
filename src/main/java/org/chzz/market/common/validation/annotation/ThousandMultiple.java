package org.chzz.market.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.chzz.market.common.validation.validator.ThousandMultipleValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ThousandMultipleValidator.class)
public @interface ThousandMultiple {
    String message() default "값은 1000의 배수여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
