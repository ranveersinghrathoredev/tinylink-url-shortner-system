package com.github.ranveer.tinylink.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            URI uri = new URI(value);
            String host = uri.getHost();


            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && host != null
                    && !host.isBlank()
                    && host.contains(".")
                    && !host.endsWith(".")
                    && uri.getPort() <= 65535;

        } catch (Exception e) {

            return false;
        }
    }
}
