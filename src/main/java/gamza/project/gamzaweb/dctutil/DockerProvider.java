package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;
import gamza.project.gamzaweb.error.requestError.DockerRequestException;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.ContainerRepository;
import gamza.project.gamzaweb.repository.ImageRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import gamza.project.gamzaweb.validate.ProjectValidate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.List;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
@Component
@RequiredArgsConstructor
@Slf4j
public class DockerProvider {

    //don't be use this value in out of class
    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ContainerRepository containerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectValidate projectValidate;

    public List<Container> getContainerList() {
        return dockerClient.listContainersCmd().exec();
    }

    public List<Image> getImageList() {
        return dockerClient.listImagesCmd().exec();
    }

    public String listContainers(HttpServletRequest request) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);

        List<String> containerIds = containerRepository.findContainerIdsByUserId(userId);

        if (containerIds.isEmpty()) {
            return "You don't have any containers :(";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("containers.size() : ").append(containerIds.size()).append("\n");
        for (String containerId : containerIds) {
            sb.append(containerId).append("\n");
        }
        return sb.toString();
    }

    public void taggingImage(String imageId, String name, String tag) {
        dockerClient.tagImageCmd(imageId, name, tag).exec(); // 이렇게 해도 되나..?
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


}