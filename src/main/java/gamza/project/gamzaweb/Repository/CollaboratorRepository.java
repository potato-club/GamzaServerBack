package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.CollaboratorEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<CollaboratorEntity, Long> {

    Optional<CollaboratorEntity> findByProjectIdAndUserId(Long project_id, Long user_id);

    void deleteAllByProject(ProjectEntity project);

}
