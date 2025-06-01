package gamza.project.gamzaweb.utils.validate;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.PlatformEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.BadRequestException;
import gamza.project.gamzaweb.utils.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProjectValidate {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public void isParticipateInProject(Long projectId, Long userId) {

        // 어드민이거나 해당 프로젝트 협력자일경우 삭제 가능

        UserEntity user = userRepository.findById(userId) // 해당 유저는 어드민이거나 해당 프로젝트 협력자
                .orElseThrow(() -> new UnAuthorizedException("해당 유저가 존재하지 않습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        if (!projectRepository.isUserCollaborator(projectId, userId)) {
            if(!user.getUserRole().equals(UserRole.ADMIN)) {
                throw new UnAuthorizedException("해당 프로젝트를 삭제할 권한이 존재하지 않습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
            }
        }
    }

    public void isProjectFixedState (ProjectEntity project) {
        if (!project.isFixedState()) {
            throw new BadRequestException("해당 프로젝트는 수정 요청 상태가 아닙니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    public void platformChecker(PlatformEntity platform, String projectType) {
        if (platform.getProjects().size() >= 3) {
            throw new BadRequestException("하나의 플랫폼에는 4개 이상의 프로젝트를 가질 수 없습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

        boolean hasFront = false;
        boolean hasBack = false;

        List<ProjectEntity> projectEntities = platform.getProjects();

        for (ProjectEntity project : projectEntities) {
            if (project.getProjectType().name().equals("FRONT")) {
                hasFront = true;
            }
            if (project.getProjectType().name().equals("BACK")) {
                hasBack = true;
            }
        }
        if (hasFront) {
            throw new BadRequestException("이미 FRONT프로젝트가 존재합니다 WAIT상태로 변경후 다시 시도해주세요.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
        if (hasBack) {
            throw new BadRequestException("이미 BACK프로젝트가 존재합니다 WAIT상태로 변경후 다시 시도해주세요.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }
}
