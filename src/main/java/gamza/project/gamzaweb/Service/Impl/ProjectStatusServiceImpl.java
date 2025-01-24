package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import gamza.project.gamzaweb.Service.Interface.ProjectStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProjectStatusServiceImpl implements ProjectStatusService {
    private ProjectRepository projectRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDeploymentStep(ProjectEntity project, String step) {
        project.updateDeploymentStep(step);
        projectRepository.saveAndFlush(project);
        System.out.println("Deployment Step Updated: " + step);
    }
}
