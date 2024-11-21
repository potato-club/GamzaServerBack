package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByOrderByUpdatedDateDesc();

    List<ProjectEntity> findByApproveStateTrueOrderByUpdatedDateDesc();

    Optional<ProjectEntity> findByIdAndApproveStateTrue(Long id);


    List<ProjectEntity> findByLeaderOrderByUpdatedDateDesc(UserEntity user);

    Page<ProjectEntity> findByApproveState(boolean approveState, Pageable pageable);

}
