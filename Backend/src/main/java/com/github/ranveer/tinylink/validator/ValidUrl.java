package com.github.ranveer.tinylink.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

    String message() default "Please provide a valid HTTP or HTTPS URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}