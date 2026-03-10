package bizz.addonai.users.msuserspoc.controllers.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.PageMetadata;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import bizz.addonai.users.msuserspoc.exceptions.BadGatewayException;
import bizz.addonai.users.msuserspoc.exceptions.BadRequestException;
import bizz.addonai.users.msuserspoc.exceptions.ConflictException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.exceptions.NotFoundException;
import bizz.addonai.users.msuserspoc.models.enums.SubscriptionType;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerImplTest {

    @Mock private IUserService userService;
    @InjectMocks private UserControllerImpl controller;

    private UserDTO adminDTO;
    private UserDTO regularDTO;
    private UserPageResponse pagedResponse;

    @BeforeEach
    void setUp() {
        adminDTO = UserDTO.builder()
                .id(UUID.randomUUID()).username("admin1").email("admin@test.com")
                .userType(UserType.ADMIN).permissions("ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM")
                .dashboardUrl("/admin/dashboard").createdAt(LocalDateTime.now())
                .adminLevel("SENIOR").department("IT").build();

        regularDTO = UserDTO.builder()
                .id(UUID.randomUUID()).username("user1").email("user@test.com")
                .userType(UserType.REGULAR).permissions("LIMITED:READ,WRITE")
                .dashboardUrl("/user/dashboard").createdAt(LocalDateTime.now())
                .subscriptionType(SubscriptionType.FREE).newsletterSubscribed(false).build();

        pagedResponse = UserPageResponse.builder()
                .content(List.of(adminDTO, regularDTO))
                .pageInfo(PageMetadata.builder()
                        .page(0).size(10).totalElements(2).totalPages(1)
                        .hasNext(false).hasPrevious(false).build())
                .build();
    }

    // --- allUsers ---

    @Test
    void allUsers_noFilters_returnsPagedResponse() {
        when(userService.getAllUsers(null, null)).thenReturn(pagedResponse);

        UserPageResponse result = controller.allUsers(null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageInfo().getTotalElements()).isEqualTo(2);
    }

    @Test
    void allUsers_withFilters_passesThemToService() {
        UserFilterInput filter = UserFilterInput.builder().startDate("2025-01-01").build();
        PageInput page = PageInput.builder().page(0).size(5).build();
        when(userService.getAllUsers(filter, page)).thenReturn(pagedResponse);

        UserPageResponse result = controller.allUsers(filter, page);

        assertThat(result).isEqualTo(pagedResponse);
        verify(userService).getAllUsers(filter, page);
    }

    @Test
    void allUsers_invalidSortBy_propagatesBadRequest() {
        PageInput page = PageInput.builder().sortBy("password").build();
        when(userService.getAllUsers(null, page)).thenThrow(new BadRequestException("Invalid sortBy field: 'password'"));

        assertThatThrownBy(() -> controller.allUsers(null, page))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sortBy field");
    }

    @Test
    void allUsers_serviceThrowsInternalError_throwsBadGateway() {
        when(userService.getAllUsers(any(), any()))
                .thenThrow(new InternalServerErrorException("DB error", new RuntimeException()));

        assertThatThrownBy(() -> controller.allUsers(null, null))
                .isInstanceOf(BadGatewayException.class);
    }

    // --- userById ---

    @Test
    void userById_existing_returnsDTO() {
        UUID id = adminDTO.getId();
        when(userService.getUserById(id)).thenReturn(Optional.of(adminDTO));

        UserDTO result = controller.userById(id);

        assertThat(result.getUsername()).isEqualTo("admin1");
    }

    @Test
    void userById_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.userById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void userById_serviceThrowsInternalError_throwsBadGateway() {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new InternalServerErrorException("DB error", new RuntimeException()));

        assertThatThrownBy(() -> controller.userById(id))
                .isInstanceOf(BadGatewayException.class);
    }

    // --- createUser ---

    @Test
    void createUser_validInput_returnsCreatedUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1").email("admin@test.com").password("Pass@1234")
                .userType(UserType.ADMIN).adminLevel("SENIOR").build();
        when(userService.createUser(request)).thenReturn(adminDTO);

        UserDTO result = controller.createUser(request);

        assertThat(result.getUsername()).isEqualTo("admin1");
    }

    @Test
    void createUser_conflict_propagatesConflictException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("dup").email("dup@test.com").password("Pass@1234").userType(UserType.REGULAR).build();
        when(userService.createUser(request)).thenThrow(new ConflictException("Email already registered"));

        assertThatThrownBy(() -> controller.createUser(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createUser_serviceThrowsInternalError_throwsBadGateway() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1").email("user@test.com").password("Pass@1234").userType(UserType.REGULAR).build();
        when(userService.createUser(request)).thenThrow(new InternalServerErrorException("DB error", new RuntimeException()));

        assertThatThrownBy(() -> controller.createUser(request))
                .isInstanceOf(BadGatewayException.class);
    }

    // --- updateUser ---

    @Test
    void updateUser_existing_returnsUpdatedUser() {
        UUID id = adminDTO.getId();
        UpdateUserRequest request = UpdateUserRequest.builder().username("newAdmin").build();
        when(userService.updateUser(id, request)).thenReturn(Optional.of(adminDTO));

        UserDTO result = controller.updateUser(id, request);

        assertThat(result).isEqualTo(adminDTO);
    }

    @Test
    void updateUser_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateUser(id, new UpdateUserRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updateUser_serviceThrowsInternalError_throwsBadGateway() {
        UUID id = UUID.randomUUID();
        when(userService.updateUser(eq(id), any())).thenThrow(new InternalServerErrorException("DB error", new RuntimeException()));

        assertThatThrownBy(() -> controller.updateUser(id, new UpdateUserRequest()))
                .isInstanceOf(BadGatewayException.class);
    }

    // --- deleteUser ---

    @Test
    void deleteUser_existing_returnsTrue() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(true);

        Boolean result = controller.deleteUser(id);

        assertThat(result).isTrue();
    }

    @Test
    void deleteUser_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenReturn(false);

        assertThatThrownBy(() -> controller.deleteUser(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void deleteUser_serviceThrowsInternalError_throwsBadGateway() {
        UUID id = UUID.randomUUID();
        when(userService.deleteUser(id)).thenThrow(new InternalServerErrorException("DB error", new RuntimeException()));

        assertThatThrownBy(() -> controller.deleteUser(id))
                .isInstanceOf(BadGatewayException.class);
    }
}
