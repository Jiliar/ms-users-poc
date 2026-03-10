package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminUserFactoryTest {

    private AdminUserFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AdminUserFactory();
    }

    @Test
    void getFactoryType_returnsADMIN() {
        assertThat(factory.getFactoryType()).isEqualTo("ADMIN");
    }

    @Test
    void createUser_withAllFields_buildsAdminUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .userType(UserType.ADMIN)
                .adminLevel("SENIOR")
                .department("IT")
                .build();

        UserEntity result = factory.createUser(request, "hashedPass");

        assertThat(result).isInstanceOf(AdminUser.class);
        AdminUser admin = (AdminUser) result;
        assertThat(admin.getUsername()).isEqualTo("admin1");
        assertThat(admin.getEmail()).isEqualTo("admin@test.com");
        assertThat(admin.getPassword()).isEqualTo("hashedPass");
        assertThat(admin.getAdminLevel()).isEqualTo("SENIOR");
        assertThat(admin.getDepartment()).isEqualTo("IT");
    }

    @Test
    void createUser_nullDepartment_defaultsToGENERAL() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .adminLevel("SENIOR")
                .department(null)
                .build();

        AdminUser admin = (AdminUser) factory.createUser(request, "hash");

        assertThat(admin.getDepartment()).isEqualTo("GENERAL");
    }

    @Test
    void createUser_nullAdminLevel_throwsInvalidUserTypeException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .adminLevel(null)
                .build();

        assertThatThrownBy(() -> factory.createUser(request, "hash"))
                .isInstanceOf(InvalidUserTypeException.class)
                .hasMessageContaining("Admin level is required");
    }

    @Test
    void createUser_emptyAdminLevel_throwsInvalidUserTypeException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .adminLevel("")
                .build();

        assertThatThrownBy(() -> factory.createUser(request, "hash"))
                .isInstanceOf(InvalidUserTypeException.class);
    }

    @Test
    void createUser_adminPermissionsAndDashboard() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .adminLevel("SUPER")
                .build();

        AdminUser admin = (AdminUser) factory.createUser(request, "hash");

        assertThat(admin.getPermissions()).isEqualTo("ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM");
        assertThat(admin.getDashboardUrl()).isEqualTo("/admin/dashboard");
    }

    @Test
    void validateUserData_validAdminLevel_doesNotThrow() {
        CreateUserRequest request = CreateUserRequest.builder()
                .adminLevel("STANDARD")
                .build();

        // Should not throw
        factory.validateUserData(request);
    }
}
