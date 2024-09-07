package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListNotApproveResponse {

    private Long id;
    private String userName;
    private String name;
    private String description;
    private ProjectState state;
//    private  // port 추가하면 되는데 일단 이어서 할떄는 project create에서 port를 받는데 application entity에 추가하도록 수정먼저하자

    public ProjectListNotApproveResponse(ProjectEntity project) {
        this.id = project.getId();
        this.userName = project.getLeader().getFamilyName() + project.getLeader().getGivenName();
        this.name = project.getName();
        this.description = project.getDescription();
        this.state = project.getState();
    }
}
