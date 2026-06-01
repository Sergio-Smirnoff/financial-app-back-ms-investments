package com.financialapp.investments.infrastructure.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IolPropertiesValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validProps_noViolations() {
        IolProperties props = new IolProperties(
                "https://api.example.com",
                "user",
                "pass",
                "0 */15 10-17 * * MON-FRI");

        assertThat(validator.validate(props)).isEmpty();
    }

    @Test
    void blankBaseUrl_violates() {
        IolProperties props = new IolProperties(
                "", "user", "pass", "0 */15 10-17 * * MON-FRI");

        Set<ConstraintViolation<IolProperties>> violations = validator.validate(props);
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("baseUrl"));
    }

    @Test
    void blankUsername_violates() {
        IolProperties props = new IolProperties(
                "https://api.example.com", "", "pass", "0 */15 10-17 * * MON-FRI");

        assertThat(validator.validate(props))
                .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankPassword_violates() {
        IolProperties props = new IolProperties(
                "https://api.example.com", "user", "", "0 */15 10-17 * * MON-FRI");

        assertThat(validator.validate(props))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void malformedCron_violatesPattern() {
        IolProperties props = new IolProperties(
                "https://api.example.com", "user", "pass", "not-a-cron");

        assertThat(validator.validate(props))
                .anyMatch(v -> v.getPropertyPath().toString().equals("priceRefreshCron"));
    }

    @Test
    void cronWithFiveFields_violatesPattern() {
        IolProperties props = new IolProperties(
                "https://api.example.com", "user", "pass", "0 0 12 * *");

        assertThat(validator.validate(props))
                .anyMatch(v -> v.getPropertyPath().toString().equals("priceRefreshCron"));
    }

    @Test
    void cronWithSixFields_passes() {
        IolProperties props = new IolProperties(
                "https://api.example.com", "user", "pass", "0 0 12 * * MON-FRI");

        assertThat(validator.validate(props)).isEmpty();
    }
}
