package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.services.factories.users.IUserFactory;
import bizz.addonai.users.msuserspoc.services.factories.users.impl.AdminUserFactory;
import bizz.addonai.users.msuserspoc.services.factories.users.impl.RegularUserFactory;
import bizz.addonai.users.msuserspoc.services.factories.users.IUserFactory;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactoryProviderImpl;

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
        IUserFactory factory = provider.getFactory(UserType.ADMIN);
        assertThat(factory).isInstanceOf(AdminUserFactory.class);
    }

    @Test
    void getFactory_regularType_returnsRegularFactory() {
        IUserFactory factory = provider.getFactory(UserType.REGULAR);
        assertThat(factory).isInstanceOf(RegularUserFactory.class);
    }

    @Test
    void getFactory_unknownType_throwsInvalidUserTypeException() {
        assertThatThrownBy(() -> provider.getFactory(UserType.SUPER_ADMIN))
                .isInstanceOf(InvalidUserTypeException.class)
                .hasMessageContaining("Unknown user type");
    }

    @Test
    void supports_knownType_returnsTrue() {
        assertThat(provider.supports(UserType.ADMIN)).isTrue();
        assertThat(provider.supports(UserType.REGULAR)).isTrue();
    }

    @Test
    void supports_unknownType_returnsFalse() {
        assertThat(provider.supports(UserType.GUEST)).isFalse();
    }


}
