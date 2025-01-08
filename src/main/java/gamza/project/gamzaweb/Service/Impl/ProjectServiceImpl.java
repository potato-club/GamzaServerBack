package gamza.project.gamzaweb.Service.Impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.HostConfig;
import gamza.project.gamzaweb.Dto.User.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.Dto.User.ResponseCollaboratorDto;
import gamza.project.gamzaweb.Dto.docker.ImageBuildEventDto;
import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Entity.*;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.*;
import gamza.project.gamzaweb.Repository.*;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import gamza.project.gamzaweb.Validate.ProjectValidate;
import gamza.project.gamzaweb.Validate.UserValidate;
import gamza.project.gamzaweb.dctutil.DockerDataStore;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import gamza.project.gamzaweb.dctutil.FileController;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

import java.io.FileWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;


@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final ImageRepository imageRepository;
    private final UserValidate userValidate;
    private final ProjectValidate projectValidate;
    private final ContainerRepository containerRepository;

    private final DockerProvider dockerProvider;
    private final ApplicationEventPublisher applicationEventPublisher;

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

            ProjectEntity project = ProjectEntity.builder()
                    .application(application)
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .state(dto.getState())
                    .leader(user)
                    .startedDate(dto.getStartedDate())
                    .endedDate(dto.getEndedDate())
                    .build();


            String filePath = FileController.saveFile(file.getInputStream(), project.getName(), project.getName());

            if (filePath == null) {
                throw new BadRequestException("Failed SaveFile (ZIP)", ErrorCode.FAILED_PROJECT_ERROR);
            }

            project.getApplication().updateDockerfilePath(filePath);

            projectRepository.save(project);


        } catch (Exception e) {
            throw new BadRequestException("Fail Created Project (DockerFile Error)", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    @Override
    public ProjectListResponseDto getAllProject() {
        List<ProjectEntity> projectPage = projectRepository.findProjectsWithImages();

        System.out.println(projectPage);

        List<ProjectResponseDto> collect = projectPage.stream()
                .map(project -> {
                    List<String> imageIds = project.getImageEntity().stream()
                            .map(ImageEntity::getImageId)
                            .filter(imageId -> imageId != null)
                            .toList();

                    List<ResponseCollaboratorDto> collaboratorDtos = project.getCollaborators().stream()
                            .map(collaborator -> ResponseCollaboratorDto.builder()
                                    .id(collaborator.getUser().getId())    // User PK 값
                                    .name(collaborator.getUser().getFamilyName() + collaborator.getUser().getFamilyName()) // User 이름
                                    .build())
                            .toList();

                    return new ProjectResponseDto(
                            project.getId(),
                            project.getName(),
                            project.getDescription(),
                            project.getState(),
                            project.getStartedDate(),
                            project.getEndedDate(),
                            imageIds,
                            collaboratorDtos
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
        Long userId = jwtTokenProvider.extractId(token);
        ProjectEntity project = projectRepository.findByIdAndApproveStateTrue(id)
                .orElseThrow(() -> new ForbiddenException("승인되지 않은 프로젝트이거나 존재하지 않는 프로젝트입니다.", ErrorCode.FAILED_PROJECT_ERROR));

        if (!project.getLeader().getId().equals(userId)) {
            throw new ForbiddenException("프로젝트 리더만 접근 가능합니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        return new ProjectDetailResponseDto(project);
    }

    @Override
    public ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, Long projectId) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        ProjectEntity project = projectRepository.findByIdAndApproveStateTrue(projectId)
                .orElseThrow(() -> new ForbiddenException("승인되지 않은 프로젝트이거나 존재하지 않는 프로젝트입니다.", ErrorCode.FAILED_PROJECT_ERROR));

        if (!project.getLeader().getId().equals(userId)) {
            throw new ForbiddenException("프로젝트 리더만 접근 가능합니다.", ErrorCode.FORBIDDEN_EXCEPTION);
        }

        ApplicationEntity application = project.getApplication();
        if (application == null) {
            throw new NotFoundException("Application이 이 프로젝트에 연결되지 않았습니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        return new ApplicationDetailResponseDto(application);
    }

    @Override
    @Transactional
    public void addProjectCollaborator(HttpServletRequest request, Long projectId, RequestAddCollaboratorDto dto) {

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

        UserEntity collaborator = userRepository.findById(dto.getCollaboratorId())
                .orElseThrow(() -> new UnAuthorizedException("추가하려는 해당 유저를 찾을 수 없습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION));

        project.updateProjectCollaborator(collaborator);
        projectRepository.save(project);

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
                                ".zip"))
                .collect(Collectors.toList());

        List<ProjectPerResponseDto> completeProjects = projects.stream()
                .filter(ProjectEntity::isApproveState) // 승인된 프로젝트
                .map(project ->
                        new ProjectPerResponseDto(
                                project.getId(),
                                project.getName(),
                                project.getApplication().getOuterPort(),
                                ".zip"))
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

//        project.updateProject(dto.getName(), dto.getDescription(), dto.getState(), dto.getStartedDate(), dto.getEndedDate());
//        projectRepository.save(project);

    }

    @Override
    public Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveState(false, false, pageable);

        return projectEntities.map(ProjectListNotApproveResponse::new);

        // 이거 리스트 만들기 전에 프로젝트 승인해주는 api 먼저 만들기
    }

    @Override
    public Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(HttpServletRequest request, Pageable pageable) {
        userValidate.validateUserRole(request);

        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveFixedState(true, false, pageable);

        return projectEntities.map(FixedProjectListNotApproveResponse::new);

    }

    @Override
    public void approveExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        ProjectEntity project = getProjectById(id);
        checkProjectApprovalState(project);

        // Docker 이미지 빌드
        buildDockerImageFromApplicationZip(request, project);
    }

    @Override
    public void removeExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        projectValidate.validateProject(id);
        projectRepository.deleteById(id);
    }

    @Override
    public void approveFixedExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        ProjectEntity project = getProjectById(id);
        checkProjectApprovalState(project);


        // Docker 이미지 빌드
        buildDockerImageFromApplicationZip(request, project);
    }


    @Override
    public void removeFixedExecutionApplication(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);
        projectValidate.validateProject(id);
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

    //
    private void buildDockerImageFromApplicationZip(HttpServletRequest request, ProjectEntity project) {
        if (project.getApplication().getImageId() == null) {
            throw new BadRequestException("PROJECT ZIP PATH IS NULL", ErrorCode.FAILED_PROJECT_ERROR);
        }

        try {
            Path dockerfilePath = extractDockerfileFromZip(project.getApplication().getImageId());

            buildDockerImage(
                    request,
                    dockerfilePath.toFile(),
                    project,
                    imageId -> {
                        createContainer(request, project, imageId);

//             Docker 빌드 성공 후 Nginx 설정 처리
//            generateNginxConfig(project.getName(), project.getApplication().getOuterPort());
//            reloadNginx();

                        System.out.println("Docker image built successfully: " + imageId);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Failed to extract Dockerfile from ZIP", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    private void createContainer(HttpServletRequest request, ProjectEntity project, String imageId) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        CreateContainerResponse container = dockerClient.createContainerCmd(project.getName())
                .withExposedPorts(ExposedPort.tcp(project.getApplication().getOuterPort()))
                .withHostConfig(newHostConfig()
                        .withPortBindings(new PortBinding(Ports.Binding.bindPort(project.getApplication().getOuterPort()),
                                ExposedPort.tcp(project.getApplication().getInternalPort()))))
                .withImage(imageId)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        ContainerEntity containerEntity = ContainerEntity.builder()
                .containerId(container.getId())
                .imageId(project.getName() + ":" + project.getApplication().getTag())
                .user(userPk)
                .build();

        containerRepository.save(containerEntity);
    }

    private void buildDockerImage(HttpServletRequest request, File dockerfile, ProjectEntity project, DockerProvider.DockerProviderBuildCallback callback) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);


        ImageEntity imageEntity = ImageEntity.builder()
                .project(project)
                .user(userPk)
                .name(project.getName())
                .variableKey(project.getApplication().getVariableKey())
                .build();
        imageRepository.save(imageEntity);

        if (isImageExists(project.getName())) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }

        executeDockerBuild(dockerfile, project.getName(), project.getApplication().getVariableKey(), project.getApplication().getTag(), callback, userPk);
    }

    private void createContainerFromImage(String imageName, String containerName, String portMapping, UserEntity user, DockerProvider.DockerProviderBuildCallback callback) {
        try {
            // Docker 이미지 조회
            List<Image> existingImages = dockerClient.listImagesCmd().exec();
            String imageId = existingImages.stream()
                    .filter(image -> Arrays.asList(image.getRepoTags()).contains(imageName))
                    .map(Image::getId)
                    .findFirst()
                    .orElseThrow(() -> new DockerRequestException("3003 IMAGE NOT FOUND", ErrorCode.FAILED_IMAGE_FOUND));

            // 컨테이너 생성
            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withName(containerName)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withPortBindings(PortBinding.parse(portMapping)))
                    .exec();

            // 컨테이너 실행
            dockerClient.startContainerCmd(container.getId()).exec();

            // 컨테이너 정보를 DB에 저장
//            saveContainerInfoToDatabase(container.getId(), imageId, user);
//
//             성공 시 콜백 호출
//            callback.onContainerCreated(container.getId());

            System.out.println("Container created and started successfully: " + containerName);

        } catch (Exception e) {
            e.printStackTrace();
//            throw new DockerRequestException("3002 FAILED CONTAINER CREATION", ErrorCode.FAILED_CONTAINER_CREATION);
        }
    }


    private boolean isImageExists(String name) {
        List<Image> existingImages = dockerClient.listImagesCmd().exec();
        return existingImages.stream()
                .anyMatch(image -> image.getRepoTags() != null &&
                        Arrays.asList(image.getRepoTags()).contains(name));
    }

    private void executeDockerBuild(File dockerfile, String name, @Nullable String key, String tag, DockerProvider.DockerProviderBuildCallback callback, UserEntity userPk) {
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

                        callback.getImageId(item.getImageId()); // 여기서 콜백 호출

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


    private void generateNginxConfig(String applicationName, int applicationPort) {
        String configContent = """
                    server {
                        listen 80;
                        listen [::]:80;
                        server_name %s.gamza.club;
                        return 301 https://$host$request_uri;
                    }
                    server {
                        listen 443 ssl http2;
                        listen [::]:443 ssl http2;
                        server_name %s.gamza.club;
                        ssl_certificate /etc/letsencrypt/live/gamza.club/fullchain.pem;
                        ssl_certificate_key /etc/letsencrypt/live/gamza.club/privkey.pem;
                
                        location / {
                            proxy_pass http://localhost:%d;
                            proxy_http_version 1.1;
                            proxy_set_header Upgrade $http_upgrade;
                            proxy_set_header Connection 'upgrade';
                            proxy_set_header Host $host;
                            proxy_cache_bypass $http_upgrade;
                        }
                    }
                """.formatted(applicationName, applicationName, applicationPort);

        try (FileWriter writer = new FileWriter("/etc/nginx/conf.d/" + applicationName + ".conf")) {
            writer.write(configContent);
            System.out.println("Nginx config generated for: " + applicationName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Nginx config for: " + applicationName, e);
        }
    }

    private void reloadNginx() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "nginx -s reload");

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Nginx reloaded successfully.");
            } else {
                System.err.println("Failed to reload Nginx. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to reload Nginx", e);
        }
    }

    private Path extractDockerfileFromZip(String zipPath) throws IOException {
        boolean unzipResult = FileController.unzip(zipPath);

        if (!unzipResult) {
            throw new IOException("Zip 파일 압축 해제를 실패하였습니다.");
        }

        File zipFile = new File(zipPath);
        String extractedDirectoryPath = zipFile.getParent(); // ZIP 파일과 동일한 폴더

        File dockerfile = new File(extractedDirectoryPath, "Dockerfile");
        System.out.println("Extracted Dockerfile path: " + dockerfile.getAbsolutePath());

        if (!dockerfile.exists()) {
            throw new BadRequestException("Dockerfile not found in the extracted archive", ErrorCode.FAILED_PROJECT_ERROR);
        }

        return dockerfile.toPath();
    }


    private ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("4001 NOT FOUND PROJECT", ErrorCode.FAILED_PROJECT_ERROR));
    }

    private void checkProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }

    private void updateProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }

}
