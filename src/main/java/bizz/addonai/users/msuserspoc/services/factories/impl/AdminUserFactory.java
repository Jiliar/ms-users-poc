package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AdminUserFactory implements UserFactory {

    @Override
    public UserEntity createUser(CreateUserRequest request, String encryptedPassword) {
        validateUserData(request);

        return AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .adminLevel(request.getAdminLevel() != null ? request.getAdminLevel() : "STANDARD")
                .department(request.getDepartment() != null ? request.getDepartment() : "GENERAL")
                .build();
    }

    @Override
    public String getFactoryType() {
        return "ADMIN";
    }

    @Override
    public void validateUserData(CreateUserRequest request) {
        if (request.getAdminLevel() == null || request.getAdminLevel().isEmpty()) {
            throw new InvalidUserTypeException("Admin level is required for admin users");
        }
    }
}
