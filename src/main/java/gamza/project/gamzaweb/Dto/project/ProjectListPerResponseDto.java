package gamza.project.gamzaweb.Dto.project;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ProjectListPerResponseDto {

    private List<ProjectPerResponseDto> waitProjects;
    private List<ProjectPerResponseDto> completeProjects;
}
