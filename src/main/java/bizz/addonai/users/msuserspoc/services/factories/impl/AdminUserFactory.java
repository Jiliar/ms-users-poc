package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminUserFactory implements UserFactory {

    @Override
    public UserEntity createUser(CreateUserRequest request, String encryptedPassword) {
        log.debug("[AdminUserFactory] createUser invocado - username={}, email={}, adminLevel={}, department={}",
                request.getUsername(), request.getEmail(), request.getAdminLevel(), request.getDepartment());
        validateUserData(request);

        AdminUser user = AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .userType(UserType.ADMIN)
                .adminLevel(request.getAdminLevel() != null ? request.getAdminLevel() : "STANDARD")
                .department(request.getDepartment() != null ? request.getDepartment() : "GENERAL")
                .build();

        log.debug("[AdminUserFactory] AdminUser construido - adminLevel={}, department={}",
                user.getAdminLevel(), user.getDepartment());
        return user;
    }

    @Override
    public String getFactoryType() {
        return "ADMIN";
    }

    @Override
    public void validateUserData(CreateUserRequest request) {
        log.debug("[AdminUserFactory] validateUserData - adminLevel='{}'", request.getAdminLevel());
        if (request.getAdminLevel() == null || request.getAdminLevel().isEmpty()) {
            log.debug("[AdminUserFactory] Validacion fallida: adminLevel es nulo o vacio");
            throw new InvalidUserTypeException("Admin level is required for admin users");
        }
        log.debug("[AdminUserFactory] Validacion exitosa");
    }
}
