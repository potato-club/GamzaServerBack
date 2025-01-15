package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequestDto {

    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;
    private List<Integer> collaborators = new ArrayList<>();

}
