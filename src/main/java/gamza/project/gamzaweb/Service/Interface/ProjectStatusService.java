package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;

public interface ProjectStatusService {
    void sendDeploymentStep(ProjectEntity project, DeploymentStep step);
}
