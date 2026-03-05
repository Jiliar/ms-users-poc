package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.models.UserEntity;

/**
 * Abstract Factory Interface
 * Define la interfaz para crear familias de objetos UserEntity relacionados
 */
public interface UserFactory {

    UserEntity createUser(CreateUserRequest request, String encryptedPassword);
    String getFactoryType();
    void validateUserData(CreateUserRequest request);

}