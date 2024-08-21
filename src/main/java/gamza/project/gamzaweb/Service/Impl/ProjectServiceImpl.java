package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.project.ProjectListResponseDto;
import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import gamza.project.gamzaweb.Dto.project.ProjectResponseDto;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.ForbiddenException;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void createProject(HttpServletRequest request, ProjectRequestDto dto) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity user = userRepository.findById(userId).orElseThrow();

        ProjectEntity project = ProjectEntity.builder()
                .startedDate(dto.getStartedDate())
                .endedDate(dto.getEndedDate())
                .leader(user)
                .name(dto.getName())
                .description(dto.getDescription())
                .state(dto.getState())
                .build();

        projectRepository.save(project);

    }

    public ProjectListResponseDto getAllProject(Pageable pageable) {
        // 프로젝트를 페이지네이션하여 조회
        Page<ProjectEntity> projectPage = projectRepository.findByOrderByUpdatedDateDesc(pageable);

        // 프로젝트가 없을 경우 예외 처리
        if (projectPage.isEmpty()) {
            throw new ForbiddenException("Project doesn't exist", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        List<ProjectResponseDto> collect = projectPage.getContent().stream()
                .map(project -> new ProjectResponseDto(
                        project.getName(),
                        project.getDescription(),
                        project.getState(),
                        project.getStartedDate(),
                        project.getEndedDate()
                ))
                .collect(Collectors.toList());

        // ProjectListResponseDto 반환
        return ProjectListResponseDto.builder()
                .size(collect.size())
                .contents(collect)  // 'projects' 대신 'content'로 수정
                .build();
    }
}

