package bizz.addonai.users.msuserspoc.controllers.impl;

import bizz.addonai.users.msuserspoc.controllers.IUserController;
import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.exceptions.BadGatewayException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.exceptions.NotFoundException;
import bizz.addonai.users.msuserspoc.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Controller
@Validated
@RequiredArgsConstructor
public class UserControllerImpl implements IUserController {

    private final IUserService userService;

    @QueryMapping
    public List<UserDTO> allUsers() {
        try {
            return userService.getAllUsers();
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO userById(@Argument UUID id) {
        try {
            return userService.getUserById(id)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public UserDTO createUser(@Argument @Valid CreateUserRequest input) {
        try {
            return userService.createUser(input);
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public UserDTO updateUser(@Argument UUID id, @Argument @Valid UpdateUserRequest input) {
        try {
            return userService.updateUser(id, input)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public Boolean deleteUser(@Argument UUID id) {
        try {
            if (!userService.deleteUser(id)) {
                throw new NotFoundException("User not found with id: " + id);
            }
            return true;
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }
}
