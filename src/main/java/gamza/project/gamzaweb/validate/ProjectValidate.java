package gamza.project.gamzaweb.validate;

import gamza.project.gamzaweb.Entity.PlatformEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;
import gamza.project.gamzaweb.error.requestError.NotFoundException;
import gamza.project.gamzaweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProjectValidate {

    private final ProjectRepository projectRepository;

    public ProjectEntity validateProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    public void platformChecker(PlatformEntity platform, String projectType) {
        if (platform.getProjects().size() >= 3) {
            throw new BadRequestException("하나의 플랫폼에는 4개 이상의 프로젝트를 가질 수 없습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

        boolean hasFront = false;
        boolean hasBack = false;

        List<ProjectEntity> projectEntities = platform.getProjects();

        for(ProjectEntity project : projectEntities) {
            if(project.getProjectType().name().equals("FRONT")) {
                hasFront = true;
            }
            if(project.getProjectType().name().equals("BACK")) {
                hasBack = true;
            }
        }
        if(hasFront) {
            throw new BadRequestException("이미 FRONT프로젝트가 존재합니다 WAIT상태로 변경후 다시 시도해주세요.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
        if(hasBack) {
            throw new BadRequestException("이미 BACK프로젝트가 존재합니다 WAIT상태로 변경후 다시 시도해주세요.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }
}
