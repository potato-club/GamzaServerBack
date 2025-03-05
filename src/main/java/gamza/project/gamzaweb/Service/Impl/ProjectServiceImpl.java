package gamza.project.gamzaweb.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import gamza.project.gamzaweb.Dto.User.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.Dto.User.response.ResponseCollaboratorDto;
import gamza.project.gamzaweb.Dto.docker.ImageBuildEventDto;
import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Entity.*;
import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.Enums.ProjectType;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.*;
import gamza.project.gamzaweb.Repository.*;
import gamza.project.gamzaweb.Service.Interface.NginxService;
import gamza.project.gamzaweb.Service.Interface.PlatformService;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Interface.ProjectStatusService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import gamza.project.gamzaweb.Validate.DeploymentStepQueue;
import gamza.project.gamzaweb.Validate.FileUploader;
import gamza.project.gamzaweb.Validate.ProjectValidate;
import gamza.project.gamzaweb.Validate.UserValidate;
import gamza.project.gamzaweb.dctutil.DockerDataStore;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import gamza.project.gamzaweb.dctutil.DockerProvider.DockerProviderBuildCallback;
import gamza.project.gamzaweb.dctutil.FileController;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();
    private final DockerProvider dockerProvider;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FileUploader fileUploader;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final ImageRepository imageRepository;
    private final ContainerRepository containerRepository;
    private final PlatformRepository platformRepository;

    private final PlatformService platformService;
    private final ProjectStatusService projectStatusService;
    private final DeploymentStepQueue deploymentStepQueue;
    private final NginxService nginxService;

    private final UserValidate userValidate;
    private final ProjectValidate projectValidate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드풀에 일단 5개 생성 먼저

    @Override
    @Transactional
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
        System.out.println(projectPage);

        List<ProjectResponseDto> collect = projectPage.stream()
                .map(project -> {
                    boolean isCollaborator = "0".equals(userRole) ||
                            (userId != null && project.getCollaborators().stream()
                                    .anyMatch(collaborator -> collaborator.getUser().getId().equals(userId)));

                    List<String> imageIds = project.getImageEntity().stream()
                            .map(ImageEntity::getImageId)
                            .filter(imageId -> imageId != null)
                            .toList();

                    List<ResponseCollaboratorDto> collaboratorDtos = project.getCollaborators().stream()
                            .map(collaborator -> ResponseCollaboratorDto.builder()
                                    .id(collaborator.getUser().getId())    // User PK 값
                                    .name(collaborator.getUser().getFamilyName() + collaborator.getUser().getFamilyName()) // User 이름
                                    .studentId(collaborator.getUser().getStudentId())
                                    .build())
                            .toList();
                    String route = "https://"
                            + project.getName().toLowerCase().replace(" ", "-")
                            + ".gamza-club.com";

                    return new ProjectResponseDto(
                            project.getId(),
                            project.getName(),
                            project.getDescription(),
                            project.getState(),
                            project.getStartedDate(),
                            project.getEndedDate(),
                            imageIds,
                            collaboratorDtos,
                            isCollaborator,
                            route
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

        List<ProjectEntity> projects = projectRepository.findByLeaderOrderByUpdatedDateDesc(user);

        // 승인된 프로젝트와 미승인된 프로젝트를 나눔
        //.zip 추후 수정해야함: zip 파일 이름으로
        List<ProjectPerResponseDto> waitProjects = projects.stream()
                .filter(project -> !project.isApproveState()) // 미승인된 프로젝트
                .map(project ->
                        new ProjectPerResponseDto(
                                project.getId(),
                                project.getName(),
                                project.getApplication().getOuterPort(),
                                fileUploader.recentGetFileUrl(project),
                                project.getApplication().getContainerEntity().getContainerId().substring(0,12))) //컨테이너 아이디는 12글자만 있으면 조회 됨
                .collect(Collectors.toList());

        List<ProjectPerResponseDto> completeProjects = projects.stream()
                .filter(ProjectEntity::isApproveState) // 승인된 프로젝트
                .map(project ->
                        new ProjectPerResponseDto(
                                project.getId(),
                                project.getName(),
                                project.getApplication().getOuterPort(),
                                fileUploader.recentGetFileUrl(project),
                                project.getApplication().getContainerEntity().getContainerId().substring(0,12)))
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

        List<CollaboratorEntity> newCollaborators = new ArrayList<>();

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
    public Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveState(false, false, pageable);

        return projectEntities.map(project -> {
            String fileUrl = fileUploader.recentGetFileUrl(project);
            return new ProjectListNotApproveResponse(project, fileUrl);
        });
    }

    @Override
    public Page<ProjectListApproveResponse> approvedProjectList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        //이거 나중에 쿼리DSL로 돌리기
//        Page<ProjectEntity> projectEntities = projectRepository.findByApprovalProjectStatusIsNotNull(pageable);
        Page<ProjectEntity> projectEntities = projectRepository.findByApprovalProjectStatusIsNotNullAndSuccessCheckFalse(pageable);

        return projectEntities.map(project -> {
            String fileUrl = fileUploader.recentGetFileUrl(project);
            return new ProjectListApproveResponse(project, fileUrl);
        });
    }

    @Override
    public Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        // 승인요청 fixedState 는 true이고 approveFixedState(승인요청 상태가 미허가된 상태 0false) 인애들만 추출
        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveFixedState(true, false, pageable);

        return projectEntities.map(FixedProjectListNotApproveResponse::new);

    }

    @Override
    @Transactional
    public void checkSuccessProject(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        ProjectEntity project = projectValidate.validateProject(id);
        project.updateSuccessCheck();
    }


    @Override
    public void removeExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        projectValidate.validateProject(id);
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void approveFixedExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        ProjectEntity project = getProjectById(id);

        ContainerEntity containerEntity = containerRepository.findContainerEntityByApplication(project.getApplication());
        dockerProvider.stopContainer(request, containerEntity);
        dockerProvider.removeContainer(containerEntity.getContainerId());
        containerRepository.delete(containerEntity);

        String AT = request.getHeader("Authorization").substring(7);

        boolean buildSuccess = buildDockerImageFromApplicationZip(AT, project);
        if (buildSuccess) {
            updateProjectApprovalFixedState(project);
        }
    }


    @Override
    @Transactional
    public void removeFixedExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        ProjectEntity project = projectValidate.validateProject(id);

        if (!project.isFixedState()) {
            throw new BadRequestException("해당 프로젝트는 수정 요청 상태가 아닙니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

        projectRepository.deleteById(id);
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

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void updateApprovalStatus(ProjectEntity project, ApprovalProjectStatus status) {
////        deploymentStepQueue.addDeploymentUpdate(project, status); // 이거를 물어봐야겠따 어떻게 처리할까??
//        project.updateApprovalStatus(status);
//        projectRepository.save(project);
//    }


    @Override
    @Transactional
    public void approveExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);

        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 프로젝트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        // 배포 시작 상태 SSE 전송
        project.updateApprovalProjectStatus(ApprovalProjectStatus.PENDING);
        projectRepository.save(project);

        String AT = request.getHeader("Authorization").substring(7);

        executorService.submit(() -> {
            try {
                projectRepository.save(project);
                boolean buildSuccess = buildDockerImageFromApplicationZip(AT, project);
                if (buildSuccess) {
                    projectStatusService.updateDeploymentStep(project, DeploymentStep.SUCCESS);
                    project.updateApprovalProjectStatus(ApprovalProjectStatus.SUCCESS); // 승인 상태 업데이트
                    updateProjectApprovalState(project);
                } else {
                    projectStatusService.updateDeploymentStep(project, DeploymentStep.FAILED);
                    project.updateApprovalProjectStatus(ApprovalProjectStatus.FAILED); //  승인 실패 업데이트
                }
            } catch (Exception e) {
                e.printStackTrace();
                projectStatusService.updateDeploymentStep(project, DeploymentStep.FAILED);
                project.updateApprovalProjectStatus(ApprovalProjectStatus.FAILED); // 승인 실패 업데이트
            }
            projectRepository.save(project);
        });
    }

    private boolean buildDockerImageFromApplicationZip(String token, ProjectEntity project) {
        if (project.getApplication().getImageId() == null) {
            projectStatusService.updateDeploymentStep(project, DeploymentStep.ZIP_PATH_CHECK);

            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.ZIP_PATH_CHECK);
            throw new BadRequestException("PROJECT ZIP PATH IS NULL", ErrorCode.FAILED_PROJECT_ERROR);
        }

        AtomicBoolean buildSuccess = new AtomicBoolean(false);

        try {
            projectStatusService.updateDeploymentStep(project, DeploymentStep.DOCKERFILE_EXTRACT);
            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.DOCKERFILE_EXTRACT);
            Path dockerfilePath = extractDockerfileFromZip(project.getApplication().getImageId(), project.getName());

            projectStatusService.updateDeploymentStep(project, DeploymentStep.DOCKER_BUILD);
            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.DOCKER_BUILD);
            buildDockerImage(
                    token,
                    dockerfilePath.toFile(),
                    project,
                    imageId -> {

                        createContainer(token, project, imageId);

                        // Docker 빌드 성공 후 Nginx 설정 생성
                        String applicationName = project.getName();
                        int applicationPort = project.getApplication().getOuterPort();

                        deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.NGINX_CONFIG);
                        projectStatusService.updateDeploymentStep(project, DeploymentStep.NGINX_CONFIG);

                        nginxService.generateNginxConf(applicationName, applicationPort);
                        nginxService.restartNginx(); // Nginx 재시작

                        deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.NGINX_RELOAD);
                        projectStatusService.updateDeploymentStep(project, DeploymentStep.NGINX_RELOAD);

                        System.out.println("Docker image built successfully: " + imageId);
                        deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.SUCCESS);
                        projectStatusService.updateDeploymentStep(project, DeploymentStep.SUCCESS);
                        buildSuccess.set(true);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            projectStatusService.updateDeploymentStep(project, DeploymentStep.FAILED);

            throw new BadRequestException("Failed to extract Dockerfile from ZIP", ErrorCode.FAILED_PROJECT_ERROR);
        }
        return buildSuccess.get();
    }

    private void createContainer(String token, ProjectEntity project, String imageId) {
//        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
                .withName(project.getName())
//                .withExposedPorts(ExposedPort.tcp(project.getApplication().getInternalPort()))
                .withExposedPorts(ExposedPort.tcp(project.getApplication().getOuterPort()))
                .withHostConfig(newHostConfig()
                        .withPortBindings(new PortBinding(
                                Binding.bindPort(project.getApplication().getOuterPort()),
                                ExposedPort.tcp(project.getApplication().getOuterPort())
                        )))
                .withImage(imageId)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        ContainerEntity containerEntity = ContainerEntity.builder()
                .application(project.getApplication())
                .containerId(container.getId())
                .imageId(project.getName() + ":" + project.getApplication().getTag())
                .user(userPk)
                .build();

        containerRepository.save(containerEntity);
    }

    private void buildDockerImage(String token, File dockerfile, ProjectEntity project, DockerProviderBuildCallback callback) {
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        Optional<ImageEntity> existingImage = imageRepository.findByProjectAndUser(project, userPk);

        if (existingImage.isEmpty()) {  // 중복이 없을 때만 저장
            ImageEntity imageEntity = ImageEntity.builder()
                    .project(project)
                    .user(userPk)
                    .name(project.getName())
                    .variableKey(project.getApplication().getVariableKey())
                    .build();
            imageRepository.save(imageEntity);
        }

        if (isImageExists(project.getName())) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }

        executeDockerBuild(dockerfile, project.getName(), project.getApplication().getVariableKey(), project.getApplication().getTag(), callback, userPk);
    }

//    private void createContainerFromImage(String imageName, String containerName, String portMapping, UserEntity user, DockerProvider.DockerProviderBuildCallback callback) {
//        try {
//            // Docker 이미지 조회
//            List<Image> existingImages = dockerClient.listImagesCmd().exec();
//            String imageId = existingImages.stream()
//                    .filter(image -> Arrays.asList(image.getRepoTags()).contains(imageName))
//                    .map(Image::getId)
//                    .findFirst()
//                    .orElseThrow(() -> new DockerRequestException("3003 IMAGE NOT FOUND", ErrorCode.FAILED_IMAGE_FOUND));
//
//            // 컨테이너 생성
//            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
//                    .withName(containerName)
//                    .withHostConfig(HostConfig.newHostConfig()
//                            .withPortBindings(PortBinding.parse(portMapping)))
//                    .exec();
//
//            // 컨테이너 실행
//            dockerClient.startContainerCmd(container.getId()).exec();
//
//            // 컨테이너 정보를 DB에 저장
////            saveContainerInfoToDatabase(container.getId(), imageId, user);
////
////             성공 시 콜백 호출
////            callback.onContainerCreated(container.getId());
//
//            System.out.println("Container created and started successfully: " + containerName);
//
//        } catch (Exception e) {
//            e.printStackTrace();
////            throw new DockerRequestException("3002 FAILED CONTAINER CREATION", ErrorCode.FAILED_CONTAINER_CREATION);
//        }
//    }

//
//    private boolean isImageExists(String name) {
//        List<Image> existingImages = dockerClient.listImagesCmd().exec();
//        return existingImages.stream()
//                .anyMatch(image -> image.getRepoTags() != null &&
//                        Arrays.asList(image.getRepoTags()).contains(name));
//    }

    //null 체크 먼저하게 했음
    private boolean isImageExists(String name) {
        List<Image> existingImages = dockerClient.listImagesCmd().exec();
        return existingImages.stream()
                .filter(image -> image.getRepoTags() != null)
                .flatMap(image -> Arrays.stream(image.getRepoTags()))
                .anyMatch(tag -> tag.equals(name));
    }


    private void executeDockerBuild(File dockerfile, String name, @Nullable String key, String tag, DockerProviderBuildCallback callback, UserEntity userPk) {
        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(dockerfile);

        if (key != null && !key.isEmpty()) {
            buildImageCmd.withBuildArg("key", key);
        }

        try {
            buildImageCmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    super.onNext(item);
                    if (item.getImageId() != null) {
                        dockerProvider.taggingImage(item.getImageId(), name, tag);

                        ImageBuildEventDto event = new ImageBuildEventDto(userPk, item.getImageId(), name, key);
                        applicationEventPublisher.publishEvent(event);

                        try {
                            callback.getImageId(item.getImageId()); // 여기서 콜백 호출
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                    System.out.println("Docker build completed successfully");
                }

                @Override
                public void onError(Throwable throwable) {
                    super.onError(throwable);
                    System.err.println("Docker build failed: " + throwable.getMessage());
                }
            }).awaitCompletion(); // 이미지가 빌드되는 동안 대기 -> 스케줄러로 변경해야하는 부분
        } catch (Exception e) {
            e.printStackTrace();
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }
    }


    private Path extractDockerfileFromZip(String parentDirectory, String projectName) throws IOException {
        File unzipResultDir = FileController.unzip(parentDirectory + File.separator + projectName + ".zip");

        if (unzipResultDir == null) {
            throw new IOException("Zip 파일 압축 해제를 실패하였습니다.");
        }

        File[] files = unzipResultDir.listFiles();
        File dockerfile = null;
        if (files == null) {
            throw new BadRequestException("Dockerfile not found in the extracted archive", ErrorCode.FAILED_PROJECT_ERROR);
        }

        for (File file : files) {
            if (file.getName().equals("Dockerfile") && file.isFile()) {
                dockerfile = file;
            }
        }

        if (!dockerfile.exists()) {
            throw new BadRequestException("Dockerfile not found in the extracted archive", ErrorCode.FAILED_PROJECT_ERROR);
        }

        return dockerfile.toPath();
    }

    private ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("4001 NOT FOUND PROJECT", ErrorCode.FAILED_PROJECT_ERROR));
    }

    private void updateProjectApprovalFixedState(ProjectEntity project) {
        project.approveFixedProject();
        projectRepository.save(project);
    }

    private void updateProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }

}

