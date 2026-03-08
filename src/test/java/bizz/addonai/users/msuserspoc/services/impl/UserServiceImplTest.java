package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.exceptions.UserNotFoundException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.repositories.IUserRepository;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserFactoryProvider factoryProvider;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserFactory userFactory;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, factoryProvider, passwordService);
    }

    // --- helpers ---

    private AdminUser adminUserWith(UUID id, String username, String email) {
        AdminUser user = AdminUser.builder()
                .username(username)
                .email(email)
                .password("hash")
                .adminLevel("SENIOR")
                .department("IT")
                .build();
        // set id via reflection-free workaround: use setter from @Data
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUserType(UserType.ADMIN);
        return user;
    }

    private RegularUser regularUserWith(UUID id, String username, String email) {
        RegularUser user = RegularUser.builder()
                .username(username)
                .email(email)
                .password("hash")
                .subscriptionType("FREE")
                .newsletterSubscribed(false)
                .build();
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUserType(UserType.REGULAR);
        return user;
    }

    // --- createUser ---

    @Test
    void createUser_adminUser_success() {
        UUID id = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .password("Pass@1234")
                .userType("ADMIN")
                .adminLevel("SENIOR")
                .department("IT")
                .build();

        AdminUser entity = adminUserWith(id, "admin1", "admin@test.com");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("admin1")).thenReturn(false);
        when(passwordService.encryptPassword("Pass@1234")).thenReturn("hash");
        when(factoryProvider.getFactory("ADMIN")).thenReturn(userFactory);
        when(userFactory.createUser(request, "hash")).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("admin1");
        assertThat(result.getEmail()).isEqualTo("admin@test.com");
        assertThat(result.getAdminLevel()).isEqualTo("SENIOR");
        assertThat(result.getDepartment()).isEqualTo("IT");
        assertThat(result.getPermissions()).isEqualTo("ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM");
    }

    @Test
    void createUser_regularUser_success() {
        UUID id = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .password("Pass@1234")
                .userType("REGULAR")
                .subscriptionType("BASIC")
                .newsletterSubscribed(true)
                .build();

        RegularUser entity = regularUserWith(id, "user1", "user@test.com");
        entity.setSubscriptionType("BASIC");
        entity.setNewsletterSubscribed(true);

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordService.encryptPassword("Pass@1234")).thenReturn("hash");
        when(factoryProvider.getFactory("REGULAR")).thenReturn(userFactory);
        when(userFactory.createUser(request, "hash")).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getSubscriptionType()).isEqualTo("BASIC");
        assertThat(result.getNewsletterSubscribed()).isTrue();
        assertThat(result.getPermissions()).isEqualTo("LIMITED:READ,WRITE");
    }

    @Test
    void createUser_duplicateEmail_throwsIllegalArgument() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("dup@test.com")
                .password("Pass@1234")
                .userType("REGULAR")
                .build();

        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void createUser_duplicateUsername_throwsIllegalArgument() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("taken")
                .email("new@test.com")
                .password("Pass@1234")
                .userType("REGULAR")
                .build();

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsMappedList() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        AdminUser a = adminUserWith(id1, "admin", "admin@test.com");
        RegularUser r = regularUserWith(id2, "user", "user@test.com");

        when(userRepository.findAll()).thenReturn(List.of(a, r));

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        assertThat(result.get(1).getUsername()).isEqualTo("user");
    }

    @Test
    void getAllUsers_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // --- getUserById ---

    @Test
    void getUserById_found_returnsDTO() {
        UUID id = UUID.randomUUID();
        AdminUser entity = adminUserWith(id, "admin", "admin@test.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        UserDTO result = userService.getUserById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    void getUserById_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // --- updateUser ---

    @Test
    void updateUser_adminUser_updatesFields() {
        UUID id = UUID.randomUUID();
        AdminUser entity = adminUserWith(id, "admin", "admin@test.com");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newAdmin")
                .email("new@admin.com")
                .adminLevel("SUPER")
                .department("OPS")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.updateUser(id, request);

        assertThat(result.getUsername()).isEqualTo("newAdmin");
        assertThat(result.getEmail()).isEqualTo("new@admin.com");
        assertThat(result.getAdminLevel()).isEqualTo("SUPER");
        assertThat(result.getDepartment()).isEqualTo("OPS");
    }

    @Test
    void updateUser_regularUser_updatesSubscription() {
        UUID id = UUID.randomUUID();
        RegularUser entity = regularUserWith(id, "user", "user@test.com");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .subscriptionType("PREMIUM")
                .newsletterSubscribed(true)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.updateUser(id, request);

        assertThat(result.getSubscriptionType()).isEqualTo("PREMIUM");
        assertThat(result.getNewsletterSubscribed()).isTrue();
    }

    @Test
    void updateUser_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(id, new UpdateUserRequest()))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_nullFields_doesNotOverwrite() {
        UUID id = UUID.randomUUID();
        AdminUser entity = adminUserWith(id, "admin", "admin@test.com");

        UpdateUserRequest request = new UpdateUserRequest(); // all nulls

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.updateUser(id, request);

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getEmail()).isEqualTo("admin@test.com");
    }

    // --- deleteUser ---

    @Test
    void deleteUser_existing_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // --- authenticate ---

    @Test
    void authenticate_validCredentials_returnsTrue() {
        String email = "user@test.com";
        RegularUser entity = regularUserWith(UUID.randomUUID(), "user", email);
        entity.setPassword("hash");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(passwordService.verifyPassword("secret", "hash")).thenReturn(true);

        boolean result = userService.authenticate(email, "secret");

        assertThat(result).isTrue();
    }

    @Test
    void authenticate_wrongPassword_returnsFalse() {
        String email = "user@test.com";
        RegularUser entity = regularUserWith(UUID.randomUUID(), "user", email);
        entity.setPassword("hash");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));
        when(passwordService.verifyPassword("wrong", "hash")).thenReturn(false);

        boolean result = userService.authenticate(email, "wrong");

        assertThat(result).isFalse();
    }

    @Test
    void authenticate_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("unknown@test.com", "pass"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
