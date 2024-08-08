package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {

    @Query("SELECT c.containerId FROM ContainerEntity c WHERE c.user.id = :userId")
    List<String> findContainerIdsByUserId(@Param("userId") Long userId);

    Optional<ContainerEntity> findByContainerIdAndUser(String containerId, UserEntity user);
}
