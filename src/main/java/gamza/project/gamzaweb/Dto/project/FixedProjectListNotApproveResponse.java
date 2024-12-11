package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.ProjectEntity;

public class FixedProjectListNotApproveResponse {
    private Long id;
    private String userName;
    private String name;
    private String description;
    private ProjectState state;

    public FixedProjectListNotApproveResponse(ProjectEntity project) {
        this.id = project.getId();
        this.userName = project.getLeader().getFamilyName() + project.getLeader().getGivenName();
        this.name = project.getName();
        this.description = project.getDescription();
        this.state = project.getState();
    }
}
