package bizz.addonai.users.msuserspoc.controllers.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.exceptions.UserNotFoundException;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerImplTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserControllerImpl controller;

    private UserDTO adminDTO;
    private UserDTO regularDTO;

    @BeforeEach
    void setUp() {
        adminDTO = UserDTO.builder()
                .id(UUID.randomUUID())
                .username("admin1")
                .email("admin@test.com")
                .userType(UserType.ADMIN)
                .permissions("ALL:READ,WRITE,DELETE,MANAGE_USERS,MANAGE_SYSTEM")
                .dashboardUrl("/admin/dashboard")
                .createdAt(LocalDateTime.now())
                .adminLevel("SENIOR")
                .department("IT")
                .build();

        regularDTO = UserDTO.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .email("user@test.com")
                .userType(UserType.REGULAR)
                .permissions("LIMITED:READ,WRITE")
                .dashboardUrl("/user/dashboard")
                .createdAt(LocalDateTime.now())
                .subscriptionType("FREE")
                .newsletterSubscribed(false)
                .build();
    }

    // --- allUsers ---

    @Test
    void allUsers_returnsListFromService() {
        when(userService.getAllUsers()).thenReturn(List.of(adminDTO, regularDTO));

        List<UserDTO> result = controller.allUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("admin1");
        assertThat(result.get(1).getUsername()).isEqualTo("user1");
        verify(userService).getAllUsers();
    }

    @Test
    void allUsers_emptyList_returnsEmpty() {
        when(userService.getAllUsers()).thenReturn(List.of());

        List<UserDTO> result = controller.allUsers();

        assertThat(result).isEmpty();
    }

    // --- userById ---

    @Test
    void userById_existing_returnsDTO() {
        UUID id = adminDTO.getId();
        when(userService.getUserById(id)).thenReturn(adminDTO);

        UserDTO result = controller.userById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsername()).isEqualTo("admin1");
        verify(userService).getUserById(id);
    }

    @Test
    void userById_notFound_propagatesException() {
        UUID id = UUID.randomUUID();
        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("not found"));

        assertThatThrownBy(() -> controller.userById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- createUser ---

    @Test
    void createUser_validInput_returnsCreatedUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("admin1")
                .email("admin@test.com")
                .password("Pass@1234")
                .userType("ADMIN")
                .adminLevel("SENIOR")
                .build();

        when(userService.createUser(request)).thenReturn(adminDTO);

        UserDTO result = controller.createUser(request);

        assertThat(result.getUsername()).isEqualTo("admin1");
        verify(userService).createUser(request);
    }

    @Test
    void createUser_serviceReturnsNull_throwsRuntimeException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("user1")
                .email("user@test.com")
                .password("Pass@1234")
                .userType("REGULAR")
                .build();

        when(userService.createUser(request)).thenReturn(null);

        assertThatThrownBy(() -> controller.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo crear el usuario");
    }

    @Test
    void createUser_serviceThrowsException_wrapsInRuntimeException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("dup")
                .email("dup@test.com")
                .password("Pass@1234")
                .userType("REGULAR")
                .build();

        when(userService.createUser(request))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        assertThatThrownBy(() -> controller.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error al crear usuario");
    }

    // --- updateUser ---

    @Test
    void updateUser_validInput_returnsUpdatedUser() {
        UUID id = adminDTO.getId();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("newAdmin")
                .adminLevel("SUPER")
                .build();

        when(userService.updateUser(id, request)).thenReturn(adminDTO);

        UserDTO result = controller.updateUser(id, request);

        assertThat(result).isEqualTo(adminDTO);
        verify(userService).updateUser(id, request);
    }

    @Test
    void updateUser_notFound_propagatesException() {
        UUID id = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();

        when(userService.updateUser(eq(id), any())).thenThrow(new UserNotFoundException("not found"));

        assertThatThrownBy(() -> controller.updateUser(id, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- deleteUser ---

    @Test
    void deleteUser_existing_returnsTrue() {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).deleteUser(id);

        Boolean result = controller.deleteUser(id);

        assertThat(result).isTrue();
        verify(userService).deleteUser(id);
    }

    @Test
    void deleteUser_notFound_propagatesException() {
        UUID id = UUID.randomUUID();
        doThrow(new UserNotFoundException("not found")).when(userService).deleteUser(id);

        assertThatThrownBy(() -> controller.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class);
    }
}
