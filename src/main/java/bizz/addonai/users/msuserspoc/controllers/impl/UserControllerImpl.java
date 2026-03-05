package bizz.addonai.users.msuserspoc.controllers.impl;

import bizz.addonai.users.msuserspoc.controllers.IUserController;
import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    private final IUserService userService;

    // Queries
    @QueryMapping
    public List<UserDTO> allUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public UserDTO userById(@Argument UUID id) {
        return userService.getUserById(id);
    }

    // Mutations
    @MutationMapping
    public UserDTO createUser(@Argument CreateUserRequest input) {
        try {
            UserDTO user = userService.createUser(input);
            if (user == null) {
                throw new RuntimeException("No se pudo crear el usuario");
            }
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error al crear usuario: " + e.getMessage(), e);
        }
    }

    @MutationMapping
    public UserDTO updateUser(@Argument UUID id, @Argument UpdateUserRequest input) {
        return userService.updateUser(id, input);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument UUID id) {
        userService.deleteUser(id);
        return true;
    }
}
