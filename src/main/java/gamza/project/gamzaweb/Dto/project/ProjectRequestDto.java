package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Dto.User.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.Dto.application.ApplicationRequestDto;
import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.CollaboratorEntity;
import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import gamza.project.gamzaweb.Entity.Enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDto {

    private Long platformId;
    private String platformName;
    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;
    private int outerPort;
    private String tag;
    private String variableKey;
    private List<Integer> collaborators = new ArrayList<>();
    private ProjectType projectType;
//    private List<RequestAddCollaboratorDto> collaborators = new ArrayList<>();

}
