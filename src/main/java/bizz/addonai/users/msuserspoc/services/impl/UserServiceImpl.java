package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.dtos.CreateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.PageInput;
import bizz.addonai.users.msuserspoc.dtos.PageMetadata;
import bizz.addonai.users.msuserspoc.dtos.UpdateUserRequest;
import bizz.addonai.users.msuserspoc.dtos.UserDTO;
import bizz.addonai.users.msuserspoc.dtos.UserFilterInput;
import bizz.addonai.users.msuserspoc.dtos.UserPageResponse;
import bizz.addonai.users.msuserspoc.exceptions.BadRequestException;
import bizz.addonai.users.msuserspoc.exceptions.ConflictException;
import bizz.addonai.users.msuserspoc.exceptions.InternalServerErrorException;
import bizz.addonai.users.msuserspoc.models.AdminUser;
import bizz.addonai.users.msuserspoc.models.RegularUser;
import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.repositories.IUserRepository;
import bizz.addonai.users.msuserspoc.repositories.specifications.UserSpecification;
import bizz.addonai.users.msuserspoc.services.IUserService;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements IUserService {

    private static final int DEFAULT_PAGE      = 0;
    private static final int DEFAULT_SIZE      = 10;
    private static final int MAX_SIZE          = 100;
    private static final String DEFAULT_SORT   = "createdAt";
    private static final Set<String> SORTABLE_FIELDS = Set.of("createdAt", "updatedAt", "username", "email");

    private final IUserRepository userRepository;
    private final IUserFactoryProvider factoryProvider;
    private final PasswordService passwordService;

    public UserDTO createUser(CreateUserRequest request) {
        log.info("Iniciando registro de usuario: {}", request.getUsername());
        log.debug("[createUser] Request recibido - userType={}, email={}", request.getUserType(), request.getEmail());
        validateUniqueConstraints(request);
        log.debug("[createUser] Validaciones de unicidad superadas, cifrando contraseña");
        String encryptedPassword = passwordService.encryptPassword(request.getPassword());
        log.debug("[createUser] Contraseña cifrada, obteniendo factory para tipo={}", request.getUserType());
        UserFactory factory = factoryProvider.getFactory(request.getUserType().name());
        log.debug("[createUser] Factory obtenida: {}", factory.getClass().getSimpleName());
        UserEntity userEntity = factory.createUser(request, encryptedPassword);
        log.debug("[createUser] Entidad construida, persistiendo en base de datos");
        try {
            UserEntity saved = userRepository.save(userEntity);
            log.info("Usuario registrado exitosamente: {} (ID: {})", saved.getUsername(), saved.getId());
            log.debug("[createUser] Usuario persistido con ID={}", saved.getId());
            return convertToDTO(saved);
        } catch (DataAccessException e) {
            log.debug("[createUser] Error de acceso a datos al guardar: {}", e.getMessage());
            throw new InternalServerErrorException("Error al guardar el usuario en la base de datos", e);
        } catch (Exception e) {
            log.debug("[createUser] Error inesperado al guardar: {}", e.getMessage());
            throw new InternalServerErrorException("Error inesperado al crear el usuario", e);
        }
    }

    @Transactional(readOnly = true)
    public UserPageResponse getAllUsers(UserFilterInput filter, PageInput pageInput) {
        log.debug("[getAllUsers] Consultando usuarios - filter={}, pageInput={}", filter, pageInput);
        PageRequest pageRequest = buildPageRequest(pageInput);
        log.debug("[getAllUsers] PageRequest construido - page={}, size={}", pageRequest.getPageNumber(), pageRequest.getPageSize());
        Specification<UserEntity> spec = buildSpecification(filter);
        try {
            Page<UserEntity> page = userRepository.findAll(spec, pageRequest);
            log.debug("[getAllUsers] Resultado: totalElements={}, totalPages={}", page.getTotalElements(), page.getTotalPages());
            List<UserDTO> content = page.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return UserPageResponse.builder()
                    .content(content)
                    .pageInfo(PageMetadata.builder()
                            .page(page.getNumber())
                            .size(page.getSize())
                            .totalElements(page.getTotalElements())
                            .totalPages(page.getTotalPages())
                            .hasNext(page.hasNext())
                            .hasPrevious(page.hasPrevious())
                            .build())
                    .build();
        } catch (DataAccessException e) {
            log.debug("[getAllUsers] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al consultar usuarios en la base de datos", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(UUID id) {
        log.debug("[getUserById] Buscando usuario con id={}", id);
        try {
            Optional<UserDTO> result = userRepository.findById(id).map(this::convertToDTO);
            log.debug("[getUserById] Resultado: {}", result.isPresent() ? "encontrado" : "no encontrado");
            return result;
        } catch (DataAccessException e) {
            log.debug("[getUserById] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al consultar el usuario en la base de datos", e);
        }
    }

    public Optional<UserDTO> updateUser(UUID id, UpdateUserRequest request) {
        log.debug("[updateUser] Actualizando usuario id={}, campos: username={}, email={}", id, request.getUsername(), request.getEmail());
        Optional<UserEntity> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            log.debug("[updateUser] Usuario id={} no encontrado", id);
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

        log.debug("[updateUser] Aplicando cambios y persistiendo usuario id={}", id);
        try {
            UserDTO updated = convertToDTO(userRepository.save(userEntity));
            log.debug("[updateUser] Usuario id={} actualizado exitosamente", id);
            return Optional.of(updated);
        } catch (DataAccessException e) {
            log.debug("[updateUser] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al actualizar el usuario en la base de datos", e);
        }
    }

    public boolean deleteUser(UUID id) {
        log.debug("[deleteUser] Eliminando usuario id={}", id);
        if (!userRepository.existsById(id)) {
            log.debug("[deleteUser] Usuario id={} no existe", id);
            return false;
        }
        try {
            userRepository.deleteById(id);
            log.debug("[deleteUser] Usuario id={} eliminado exitosamente", id);
            return true;
        } catch (DataAccessException e) {
            log.debug("[deleteUser] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al eliminar el usuario en la base de datos", e);
        }
    }

    // ==========================================
    // Private helpers
    // ==========================================

    private void validateUniqueConstraints(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already taken: " + request.getUsername());
        }
    }

    private PageRequest buildPageRequest(PageInput pageInput) {
        int page = (pageInput != null && pageInput.getPage() != null)
                ? Math.max(0, pageInput.getPage())
                : DEFAULT_PAGE;

        int size = (pageInput != null && pageInput.getSize() != null)
                ? Math.min(MAX_SIZE, Math.max(1, pageInput.getSize()))
                : DEFAULT_SIZE;

        String sortBy = (pageInput != null && pageInput.getSortBy() != null)
                ? pageInput.getSortBy()
                : DEFAULT_SORT;

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sortBy field: '" + sortBy + "'. Allowed values: " + SORTABLE_FIELDS);
        }

        Sort.Direction direction = Sort.Direction.DESC;
        if (pageInput != null && "ASC".equalsIgnoreCase(pageInput.getSortDirection())) {
            direction = Sort.Direction.ASC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private Specification<UserEntity> buildSpecification(UserFilterInput filter) {
        if (filter == null) return UserSpecification.withFilters(null);
        // Eager date validation — the Specification lambda is lazy, so we validate before handing it to JPA
        validateDateField(filter.getStartDate(), "startDate");
        validateDateField(filter.getEndDate(), "endDate");
        return UserSpecification.withFilters(filter);
    }

    private void validateDateField(String date, String fieldName) {
        if (date == null) return;
        try {
            java.time.LocalDate.parse(date);
        } catch (java.time.format.DateTimeParseException e) {
            throw new BadRequestException(
                    "Invalid date format for " + fieldName + ": '" + date + "'. Expected yyyy-MM-dd");
        }
    }

    private UserDTO convertToDTO(UserEntity userEntity) {
        UserType resolvedType = userEntity.getUserType();
        if (resolvedType == null) {
            resolvedType = userEntity instanceof AdminUser ? UserType.ADMIN : UserType.REGULAR;
        }

        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .userType(resolvedType)
                .permissions(userEntity.getPermissions())
                .dashboardUrl(userEntity.getDashboardUrl())
                .createdAt(userEntity.getCreatedAt() != null ? userEntity.getCreatedAt().toString() : null);

        if (userEntity instanceof AdminUser admin) {
            builder.adminLevel(admin.getAdminLevel())
                    .department(admin.getDepartment())
                    .subscriptionType(null)
                    .newsletterSubscribed(false);
        } else if (userEntity instanceof RegularUser regular) {
            builder.subscriptionType(regular.getSubscriptionType())
                    .newsletterSubscribed(regular.isNewsletterSubscribed());
        }

        return builder.build();
    }
}
