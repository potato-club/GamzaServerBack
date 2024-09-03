package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Error.requestError.ForbiddenException;
import gamza.project.gamzaweb.Error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity user = userRepository.findById(userId).orElseThrow();

        try {
            ProjectEntity project = ProjectEntity.builder()
                    .startedDate(dto.getStartedDate())
                    .endedDate(dto.getEndedDate())
                    .leader(user)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .state(dto.getState())
                    .approveState(false)
                    .approveFixedState(true)
                    .build();

            projectRepository.save(project);
            Path tempDir = Files.createTempDirectory("dockerfile_project_" + project.getName());
            unzipAndSaveDockerfile(file, tempDir, project);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Fail Created Project (DockerFile Error)", ErrorCode.FAILED_PROJECT_ERROR);
        }
        // zip not null Erro
    }

    private void unzipAndSaveDockerfile(MultipartFile file, Path destDir, ProjectEntity project) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newPath = zipSlipProtect(zipEntry, destDir);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (zipEntry.getName().equalsIgnoreCase("Dockerfile")) {
                    project.updateDockerfilePath(newPath.toString());
                    projectRepository.save(project);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private Path zipSlipProtect(ZipEntry zipEntry, Path destDir) throws IOException {
        Path targetDirResolved = destDir.resolve(zipEntry.getName());
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(destDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
    }

    public ProjectListResponseDto getAllProject(Pageable pageable) {
        Page<ProjectEntity> projectPage = projectRepository.findByOrderByUpdatedDateDesc(pageable);

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
                .map(project -> new ProjectPerResponseDto(project.getName(), project.isApproveState()))
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

    @Override
    public Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);

        if(!userRole.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Page<ProjectEntity> projectEntities = projectRepository.findByApproveState(false, pageable);

        return projectEntities.map(ProjectListNotApproveResponse::new);

         // 이거 리스트 만들기 전에 프로젝트 승인해주는 api 먼저 만들기
    }

    @Override
    public void approveCreateProject(HttpServletRequest request, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);

        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(()-> new BadRequestException("4001 NOT FOUND PROJECT", ErrorCode.FAILED_PROJECT_ERROR));

        if(!userRole.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        if(project.isApproveState()) {
            throw new BadRequestException("4001 PROJECT ALREADY APPROVE", ErrorCode.FAILED_PROJECT_ERROR);
        }

        project.approveCreateProject();
        projectRepository.save(project);

        if(project.getZipPath() != null) {

        }
    }



}

