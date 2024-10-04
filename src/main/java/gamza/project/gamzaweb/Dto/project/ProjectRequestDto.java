package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Dto.application.ApplicationRequestDto;
import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestDto {

    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;

    private String applicationName;
    private int outerPort;
    private String tag;
    private String variableKey;
    private ApplicationType applicationType;
}
