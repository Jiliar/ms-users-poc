package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import bizz.addonai.users.msuserspoc.exceptions.BadRequestException;
import bizz.addonai.users.msuserspoc.exceptions.ConflictException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.repositories.IUserRepository;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    @Mock private IUserRepository userRepository;
    @Mock private IUserFactoryProvider factoryProvider;
    @Mock private PasswordService passwordService;
    @Mock private UserFactory userFactory;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, factoryProvider, passwordService);
    }

    private AdminUser adminUserWith(UUID id, String username, String email) {
        AdminUser user = AdminUser.builder()
                .username(username).email(email).password("hash")
                .adminLevel("SENIOR").department("IT").build();
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUserType(UserType.ADMIN);
        return user;
    }

    private RegularUser regularUserWith(UUID id, String username, String email) {
        RegularUser user = RegularUser.builder()
                .username(username).email(email).password("hash")
                .subscriptionType(SubscriptionType.FREE).newsletterSubscribed(false).build();
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
                .username("admin1").email("admin@test.com").password("Pass@1234")
                .userType(UserType.ADMIN).adminLevel("SENIOR").department("IT").build();
        AdminUser entity = adminUserWith(id, "admin1", "admin@test.com");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("admin1")).thenReturn(false);
        when(passwordService.encryptPassword("Pass@1234")).thenReturn("hash");
        when(factoryProvider.getFactory("ADMIN")).thenReturn(userFactory);
        when(userFactory.createUser(request, "hash")).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("admin1");
        assertThat(result.getAdminLevel()).isEqualTo("SENIOR");
        assertThat(result.getPermissions()).isEqualTo("ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM");
    }

    @Test
    void createUser_regularUser_success() {
        UUID id = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1").email("user@test.com").password("Pass@1234")
                .userType(UserType.REGULAR).subscriptionType(SubscriptionType.BASIC).newsletterSubscribed(true).build();
        RegularUser entity = regularUserWith(id, "user1", "user@test.com");
        entity.setSubscriptionType(SubscriptionType.BASIC);
        entity.setNewsletterSubscribed(true);

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(passwordService.encryptPassword("Pass@1234")).thenReturn("hash");
        when(factoryProvider.getFactory("REGULAR")).thenReturn(userFactory);
        when(userFactory.createUser(request, "hash")).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);

        UserDTO result = userService.createUser(request);

        assertThat(result.getSubscriptionType()).isEqualTo(SubscriptionType.BASIC);
        assertThat(result.getNewsletterSubscribed()).isTrue();
    }

    @Test
    void createUser_duplicateEmail_throwsConflictException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1").email("dup@test.com").password("Pass@1234").userType(UserType.REGULAR).build();
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void createUser_duplicateUsername_throwsConflictException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("taken").email("new@test.com").password("Pass@1234").userType(UserType.REGULAR).build();
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void createUser_dbFailure_throwsInternalServerError() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1").email("user@test.com").password("Pass@1234").userType(UserType.REGULAR).build();
        AdminUser entity = adminUserWith(UUID.randomUUID(), "user1", "user@test.com");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordService.encryptPassword(any())).thenReturn("hash");
        when(factoryProvider.getFactory(any())).thenReturn(userFactory);
        when(userFactory.createUser(any(), any())).thenReturn(entity);
        when(userRepository.save(any())).thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(InternalServerErrorException.class);
    }

    // --- getAllUsers ---

    @Test
    @SuppressWarnings("unchecked")
    void getAllUsers_noFilters_returnsPagedResult() {
        AdminUser a = adminUserWith(UUID.randomUUID(), "admin", "admin@test.com");
        RegularUser r = regularUserWith(UUID.randomUUID(), "user", "user@test.com");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a, r)));

        UserPageResponse result = userService.getAllUsers(null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageInfo().getTotalElements()).isEqualTo(2);
        assertThat(result.getPageInfo().getPage()).isEqualTo(0);
        assertThat(result.getPageInfo().getSize()).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllUsers_withPageInput_appliesPageRequest() {
        AdminUser a = adminUserWith(UUID.randomUUID(), "admin", "admin@test.com");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a)));

        PageInput pageInput = PageInput.builder().page(0).size(5).sortBy("username").sortDirection("ASC").build();
        UserPageResponse result = userService.getAllUsers(null, pageInput);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllUsers_invalidSortBy_throwsBadRequest() {
        PageInput pageInput = PageInput.builder().sortBy("password").build();

        assertThatThrownBy(() -> userService.getAllUsers(null, pageInput))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sortBy field");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllUsers_withDateFilter_usesSpecification() {
        AdminUser a = adminUserWith(UUID.randomUUID(), "admin", "admin@test.com");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a)));

        UserFilterInput filter = UserFilterInput.builder()
                .startDate("2025-01-01").endDate("2025-12-31").build();
        UserPageResponse result = userService.getAllUsers(filter, null);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getAllUsers_invalidStartDate_throwsBadRequest() {
        UserFilterInput filter = UserFilterInput.builder().startDate("not-a-date").build();

        assertThatThrownBy(() -> userService.getAllUsers(filter, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid date format for startDate");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllUsers_dbFailure_throwsInternalServerError() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThatThrownBy(() -> userService.getAllUsers(null, null))
                .isInstanceOf(InternalServerErrorException.class);
    }

    // --- getUserById ---

    @Test
    void getUserById_found_returnsOptionalWithDTO() {
        UUID id = UUID.randomUUID();
        AdminUser entity = adminUserWith(id, "admin", "admin@test.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<UserDTO> result = userService.getUserById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    @Test
    void getUserById_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_dbFailure_throwsInternalServerError() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenThrow(new DataAccessResourceFailureException("DB down"));

        assertThatThrownBy(() -> userService.getUserById(id))
                .isInstanceOf(InternalServerErrorException.class);
    }

    // --- updateUser ---

    @Test
    void updateUser_adminUser_updatesFields() {
        UUID id = UUID.randomUUID();
        AdminUser entity = adminUserWith(id, "admin", "admin@test.com");
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newAdmin").email("new@admin.com").adminLevel("SUPER").department("OPS").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        Optional<UserDTO> result = userService.updateUser(id, request);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("newAdmin");
        assertThat(result.get().getAdminLevel()).isEqualTo("SUPER");
    }

    @Test
    void updateUser_notFound_returnsEmpty() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.updateUser(id, new UpdateUserRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_regularUser_updatesSubscription() {
        UUID id = UUID.randomUUID();
        RegularUser entity = regularUserWith(id, "user", "user@test.com");
        UpdateUserRequest request = UpdateUserRequest.builder()
                .subscriptionType(SubscriptionType.PREMIUM).newsletterSubscribed(true).build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);

        Optional<UserDTO> result = userService.updateUser(id, request);

        assertThat(result).isPresent();
        assertThat(result.get().getSubscriptionType()).isEqualTo(SubscriptionType.PREMIUM);
    }

    // --- deleteUser ---

    @Test
    void deleteUser_existing_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        boolean result = userService.deleteUser(id);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound_returnsFalse() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        boolean result = userService.deleteUser(id);

        assertThat(result).isFalse();
        verify(userRepository, never()).deleteById(any());
    }
}
