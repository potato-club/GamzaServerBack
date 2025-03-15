package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;

public interface ProjectStatusService {
    void sendDeploymentStep(ProjectEntity project, DeploymentStep step);
}
