package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private Set<ConstraintViolation<UpdateUserRequest>> validate(UpdateUserRequest req) {
        return validator.validate(req);
    }

    @Test
    void allNull_passesValidation() {
        assertThat(validate(new UpdateUserRequest())).isEmpty();
    }

    @Test
    void username_tooShort_failsValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().username("ab").build();
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void username_invalidChars_failsValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().username("john doe!").build();
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void username_valid_passesValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().username("john_doe").build();
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void email_invalidFormat_failsValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().email("not-an-email").build();
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void email_valid_passesValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().email("new@example.com").build();
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @ParameterizedTest
    @EnumSource(SubscriptionType.class)
    void subscriptionType_allEnumValues_passesValidation(SubscriptionType type) {
        UpdateUserRequest req = UpdateUserRequest.builder().subscriptionType(type).build();
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("subscriptionType"));
    }

    @Test
    void subscriptionType_null_passesValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().subscriptionType(null).build();
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("subscriptionType"));
    }

    @Test
    void adminLevel_tooLong_failsValidation() {
        UpdateUserRequest req = UpdateUserRequest.builder().adminLevel("A".repeat(51)).build();
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("adminLevel"));
    }
}
