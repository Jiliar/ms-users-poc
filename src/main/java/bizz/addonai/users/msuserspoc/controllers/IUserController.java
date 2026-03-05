package bizz.addonai.users.msuserspoc.controllers;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import org.springframework.graphql.data.method.annotation.Argument;

import java.util.List;
import java.util.UUID;

public interface IUserController {

    List<UserDTO> allUsers();
    UserDTO userById(@Argument UUID id);
    UserDTO createUser(@Argument CreateUserRequest input);
    UserDTO updateUser(@Argument UUID id, @Argument UpdateUserRequest input);
    Boolean deleteUser(@Argument UUID id);

}
