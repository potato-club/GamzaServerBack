package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String number);

    UserEntity findUserEntityById(Long id);

    List<UserEntity> findAllByOrderByFamilyNameAsc();

    Page<UserEntity> findByUserRole(UserRole user, Pageable pageable);

}
