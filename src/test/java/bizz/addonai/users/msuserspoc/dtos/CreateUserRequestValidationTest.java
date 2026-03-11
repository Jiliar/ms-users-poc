package bizz.addonai.users.msuserspoc.dtos;

import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private CreateUserRequest validRequest() {
        return CreateUserRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Secret@123")
                .userType(UserType.ADMIN)
                .adminLevel("SENIOR")
                .build();
    }

    private Set<ConstraintViolation<CreateUserRequest>> validate(CreateUserRequest req) {
        return validator.validate(req);
    }

    // --- username ---

    @Test
    void username_blank_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setUsername("");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void username_tooShort_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setUsername("ab");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void username_tooLong_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setUsername("a".repeat(51));
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void username_invalidChars_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setUsername("john doe!");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    // --- email ---

    @Test
    void email_blank_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setEmail("");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void email_invalidFormat_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setEmail("not-an-email");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    // --- password ---

    @Test
    void password_tooShort_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setPassword("Ab@1");
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

//    @Test
//    void password_noUppercase_failsValidation() {
//        CreateUserRequest req = validRequest();
//        req.setPassword("secret@123");
//        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
//    }

//    @Test
//    void password_noSpecialChar_failsValidation() {
//        CreateUserRequest req = validRequest();
//        req.setPassword("Secret1234");
//        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
//    }

    @Test
    void password_valid_passesValidation() {
        CreateUserRequest req = validRequest();
        req.setPassword("Secret@123");
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    // --- userType ---

    @Test
    void userType_null_failsValidation() {
        CreateUserRequest req = validRequest();
        req.setUserType(null);
        assertThat(validate(req)).anyMatch(v -> v.getPropertyPath().toString().equals("userType"));
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void userType_allEnumValues_passesValidation(UserType type) {
        CreateUserRequest req = validRequest();
        req.setUserType(type);
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("userType"));
    }

    // --- subscriptionType ---

    @ParameterizedTest
    @EnumSource(SubscriptionType.class)
    void subscriptionType_allEnumValues_passesValidation(SubscriptionType type) {
        CreateUserRequest req = validRequest();
        req.setSubscriptionType(type);
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("subscriptionType"));
    }

    @Test
    void subscriptionType_null_passesValidation() {
        CreateUserRequest req = validRequest();
        req.setSubscriptionType(null);
        assertThat(validate(req)).noneMatch(v -> v.getPropertyPath().toString().equals("subscriptionType"));
    }

    // --- full valid request ---

    @Test
    void allFieldsValid_noViolations() {
        assertThat(validate(validRequest())).isEmpty();
    }
}
