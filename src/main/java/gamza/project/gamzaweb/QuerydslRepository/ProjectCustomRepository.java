package gamza.project.gamzaweb.QuerydslRepository;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCustomRepository {
    List<ProjectEntity> findProjectsWithImages();

}
