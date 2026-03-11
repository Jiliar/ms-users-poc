package bizz.addonai.users.msuserspoc.services.impl;

import bizz.addonai.users.msuserspoc.config.RedisCacheConfig;
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
import bizz.addonai.users.msuserspoc.repositories.jpa.IUserRepository;
import bizz.addonai.users.msuserspoc.repositories.security.IPasswordRepository;
import bizz.addonai.users.msuserspoc.repositories.jpa.specs.UserSpecification;
import bizz.addonai.users.msuserspoc.services.IUserService;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.users.IUserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final IPasswordRepository passwordService;

    @Override
    @CacheEvict(value = RedisCacheConfig.CACHE_USERS_PAGINATED, allEntries = true)
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Iniciando registro de usuario: {}", request.getUsername());
        log.debug("[createUser] Request recibido - userType={}, email={}", request.getUserType(), request.getEmail());
        validateUniqueConstraints(request);
        
        String encryptedPassword = passwordService.encryptPassword(request.getPassword());
        IUserFactory factory = factoryProvider.getFactory(request.getUserType());
        UserEntity userEntity = factory.createUser(request, encryptedPassword);
        
        try {
            UserEntity saved = userRepository.save(userEntity);
            log.info("Usuario registrado exitosamente: {} (ID: {})", saved.getUsername(), saved.getId());
            return convertToDTO(saved);
        } catch (DataAccessException e) {
            log.error("[createUser] Error de acceso a datos al guardar: {}", e.getMessage());
            throw new InternalServerErrorException("Error al guardar el usuario en la base de datos", e);
        } catch (Exception e) {
            log.error("[createUser] Error inesperado al guardar: {}", e.getMessage());
            throw new InternalServerErrorException("Error inesperado al crear el usuario", e);
        }
    }

    // Cacheamos los resultados paginados usando como llave los parámetros de búsqueda
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.CACHE_USERS_PAGINATED, key = "{#filter, #pageInput}")
    public UserPageResponse getAllUsers(UserFilterInput filter, PageInput pageInput) {
        log.debug("[getAllUsers] Consultando usuarios - filter={}, pageInput={}", filter, pageInput);
        PageRequest pageRequest = buildPageRequest(pageInput);
        Specification<UserEntity> spec = buildSpecification(filter);
        
        try {
            Page<UserEntity> page = userRepository.findAll(spec, pageRequest);
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
            log.error("[getAllUsers] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al consultar usuarios en la base de datos", e);
        }
    }

    // Cacheamos el usuario específico. Si el Optional está vacío, no se guarda en caché gracias a 'unless'
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.CACHE_USER_BY_ID, key = "#id", unless = "#result.isEmpty()")
    public Optional<UserDTO> getUserById(UUID id) {
        log.debug("[getUserById] Buscando usuario en DB con id={}", id);
        try {
            return userRepository.findById(id).map(this::convertToDTO);
        } catch (DataAccessException e) {
            log.error("[getUserById] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al consultar el usuario en la base de datos", e);
        }
    }

    // Al actualizar, invalidamos tanto el usuario específico como las listas paginadas
    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_USER_BY_ID, key = "#id"),
        @CacheEvict(value = RedisCacheConfig.CACHE_USER_BY_EMAIL, allEntries = true),
        @CacheEvict(value = RedisCacheConfig.CACHE_USERS_PAGINATED, allEntries = true)
    })
    public Optional<UserDTO> updateUser(UUID id, UpdateUserRequest request) {
        log.debug("[updateUser] Actualizando usuario id={}, campos: username={}, email={}", id, request.getUsername(), request.getEmail());
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
            UserDTO updated = convertToDTO(userRepository.save(userEntity));
            return Optional.of(updated);
        } catch (DataAccessException e) {
            log.error("[updateUser] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al actualizar el usuario en la base de datos", e);
        }
    }

    // Al eliminar, invalidamos todas las cachés asociadas a los usuarios
    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisCacheConfig.CACHE_USER_BY_ID, key = "#id"),
        @CacheEvict(value = RedisCacheConfig.CACHE_USER_BY_EMAIL, allEntries = true),
        @CacheEvict(value = RedisCacheConfig.CACHE_USERS_PAGINATED, allEntries = true)
    })
    public boolean deleteUser(UUID id) {
        log.debug("[deleteUser] Eliminando usuario id={}", id);
        if (!userRepository.existsById(id)) {
            return false;
        }
        try {
            userRepository.deleteById(id);
            return true;
        } catch (DataAccessException e) {
            log.error("[deleteUser] Error de acceso a datos: {}", e.getMessage());
            throw new InternalServerErrorException("Error al eliminar el usuario en la base de datos", e);
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
        if (pageInput != null && "ASC".equalsIgnoreCase(pageInput.getSortDirection().name())) {
            direction = Sort.Direction.ASC;
        }

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private Specification<UserEntity> buildSpecification(UserFilterInput filter) {
        if (filter == null) return UserSpecification.withFilters(null);
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