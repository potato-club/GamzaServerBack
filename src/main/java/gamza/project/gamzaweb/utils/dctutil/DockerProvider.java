package gamza.project.gamzaweb.utils.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.dto.project.request.ImageBuildEventRequestDto;
import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.BadRequestException;
import gamza.project.gamzaweb.utils.error.requestError.DockerRequestException;
import gamza.project.gamzaweb.utils.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.ContainerRepository;
import gamza.project.gamzaweb.repository.ImageRepository;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.NginxService;
import gamza.project.gamzaweb.service.Interface.ProjectStatusService;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import gamza.project.gamzaweb.utils.validate.DeploymentStepQueue;
import gamza.project.gamzaweb.utils.validate.ProjectValidate;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
@Component
@RequiredArgsConstructor
@Slf4j
public class DockerProvider {

    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드풀에 일단 5개 생성 먼저

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ContainerRepository containerRepository;
    private final ProjectRepository projectRepository;

    private final ProjectValidate projectValidate;
    private final ProjectStatusService projectStatusService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeploymentStepQueue deploymentStepQueue;
    private final NginxService nginxService;

    public void taggingImage(String imageId, String name, String tag) {
        dockerClient.tagImageCmd(imageId, name, tag).exec();
    }

    public void stopContainer(HttpServletRequest request, ContainerEntity container) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);

        if (!userRole.equals("0")) {
            throw new UnAuthorizedException("401 ERROR USER NOT FOUNT ", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        System.out.println("delete ContainerId : " + container.getContainerId());

        try {
            StopContainerCmd stopContainer = dockerClient.stopContainerCmd(container.getContainerId());
            stopContainer.exec();
//            containerRepository.delete(container);
        } catch (NotModifiedException e) {
            e.printStackTrace();
            //maybe already stopped!
        } catch (NotFoundException e1) {
            e1.printStackTrace();
            //not found exception
        }
    }


    public void removeContainer(String containerId) {
        RemoveContainerCmd removeCmd = dockerClient.removeContainerCmd(containerId);
        removeCmd.hasForceEnabled(); //check :: is force enabled??
        removeCmd.hasRemoveVolumesEnabled();
        removeCmd.exec();
    }

    @Transactional
    public void removeAllImage(HttpServletRequest request, ProjectEntity project) {
        String token = jwtTokenProvider.resolveAccessToken(request); // 토큰 체크
        Long userId = jwtTokenProvider.extractId(token);

        projectValidate.isParticipateInProject(project.getId(), userId);

        List<ImageEntity> projectImageList = imageRepository.findImageEntitiesByProjectId(project.getId()); // List로 반환 없으면 null // null 일수도 있겠다 해당 프로젝트가 제대로 못열렸다면.

        if (!projectImageList.isEmpty()) {
            for (ImageEntity imageEntity : projectImageList) {
                RemoveImageCmd removeImageCmd = dockerClient.removeImageCmd(imageEntity.getImageId()); // 도커에서 삭제
                removeImageCmd.withForce(true);
                removeImageCmd.exec();
                imageRepository.delete(imageEntity); // db에서 삭제
            }
        }

    }

    public void removeProjectDirInServer(HttpServletRequest request, ProjectEntity project) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        projectValidate.isParticipateInProject(project.getId(), userId);

        FileController.deleteFileInRoot(project.getApplication().getImageId());
    }

    public void removeImage(String imageId, HttpServletRequest request) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        if (userPk == null) {
            throw new UnAuthorizedException("401 ERROR USER NOT FOUND", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Optional<ImageEntity> userImage = imageRepository.findByImageIdAndUser(imageId, userPk);

        if (userImage.isEmpty()) {
            throw new DockerRequestException("3007 FAILED IMAGE REMOVE", ErrorCode.FAILED_IMAGE_DELETE);
        }

        RemoveImageCmd removeCmd = dockerClient.removeImageCmd(imageId);
        removeCmd.withForce(true); //check :: is force enabled??
        removeCmd.exec();

        imageRepository.delete(userImage.get());
    }

    public List<String> getContainerLogs(String containerId, int lines) {
        List<String> logs = new ArrayList<>();

        try (LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)) {
            logContainerCmd
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(lines)
                    .exec(new LogContainerResultCallback() {
                        @Override
                        public void onNext(Frame frame) {
                            logs.add(new String(frame.getPayload()));
                        }
                    }).awaitCompletion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logs;
    }


//    public String updateNginxConfig(String containerId, String port, String cname) {
//        try {
//            String configContent = generateNginxConfig(port, cname);
//            Path tempFile = Files.createTempFile("nginx", ".conf");
//            Files.write(tempFile, configContent.getBytes());
//
//            getDockerClient().copyArchiveToContainerCmd(containerId)
//                    .withHostResource(tempFile.toString())
//                    .withRemotePath("/etc/nginx/nginx.conf")
//                    .exec();
//
//            getDockerClient().restartContainerCmd(containerId).exec();
//
//            Files.delete(tempFile);
//            return "Nginx config updated and container restarted.";
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "Error updating Nginx config: " + e.getMessage();
//        }
//    }
//
//    private String generateNginxConfig(String port, String cname) {
//        return """
//                server {
//                    listen %s;
//                    server_name %s;
//
//                    location / {
//                        proxy_pass http://localhost:8080;
//                        proxy_set_header Host $host;
//                        proxy_set_header X-Real-IP $remote_addr;
//                        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
//                        proxy_set_header X-Forwarded-Proto $scheme;
//                    }
//                }
//                """.formatted(port, cname);
//    }


    //example interface ---

    public interface DockerProviderBuildCallback {
        void getImageId(String imageId) throws IOException;
    }


    public interface innerDockerProviderBuildCallback {
        void closeFunction();
    }

    //examples end---

    @Transactional
    public void startExecutionApplication(ProjectEntity project, String AT) {

        executorService.submit(() -> {
            try {
                projectRepository.save(project);
                boolean buildSuccess = buildDockerImageFromApplicationZip(AT, project);
                if (buildSuccess) {
                    projectStatusService.sendDeploymentStep(project, DeploymentStep.SUCCESS);
                    project.updateApprovalProjectStatus(ApprovalProjectStatus.SUCCESS); // 승인 상태 업데이트
                    updateProjectApprovalState(project);
                } else {
                    projectStatusService.sendDeploymentStep(project, DeploymentStep.FAILED);
                    project.updateApprovalProjectStatus(ApprovalProjectStatus.FAILED); //  승인 실패 업데이트
                }
            } catch (Exception e) {
                e.printStackTrace();
                projectStatusService.sendDeploymentStep(project, DeploymentStep.FAILED);
                project.updateApprovalProjectStatus(ApprovalProjectStatus.FAILED); // 승인 실패 업데이트
            }
            projectRepository.save(project);
        });
    }

    public boolean buildDockerImageFromApplicationZip(String token, ProjectEntity project) {
        if (project.getApplication().getImageId() == null) {
            throw new BadRequestException("PROJECT ZIP PATH IS NULL", ErrorCode.FAILED_PROJECT_ERROR);
        }

        AtomicBoolean buildSuccess = new AtomicBoolean(false);

        try {

            projectStatusService.sendDeploymentStep(project, DeploymentStep.ZIP_PATH_CHECK);
            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.ZIP_PATH_CHECK);

            projectStatusService.sendDeploymentStep(project, DeploymentStep.DOCKERFILE_EXTRACT);
            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.DOCKERFILE_EXTRACT);
            Path dockerfilePath = extractDockerfileFromZip(project.getApplication().getImageId(), project.getName());

            projectStatusService.sendDeploymentStep(project, DeploymentStep.DOCKER_BUILD);
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
                        projectStatusService.sendDeploymentStep(project, DeploymentStep.NGINX_CONFIG);

                        nginxService.generateNginxConf(applicationName, applicationPort);

                        System.out.println("Docker image built successfully: " + imageId);
                        deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.SUCCESS);
                        projectStatusService.sendDeploymentStep(project, DeploymentStep.SUCCESS);
                        buildSuccess.set(true);
                    });
        } catch (IOException e) {
            e.printStackTrace();
            deploymentStepQueue.addDeploymentUpdate(project, DeploymentStep.FAILED);
            projectStatusService.sendDeploymentStep(project, DeploymentStep.FAILED);

            throw new BadRequestException("Failed to extract Dockerfile from ZIP", ErrorCode.FAILED_PROJECT_ERROR);
        }
        return buildSuccess.get();
    }

    private void createContainer(String token, ProjectEntity project, String imageId) {
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        CreateContainerResponse container;

        // 프로젝트를 생성하는데 백엔드일경우에만 isNotNullEnvKey Service 를 탄다.
        if (project.getApplication().getVariableKey() == null || project.getProjectType().name().equals("FRONT")) { // 키값의 유무에 따름
            container = isNullEnvKey(project, imageId);
        } else {
            container = isNotNullEnvKey(project, imageId);
        }

        dockerClient.startContainerCmd(container.getId()).exec();

        ContainerEntity containerEntity = ContainerEntity.builder()
                .application(project.getApplication())
                .containerId(container.getId())
                .imageId(project.getName() + ":" + project.getApplication().getTag())
                .user(userPk)
                .build();

        containerRepository.save(containerEntity);
    }

    private CreateContainerResponse isNullEnvKey(ProjectEntity project, String imageId) {
        return dockerClient.createContainerCmd(imageId)
                .withName(project.getName())
                .withExposedPorts(ExposedPort.tcp(project.getApplication().getOuterPort()))
                .withHostConfig(newHostConfig()
                        .withPortBindings(new PortBinding(
                                Ports.Binding.bindPort(project.getApplication().getOuterPort()),
                                ExposedPort.tcp(project.getApplication().getOuterPort())
                        )))
                .withImage(imageId)
                .exec();
    }

    private CreateContainerResponse isNotNullEnvKey(ProjectEntity project, String imageId) {
        return dockerClient.createContainerCmd(imageId)
                .withName(project.getName())
                .withExposedPorts(ExposedPort.tcp(project.getApplication().getOuterPort()))
                .withEnv("JASYPT_ENCRYPTOR_PASSWORD=" + project.getApplication().getVariableKey())
                .withHostConfig(newHostConfig()
                        .withPortBindings(new PortBinding(
                                Ports.Binding.bindPort(project.getApplication().getOuterPort()),
                                ExposedPort.tcp(project.getApplication().getOuterPort())
                        )))
                .withImage(imageId)
                .exec();
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
                        taggingImage(item.getImageId(), name, tag);

                        ImageBuildEventRequestDto event = new ImageBuildEventRequestDto(userPk, item.getImageId(), name, key);
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

    public void updateProjectApprovalFixedState(ProjectEntity project) {
        project.approveFixedProject();
        projectRepository.save(project);
    }

    private void updateProjectApprovalState(ProjectEntity project) {
        project.approveCreateProject();
        projectRepository.save(project);
    }

}