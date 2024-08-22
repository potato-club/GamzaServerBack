package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.project.*;
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
        Page<ProjectEntity> projectPage = projectRepository.findByOrderByUpdatedDateDesc(pageable);

        if (projectPage.isEmpty()) {
            throw new ForbiddenException("프로젝트가 존재하지 않습니다.", ErrorCode.FAILED_PROJECT_ERROR);
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
                .contents(collect)
                .build();
    }

    @Override
    public ProjectListPerResponseDto personalProject(Pageable pageable, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new ForbiddenException("유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));


        Page<ProjectEntity> projects = projectRepository.findByLeaderOrderByUpdatedDateDesc(user, pageable);

        // ProjectEntity 리스트를 ProjectPerResponseDto 리스트로 변환
        List<ProjectPerResponseDto> projectDtos = projects.stream()
                .map(project -> new ProjectPerResponseDto(project.getName()))
                .collect(Collectors.toList());

        // ProjectListPerResponseDto 생성 및 반환
        return ProjectListPerResponseDto.builder()
                .size(projectDtos.size())
                .contents(projectDtos)
                .build();
    }

    @Override
    public void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new ForbiddenException("유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        ProjectEntity project = projectRepository.findById(id).orElseThrow(() ->
                new ForbiddenException("프로젝트를 찾을 수 없습니다.", ErrorCode.FAILED_PROJECT_ERROR));

        if (!project.getLeader().getId().equals(userId)) {
            throw new ForbiddenException("이 프로젝트를 수정할 권한이 없습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        project.updateProject(dto.getName(), dto.getDescription(), dto.getState(), dto.getStartedDate(), dto.getEndedDate());
        projectRepository.save(project);

    }
}

