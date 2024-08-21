package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
}
