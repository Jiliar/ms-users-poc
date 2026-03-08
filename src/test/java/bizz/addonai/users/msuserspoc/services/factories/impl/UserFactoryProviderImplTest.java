package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserFactoryProviderImplTest {

    private UserFactoryProviderImpl provider;
    private AdminUserFactory adminFactory;
    private RegularUserFactory regularFactory;

    @BeforeEach
    void setUp() {
        adminFactory = new AdminUserFactory();
        regularFactory = new RegularUserFactory();
        provider = new UserFactoryProviderImpl(List.of(adminFactory, regularFactory));
    }

    @Test
    void getFactory_adminType_returnsAdminFactory() {
        UserFactory factory = provider.getFactory("ADMIN");
        assertThat(factory).isInstanceOf(AdminUserFactory.class);
    }

    @Test
    void getFactory_regularType_returnsRegularFactory() {
        UserFactory factory = provider.getFactory("REGULAR");
        assertThat(factory).isInstanceOf(RegularUserFactory.class);
    }

    @Test
    void getFactory_caseInsensitive_returnsFactory() {
        UserFactory factory = provider.getFactory("admin");
        assertThat(factory).isInstanceOf(AdminUserFactory.class);
    }

    @Test
    void getFactory_unknownType_throwsInvalidUserTypeException() {
        assertThatThrownBy(() -> provider.getFactory("SUPERADMIN"))
                .isInstanceOf(InvalidUserTypeException.class)
                .hasMessageContaining("Unknown user type");
    }

    @Test
    void supports_knownType_returnsTrue() {
        assertThat(provider.supports("ADMIN")).isTrue();
        assertThat(provider.supports("REGULAR")).isTrue();
    }

    @Test
    void supports_unknownType_returnsFalse() {
        assertThat(provider.supports("GUEST")).isFalse();
    }

    @Test
    void supports_caseInsensitive_returnsTrue() {
        assertThat(provider.supports("admin")).isTrue();
        assertThat(provider.supports("regular")).isTrue();
    }
}
