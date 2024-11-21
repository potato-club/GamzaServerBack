package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {

    private Long id;
    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;

//    public ProjectResponseDto(ProjectEntity project) {
//        this.id = project.getId();
//        this.name = project.getName();
//        this.description = project.getDescription();
//        this.state = project.getState();
//        this.startedDate = project.getStartedDate();
//        this.endedDate = project.getEndedDate();
//    }
}
