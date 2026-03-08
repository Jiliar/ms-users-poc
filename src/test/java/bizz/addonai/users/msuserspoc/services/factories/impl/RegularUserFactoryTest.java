package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegularUserFactoryTest {

    private RegularUserFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RegularUserFactory();
    }

    @Test
    void getFactoryType_returnsREGULAR() {
        assertThat(factory.getFactoryType()).isEqualTo("REGULAR");
    }

    @Test
    void createUser_withAllFields_buildsRegularUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .subscriptionType("PREMIUM")
                .newsletterSubscribed(true)
                .build();

        UserEntity result = factory.createUser(request, "hash");

        assertThat(result).isInstanceOf(RegularUser.class);
        RegularUser regular = (RegularUser) result;
        assertThat(regular.getUsername()).isEqualTo("user1");
        assertThat(regular.getEmail()).isEqualTo("user@test.com");
        assertThat(regular.getPassword()).isEqualTo("hash");
        assertThat(regular.getSubscriptionType()).isEqualTo("PREMIUM");
        assertThat(regular.isNewsletterSubscribed()).isTrue();
    }

    @Test
    void createUser_nullSubscriptionType_defaultsToFREE() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .subscriptionType(null)
                .build();

        RegularUser regular = (RegularUser) factory.createUser(request, "hash");

        assertThat(regular.getSubscriptionType()).isEqualTo("FREE");
    }

    @Test
    void createUser_nullNewsletterSubscribed_defaultsFalse() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .newsletterSubscribed(null)
                .build();

        RegularUser regular = (RegularUser) factory.createUser(request, "hash");

        assertThat(regular.isNewsletterSubscribed()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"FREE", "BASIC", "PREMIUM", "ENTERPRISE"})
    void validateUserData_validSubscriptionTypes_doesNotThrow(String subscriptionType) {
        CreateUserRequest request = CreateUserRequest.builder()
                .subscriptionType(subscriptionType)
                .build();

        // Should not throw
        factory.validateUserData(request);
    }

    @Test
    void validateUserData_invalidSubscriptionType_throwsIllegalArgument() {
        CreateUserRequest request = CreateUserRequest.builder()
                .subscriptionType("GOLD")
                .build();

        assertThatThrownBy(() -> factory.validateUserData(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid subscription type");
    }

    @Test
    void validateUserData_nullSubscriptionType_doesNotThrow() {
        CreateUserRequest request = CreateUserRequest.builder()
                .subscriptionType(null)
                .build();

        // null is valid — no subscription type set
        factory.validateUserData(request);
    }

    @Test
    void createUser_regularPermissionsAndDashboard() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .subscriptionType("FREE")
                .build();

        RegularUser regular = (RegularUser) factory.createUser(request, "hash");

        assertThat(regular.getPermissions()).isEqualTo("LIMITED:READ,WRITE");
        assertThat(regular.getDashboardUrl()).isEqualTo("/user/dashboard");
    }
}
