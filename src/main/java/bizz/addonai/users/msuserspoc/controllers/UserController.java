package bizz.addonai.users.msuserspoc.controllers;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import bizz.addonai.users.msuserspoc.exceptions.BadGatewayException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.exceptions.NotFoundException;
import bizz.addonai.users.msuserspoc.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Controller
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final IUserService userService;

    @QueryMapping
    public UserPageResponse allUsers(@Argument("filter") UserFilterInput filter, @Argument("page") PageInput page) {
        try {
            return userService.getAllUsers(filter, page);
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO userById(@Argument("id") UUID id) {
        try {
            return userService.getUserById(id)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public UserDTO createUser(@Argument("input") @Valid CreateUserRequest input) {
        log.info("Creating user with username: {}", input.getUsername());
        try {
            return userService.createUser(input);
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public UserDTO updateUser(@Argument("id") UUID id, @Argument("input") @Valid UpdateUserRequest input) {
        try {
            return userService.updateUser(id, input)
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        } catch (InternalServerErrorException e) {
            throw new BadGatewayException(e.getMessage(), e);
        }
    }

    @MutationMapping
    public Boolean deleteUser(@Argument("id") UUID id) {
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