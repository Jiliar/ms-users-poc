package bizz.addonai.users.msuserspoc.repositories;

import bizz.addonai.users.msuserspoc.models.UserEntity;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.userType = :userType")
    List<UserEntity> findByUserType(UserType userType);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}