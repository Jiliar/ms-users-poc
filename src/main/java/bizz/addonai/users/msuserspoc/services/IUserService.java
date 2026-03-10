package bizz.addonai.users.msuserspoc.services;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;

import java.util.Optional;
import java.util.UUID;

public interface IUserService {

    UserDTO createUser(CreateUserRequest request);
    UserPageResponse getAllUsers(UserFilterInput filter, PageInput pageInput);
    Optional<UserDTO> getUserById(UUID id);
    Optional<UserDTO> updateUser(UUID id, UpdateUserRequest request);
    boolean deleteUser(UUID id);
}
