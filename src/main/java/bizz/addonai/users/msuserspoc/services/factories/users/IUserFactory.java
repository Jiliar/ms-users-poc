package bizz.addonai.users.msuserspoc.services.factories.users;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.UserEntity;

/**
 * Abstract Factory Interface
 * Define la interfaz para crear familias de objetos UserEntity relacionados
 */
public interface IUserFactory {

    UserEntity createUser(CreateUserRequest request, String encryptedPassword);
    String getFactoryType();
    void validateUserData(CreateUserRequest request);

}