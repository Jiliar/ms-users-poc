package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.exceptions.UserNotFoundException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.repositories.IUserRepository;
import bizz.addonai.users.msuserspoc.services.IUserService;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IUserFactoryProvider factoryProvider;
    private final PasswordService passwordService;

    public UserDTO createUser(CreateUserRequest request) {
        log.info("Iniciando registro de usuario: {}", request.getUsername());
        validateUniqueConstraints(request);
        log.debug("Encriptando contraseña...");
        String encryptedPassword = passwordService.encryptPassword(request.getPassword());
        UserFactory factory = factoryProvider.getFactory(request.getUserType());
        UserEntity userEntity = factory.createUser(request, encryptedPassword);
        UserEntity savedUserEntity = userRepository.save(userEntity);
        log.info("Usuario registrado exitosamente: {} (ID: {})",
                savedUserEntity.getUsername(), savedUserEntity.getId());
        return convertToDTO(savedUserEntity);
    }

    private void validateUniqueConstraints(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
    }

    public boolean authenticate(String email, String plainPassword) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("UserEntity not found"));
        boolean valid = passwordService.verifyPassword(plainPassword, userEntity.getPassword());
        if (!valid) {
            log.warn("Failed authentication attempt for: {}", email);
        }

        return valid;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("UserEntity not found with id: " + id));
        return convertToDTO(userEntity);
    }

    public UserDTO updateUser(UUID id, UpdateUserRequest request) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("UserEntity not found with id: " + id));

        if (request.getUsername() != null) {
            userEntity.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            userEntity.setEmail(request.getEmail());
        }

        // Actualizar campos específicos según el tipo
        if (userEntity instanceof AdminUser admin && request.getAdminLevel() != null) {
            admin.setAdminLevel(request.getAdminLevel());
        }
        if (userEntity instanceof AdminUser admin && request.getDepartment() != null) {
            admin.setDepartment(request.getDepartment());
        }
        if (userEntity instanceof RegularUser regular && request.getSubscriptionType() != null) {
            regular.setSubscriptionType(request.getSubscriptionType());
        }
        if (userEntity instanceof RegularUser regular && request.getNewsletterSubscribed() != null) {
            regular.setNewsletterSubscribed(request.getNewsletterSubscribed());
        }

        UserEntity updatedUserEntity = userRepository.save(userEntity);
        return convertToDTO(updatedUserEntity);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("UserEntity not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Conversión a DTO con datos específicos según el tipo de usuario
     */
    private UserDTO convertToDTO(UserEntity userEntity) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .userType(userEntity.getUserType())
                .permissions(userEntity.getPermissions())
                .dashboardUrl(userEntity.getDashboardUrl())
                .createdAt(userEntity.getCreatedAt());

        if (userEntity instanceof AdminUser admin) {
            builder.adminLevel(admin.getAdminLevel())
                    .department(admin.getDepartment());
        } else if (userEntity instanceof RegularUser regular) {
            builder.subscriptionType(regular.getSubscriptionType())
                    .newsletterSubscribed(regular.isNewsletterSubscribed());
        }

        return builder.build();
    }
}
