package gamza.project.gamzaweb.Validate;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.NotFoundException;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProjectValidate {

    private final ProjectRepository projectRepository;

    public ProjectEntity validateProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found", ErrorCode.NOT_FOUND_EXCEPTION));
    }
}
