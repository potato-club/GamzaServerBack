package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
