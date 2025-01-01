package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.QuerydslRepository.ProjectCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long>, ProjectCustomRepository {

    List<ProjectEntity> findByOrderByUpdatedDateDesc();

    List<ProjectEntity> findByApproveStateTrueOrderByUpdatedDateDesc();

    Optional<ProjectEntity> findByIdAndApproveStateTrue(Long id);


    List<ProjectEntity> findByLeaderOrderByUpdatedDateDesc(UserEntity user);

    Page<ProjectEntity> findByApproveState(boolean approveState, Pageable pageable);

    Page<ProjectEntity> findByFixedStateAndApproveState(boolean fixedState, boolean approveState, Pageable pageable);

    Page<ProjectEntity> findByFixedStateAndApproveFixedState(boolean fixedState, boolean approveFixedState, Pageable pageable);

}
