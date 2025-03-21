package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.controller.DeploymentSseController;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.service.Interface.ProjectStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectStatusServiceImpl implements ProjectStatusService {
    private final ProjectRepository projectRepository;
    private final DeploymentSseController deploymentSseController;

    @Override
    public void sendDeploymentStep(ProjectEntity project, DeploymentStep step) {
        // 배포 상태를 SSE로 전송
        deploymentSseController.sendUpdate(project.getId(), step.getDescription());
        System.out.println("Deployment Step Updated (SSE Only): " + step.getDescription());
    }
}
