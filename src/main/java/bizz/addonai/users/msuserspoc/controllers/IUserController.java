package bizz.addonai.users.msuserspoc.controllers;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import org.springframework.graphql.data.method.annotation.Argument;

import java.util.UUID;

public interface IUserController {

    UserPageResponse allUsers(@Argument UserFilterInput filter, @Argument PageInput page);
    UserDTO userById(@Argument UUID id);
    UserDTO createUser(@Argument CreateUserRequest input);
    UserDTO updateUser(@Argument UUID id, @Argument UpdateUserRequest input);
    Boolean deleteUser(@Argument UUID id);
}
