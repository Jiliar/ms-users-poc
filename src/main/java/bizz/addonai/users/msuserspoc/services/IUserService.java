package bizz.addonai.users.msuserspoc.services;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import java.util.UUID;
import java.util.List;

public interface IUserService {

    UserDTO createUser(CreateUserRequest request);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
    UserDTO updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
}
