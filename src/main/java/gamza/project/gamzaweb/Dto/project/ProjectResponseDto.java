package gamza.project.gamzaweb.Dto.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gamza.project.gamzaweb.Dto.User.response.ResponseCollaboratorDto;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {

    private Long id;
    private String name;
    private String description;
    private ProjectState state;
    private LocalDate startedDate;
    private LocalDate endedDate;
    private List<String> imageIds;
    private List<ResponseCollaboratorDto> collaborators;
    @JsonProperty("isCollaborator")
    private boolean isCollaborator;

    @JsonIgnore
    public boolean isIsCollaborator() {
        return isCollaborator;
    }
}
