package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Controller.DeploymentSseController;
import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
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
    private final ProjectRepository projectRepository;
    private final DeploymentSseController deploymentSseController;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDeploymentStep(ProjectEntity project, DeploymentStep step) {
        project.updateDeploymentStep(step.getDescription());
        projectRepository.saveAndFlush(project);

        deploymentSseController.sendUpdate(project.getId(), step.getDescription());

        System.out.println("Deployment Step Updated: " + step.getDescription());
    }
}
