package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
