package gamza.project.gamzaweb.QuerydslRepository;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCustomRepository {
//    @Query("SELECT p FROM ProjectEntity p " +
//            "WHERE p.approveState = true " +
//            "AND (p.fixedState = false OR (p.fixedState = true AND p.approveFixedState = true))")
//    List<ProjectEntity> findApprovedProjectsWithImages();

    List<ProjectEntity> findProjectsWithImages();
}
