package gamza.project.gamzaweb.repository;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.ContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {

    @Query("SELECT c.containerId FROM ContainerEntity c WHERE c.user.id = :userId")
    List<String> findContainerIdsByUserId(@Param("userId") Long userId);

    ContainerEntity findContainerEntityByApplication(ApplicationEntity application);
}
