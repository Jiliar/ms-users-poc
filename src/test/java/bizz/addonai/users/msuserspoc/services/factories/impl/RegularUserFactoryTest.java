package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.services.jpa.factories.impl.RegularUserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
                .subscriptionType(SubscriptionType.PREMIUM)
                .newsletterSubscribed(true)
                .build();

        UserEntity result = factory.createUser(request, "hash");

        assertThat(result).isInstanceOf(RegularUser.class);
        RegularUser regular = (RegularUser) result;
        assertThat(regular.getUsername()).isEqualTo("user1");
        assertThat(regular.getEmail()).isEqualTo("user@test.com");
        assertThat(regular.getPassword()).isEqualTo("hash");
        assertThat(regular.getSubscriptionType()).isEqualTo(SubscriptionType.PREMIUM);
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

        assertThat(regular.getSubscriptionType()).isEqualTo(SubscriptionType.FREE);
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

    @Test
    void createUser_regularPermissionsAndDashboard() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .subscriptionType(SubscriptionType.FREE)
                .build();

        RegularUser regular = (RegularUser) factory.createUser(request, "hash");

        assertThat(regular.getPermissions()).isEqualTo("LIMITED:READ,WRITE");
        assertThat(regular.getDashboardUrl()).isEqualTo("/user/dashboard");
    }
}
