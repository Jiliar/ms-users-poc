package bizz.addonai.users.msuserspoc.controllers;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;

import java.util.UUID;

public interface IUserController {

    @QueryMapping
    UserPageResponse allUsers(@Argument UserFilterInput filter, @Argument PageInput page);

    @QueryMapping
    UserDTO userById(@Argument UUID id);

    @MutationMapping
    UserDTO createUser(@Argument @Valid CreateUserRequest input);

    @MutationMapping
    UserDTO updateUser(@Argument UUID id, @Argument @Valid UpdateUserRequest input);

    @MutationMapping
    Boolean deleteUser(@Argument UUID id);
}
