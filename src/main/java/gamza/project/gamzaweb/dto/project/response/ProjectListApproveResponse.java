package gamza.project.gamzaweb.dto.project.response;

import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListApproveResponse {
    private Long id;
    private String userName;
    private String name;
    private String description;
    private ProjectState state;
    private String fileUrl;
    private int port;
    private ApprovalProjectStatus status;
    private String deploymentStep;

    public ProjectListApproveResponse(ProjectEntity project, String fileUrl) {
        this.id = project.getId();
        this.userName = project.getLeader().getFamilyName() + project.getLeader().getGivenName();
        this.name = project.getName();
        this.description = project.getDescription();
        this.state = project.getState();
        this.fileUrl = fileUrl;
        this.port = project.getApplication().getOuterPort();
        this.status = project.getApprovalProjectStatus();
        this.deploymentStep = project.getDeploymentStep();
    }
}
