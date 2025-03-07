package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Dto.User.response.ResponseCollaboratorDto;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.Enums.ProjectType;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponseDto {
    private Long id;
    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;
    private List<ResponseCollaboratorDto> collaborators;
    private ProjectType projectType;
    private Long platformId;
    private String platformName;

    public ProjectDetailResponseDto(ProjectEntity project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.state = project.getState();
        this.startedDate = project.getStartedDate();
        this.endedDate = project.getEndedDate();
        this.collaborators = project.getCollaborators().stream()
                .map(collaborator -> ResponseCollaboratorDto.builder()
                        .id(collaborator.getUser().getId())    // User PK 값
                        .name(collaborator.getUser().getFamilyName() + collaborator.getUser().getGivenName()) // User 이름
                        .studentId(collaborator.getUser().getStudentId())
                        .build())
                .toList();
        this.projectType = project.getProjectType();
        this.platformId = project.getPlatformEntity().getId();
        this.platformName = project.getPlatformEntity().getPlatformName();
    }
}
