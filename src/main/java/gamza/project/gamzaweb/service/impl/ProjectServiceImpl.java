package gamza.project.gamzaweb.service.impl;

import com.github.dockerjava.api.DockerClient;
import gamza.project.gamzaweb.dto.user.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.dto.user.response.ResponseCollaboratorDto;
import gamza.project.gamzaweb.dto.project.*;
import gamza.project.gamzaweb.Entity.*;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.Enums.ProjectType;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.*;
import gamza.project.gamzaweb.repository.*;
import gamza.project.gamzaweb.service.Interface.NginxService;
import gamza.project.gamzaweb.service.Interface.PlatformService;
import gamza.project.gamzaweb.service.Interface.ProjectService;
import gamza.project.gamzaweb.service.Interface.ProjectStatusService;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;

import gamza.project.gamzaweb.utils.JpaAssistance;


import gamza.project.gamzaweb.validate.DeploymentStepQueue;
import gamza.project.gamzaweb.validate.FileUploader;
import gamza.project.gamzaweb.validate.ProjectValidate;
import gamza.project.gamzaweb.validate.UserValidate;
import gamza.project.gamzaweb.dctutil.DockerDataStore;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import gamza.project.gamzaweb.dctutil.FileController;
import gamza.project.gamzaweb.validate.custom.AdminCheck;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final PlatformRepository platformRepository;

    private final DockerProvider dockerProvider;
    private final FileUploader fileUploader;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    @AdminCheck
    public void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity user = userRepository.findById(userId).orElseThrow();

        try {
            ApplicationEntity application = ApplicationEntity.builder()
                    .tag(dto.getTag())
                    .internalPort(80)
                    .outerPort(dto.getOuterPort())
                    .variableKey(dto.getVariableKey())
                    .build();

            applicationRepository.save(application);
            applicationRepository.flush();

            PlatformEntity platform = platformRepository.findById(dto.getPlatformId())
                    .orElseThrow(() -> new BadRequestException("잘못된 플랫폼 요청입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

//            projectValidate.platformChecker(platform, dto.getProjectType().name()); // 플랫폼에 이미 BACK, FRONT 존재할경우 예외처리 버그 수정 예정

            if (dto.getProjectType().equals(ProjectType.WAIT)) {
                throw new BadRequestException("프로젝트 생성시 타입은 BACK, FRONT만 가능합니다.", ErrorCode.FAILED_PROJECT_ERROR);
            }

            ProjectEntity project = ProjectEntity.builder()
                    .application(application)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .state(dto.getState())
                    .leader(user)
                    .platformEntity(platform)
                    .projectType(dto.getProjectType())
                    .startedDate(dto.getStartedDate())
                    .endedDate(dto.getEndedDate())
                    .deploymentStep(DeploymentStep.NONE.getDescription())
                    .build();
            projectRepository.save(project);

            List<CollaboratorEntity> collaborators = new ArrayList<>();

            for (int i = 0; i < dto.getCollaborators().size(); i++) {
                UserEntity collaborator = userRepository.findById(dto.getCollaborators().get(i).longValue())
                        .orElseThrow(() -> new BadRequestException("존재하지 않는 유저 정보입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

                CollaboratorEntity collaboratorEntity = CollaboratorEntity.builder()
                        .project(project)
                        .user(collaborator)
                        .build();

                collaborators.add(collaboratorEntity);
            }

            project.addProjectCollaborator(collaborators);

            String filePath = FileController.saveFile(file.getInputStream(), project.getName(), project.getName());

            if (filePath == null) {
                throw new BadRequestException("Failed SaveFile (ZIP)", ErrorCode.FAILED_PROJECT_ERROR);
            }

            project.getApplication().updateDockerfilePath(filePath);

            projectRepository.save(project);

            fileUploader.upload(file, dto.getName(), application);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Fail Created Project (DockerFile Error)", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    @Override
    public ProjectListResponseDto getAllProject(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);

        final Long userId = (token != null && !token.isEmpty()) ? jwtTokenProvider.extractId(token) : null;
        final String userRole = (token != null && !token.isEmpty()) ? jwtTokenProvider.extractRole(token) : null;

        List<ProjectEntity> projectPage = projectRepository.findProjectsWithImages();

        List<ProjectResponseDto> collect = projectPage.stream()
                .map(project -> {
                    boolean isCollaborator = "0".equals(userRole) ||
                            (userId != null && project.getCollaborators().stream()
                                    .anyMatch(collaborator -> collaborator.getUser().getId().equals(userId)));

                    // 추후 사용할 때 주석 해제
//                    List<String> imageIds = project.getImageEntity().stream()
//                            .map(ImageEntity::getImageId)
//                            .filter(imageId -> imageId != null)
//                            .toList();

                    List<ResponseCollaboratorDto> collaboratorDtos = project.getCollaborators().stream()
                            .map(collaborator -> ResponseCollaboratorDto.builder()
                                    .id(collaborator.getUser().getId())    // User PK 값
                                    .name(collaborator.getUser().getFamilyName() + collaborator.getUser().getFamilyName()) // User 이름
                                    .studentId(collaborator.getUser().getStudentId())
                                    .build())
                            .toList();

                    String route = null; // 만약 Back만 있으면 null 반환

                    Optional<ProjectEntity> frontProject = project.getPlatformEntity().getProjects().stream()
                            .filter(p -> p.getProjectType() == ProjectType.FRONT)
                            .findFirst(); // ->  프론트 프로젝트 찾고

                    if (frontProject.isPresent()) {
                        String projectRouteName = frontProject.get().getName();
                        route = "https://"
                                + projectRouteName.toLowerCase().replace(" ", "-")
                                + ".gamzaweb.store"; // -> 실 배포시 gamza.club으로 수정
                    }


                    String containerId = null;

                    if (isCollaborator) {
                        containerId = project.getApplication().getContainerEntity().getContainerId().substring(0, 12);
                    }

                    return new ProjectResponseDto(
                            project.getId(),
                            project.getName(),
                            project.getDescription(),
                            project.getState(),
                            project.getStartedDate(),
                            project.getEndedDate(),
//                            imageIds,
                            collaboratorDtos,
                            isCollaborator,
                            route,
                            containerId,
                            project.getProjectType().name()
                    );
                })
                .collect(Collectors.toList());
        return ProjectListResponseDto.builder()
                .contents(collect)
                .build();
    }


    @Override
    public ProjectDetailResponseDto getProjectById(HttpServletRequest request, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);

        if (token == null) {
            throw new UnAuthorizedException("로그인이 필요합니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Long userId = jwtTokenProvider.extractId(token);
        String userRole = jwtTokenProvider.extractRole(token);

        ProjectEntity project = projectRepository.findByIdAndApproveStateTrue(id)
                .orElseThrow(() -> new ForbiddenException("승인되지 않은 프로젝트이거나 존재하지 않는 프로젝트입니다.", ErrorCode.FAILED_PROJECT_ERROR));

        boolean isAdmin = "0".equalsIgnoreCase(userRole);
        boolean isCollaborator = project.getLeader().getId().equals(userId) ||
                project.getCollaborators().stream()
                        .anyMatch(collaborator -> collaborator.getUser().getId().equals(userId));

        if (!isAdmin && !isCollaborator) {
            throw new ForbiddenException("프로젝트에 대한 접근 권한이 없습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        return new ProjectDetailResponseDto(project);
    }


    @Override
    public ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, Long projectId) {
        String token = jwtTokenProvider.resolveAccessToken(request);

        if (token == null || token.isEmpty()) {
            throw new UnAuthorizedException("로그인이 필요합니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Long userId = jwtTokenProvider.extractId(token);
        String userRole = jwtTokenProvider.extractRole(token);
        boolean isAdmin = "0".equalsIgnoreCase(userRole);

        ProjectEntity project = projectRepository.findByIdAndApproveStateTrue(projectId)
                .orElseThrow(() -> new ForbiddenException("승인되지 않은 프로젝트이거나 존재하지 않는 프로젝트입니다.",
                        ErrorCode.FAILED_PROJECT_ERROR));

        ApplicationEntity application = project.getApplication();
        if (application == null) {
            throw new NotFoundException("Application이 이 프로젝트에 연결되지 않았습니다.",
                    ErrorCode.NOT_FOUND_EXCEPTION);
        }

        boolean isCollaborator = project.getLeader().getId().equals(userId) ||
                projectRepository.isUserCollaborator(projectId, userId);
        String fileUrl = fileUploader.recentGetFileUrl(project);

        // jpa pk 값 순으로 젤 첫번쨰에잇느거 주면 그게 제일 최신꺼니까 -> 만약 에나중에 그 프로젝에대한 모든 zip 파일 받고싶으면 모든 findAll

        if (!isAdmin && !isCollaborator) {
            throw new ForbiddenException("프로젝트에 대한 접근 권한이 없습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        return new ApplicationDetailResponseDto(application, fileUrl);
    }


    @Override
    @Transactional
    public void updateApplication(HttpServletRequest request, ApplicationUpdateRequestDto dto, Long projectId, MultipartFile file) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnAuthorizedException("해당 유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("해당 프로젝트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        ApplicationEntity application = project.getApplication();
        if (application == null) {
            throw new NotFoundException("해당 프로젝트에 등록된 애플리케이션이 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        // 🔥 기존 파일 경로 가져오기
        String oldFilePath = application.getImageId(); // -> null 잇으면 url.
        String newFilePath = oldFilePath; // 기본적으로 기존 파일 유지

        if (file != null && !file.isEmpty()) {
            try {
                newFilePath = FileController.saveFile(file.getInputStream(), project.getName(), project.getName());
                if (newFilePath == null) {
                    throw new BadRequestException("파일 저장 실패 (ZIP)", ErrorCode.FAILED_PROJECT_ERROR);
                }

                if (oldFilePath != null && !oldFilePath.isEmpty()) {
                    FileController.deleteFile(oldFilePath);
                }

                fileUploader.upload(file, project.getName(), application);
            } catch (IOException e) {
                throw new BusinessException("파일 업로드 중 오류가 발생했습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
            }
        }

        project.updateFixedState();

        application.updateApplication(
                newFilePath,
                dto.getOuterPort(),
                dto.getTag(),
                dto.getVariableKey()
        );

        applicationRepository.save(application);
    }


    @Override
    @Transactional
    public void deleteProjectCollaborator(HttpServletRequest request, Long projectId, RequestAddCollaboratorDto dto) {

        String token = jwtTokenProvider.resolveAccessToken(request); // 공통된 로직 부분이 존재하기에 추후 리팩토링 작업시 모두 메서드 분리 예정
        String userRole = jwtTokenProvider.extractRole(token);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnAuthorizedException("해당 유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BadRequestException("해당 프로젝트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        if (!project.getLeader().equals(user) || !userRole.equals("0")) {
            throw new UnAuthorizedException("해당 프로젝트 참여 인원 수정 권한이 존재하지 않습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        CollaboratorEntity deleteCollaborator = collaboratorRepository.findByProjectIdAndUserId(project.getId(), dto.getCollaboratorId())
                .orElseThrow(() -> new BadRequestException("잘못된 요청입니다. 삭제하려는 해당 유저가 존재하지 않습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

        collaboratorRepository.delete(deleteCollaborator);
    }

    @Override
    public ProjectListPerResponseDto personalProject(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new ForbiddenException("유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        List<ProjectEntity> projects = projectRepository.findByLeaderOrderByUpdatedDateDesc(user); // -> 못찾음 수정

        List<ProjectPerResponseDto> waitProjects = projects.stream()
                .filter(project -> !project.isApproveState()) // 미승인된 프로젝트
                .map(project ->
                        new ProjectPerResponseDto(
                                project.getId(),
                                project.getName(),
                                project.getApplication().getOuterPort(),
                                fileUploader.recentGetFileUrl(project),
                                null))// 승인되지 않은 프로젝트는 컨테이너 없다.
//                                project.getApplication().getContainerEntity().getContainerId().substring(0, 12))) //컨테이너 아이디는 12글자만 있으면 조회 됨
                .collect(Collectors.toList());

        List<ProjectPerResponseDto> completeProjects = projects.stream()
                .filter(ProjectEntity::isApproveState) // 승인된 프로젝트
                .map(project ->
                        new ProjectPerResponseDto(
                                project.getId(),
                                project.getName(),
                                project.getApplication().getOuterPort(),
                                fileUploader.recentGetFileUrl(project),
                                project.getApplication().getContainerEntity().getContainerId().substring(0, 12)))
                .collect(Collectors.toList());

        return ProjectListPerResponseDto.builder()
                .waitProjects(waitProjects)
                .completeProjects(completeProjects)
                .build();
    }

    @Override
    @Transactional
    public void deleteProjectById(HttpServletRequest request, Long projectId) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ForbiddenException("프로젝트를 찾을 수 없습니다.", ErrorCode.FAILED_PROJECT_ERROR));

        // 프로젝트 리더인지 확인
        if (!project.getLeader().getId().equals(userId)) {
            throw new ForbiddenException("프로젝트 리더만 삭제할 수 있습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        ContainerEntity containerId = project.getApplication().getContainerEntity();

        dockerProvider.stopContainer(request, containerId); // container stop
        dockerProvider.removeContainer(containerId.getContainerId());
        dockerProvider.removeAllImage(request, project); // project image remove All
        dockerProvider.removeProjectDirInServer(request, project);

        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request); // 공통된 로직 부분이 존재하기에 추후 리팩토링 작업시 모두 메서드 분리 예정
        String userRole = jwtTokenProvider.extractRole(token);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnAuthorizedException("해당 유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 프로젝트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        if (!project.getLeader().equals(user)) {
            if (!userRole.equals("0")) {
                throw new UnAuthorizedException("해당 프로젝트 참여 인원 수정 권한이 존재하지 않습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
            }
        }

        if (dto.getPlatformId() != null) { // 플랫폼 업데이트
            PlatformEntity platform = platformRepository.findById(dto.getPlatformId())
                    .orElseThrow(() -> new BadRequestException("변경할 수 있는 플랫폼이 없습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

            project.updatePlatform(platform);
        }

        collaboratorRepository.deleteAllByProject(project); // 기존 해당 프로젝트의 협력자 모두 삭제

        List<CollaboratorEntity> newCollaborators = new ArrayList<>(); // 새로운 배열 생성해서 입력받은 생성자 추가

        for (int i = 0; i < dto.getCollaborators().size(); i++) {
            UserEntity collaborator = userRepository.findById(dto.getCollaborators().get(i).longValue())
                    .orElseThrow(() -> new BadRequestException("존재하지 않는 유저 정보입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

            CollaboratorEntity collaboratorEntity = CollaboratorEntity.builder()
                    .project(project)
                    .user(collaborator)
                    .build();

            newCollaborators.add(collaboratorEntity);
        }

        project.updateProject(dto.getName(), dto.getDescription(), dto.getState(), dto.getStartedDate(), dto.getEndedDate(), newCollaborators, dto.getProjectType());
        projectRepository.save(project);

    }

    @Override
    public void removeTeamProjectInMyPage(HttpServletRequest request, Long id) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저가 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 프로젝트가 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        if (!project.getLeader().equals(user)) {
            throw new InvalidTokenException("프로젝트 삭제 권한이 없습니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }
        projectRepository.delete(project);
    }

}
