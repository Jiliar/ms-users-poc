package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.exceptions.ConflictException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.repositories.IUserRepository;
import bizz.addonai.users.msuserspoc.services.IUserService;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
        String encryptedPassword = passwordService.encryptPassword(request.getPassword());
        UserFactory factory = factoryProvider.getFactory(request.getUserType());
        UserEntity userEntity = factory.createUser(request, encryptedPassword);
        try {
            UserEntity saved = userRepository.save(userEntity);
            log.info("Usuario registrado exitosamente: {} (ID: {})", saved.getUsername(), saved.getId());
            return convertToDTO(saved);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error al guardar el usuario en la base de datos", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error inesperado al crear el usuario", e);
        }
    }

    private void validateUniqueConstraints(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken: " + request.getUsername());
        }
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        try {
            return userRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error al consultar usuarios en la base de datos", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID id) {
        try {
            return userRepository.findById(id).map(this::convertToDTO);
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error al consultar el usuario en la base de datos", e);
        }
    }

    public Optional<UserDTO> updateUser(UUID id, UpdateUserRequest request) {
        Optional<UserEntity> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        UserEntity userEntity = opt.get();

        if (request.getUsername() != null) userEntity.setUsername(request.getUsername());
        if (request.getEmail() != null) userEntity.setEmail(request.getEmail());

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

        try {
            return Optional.of(convertToDTO(userRepository.save(userEntity)));
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error al actualizar el usuario en la base de datos", e);
        }
    }

    public boolean deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        try {
            userRepository.deleteById(id);
            return true;
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error al eliminar el usuario en la base de datos", e);
        }
    }

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
