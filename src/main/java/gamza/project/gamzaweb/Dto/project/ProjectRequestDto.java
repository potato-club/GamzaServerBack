package gamza.project.gamzaweb.Dto.project;

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
}
