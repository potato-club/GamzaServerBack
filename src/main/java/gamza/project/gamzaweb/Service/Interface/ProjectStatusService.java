package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;

public interface ProjectStatusService {
    void updateDeploymentStep(ProjectEntity project, DeploymentStep step);
}
