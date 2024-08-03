package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.DockerRequestException;
import gamza.project.gamzaweb.Repository.ContainerRepository;
import gamza.project.gamzaweb.Repository.ImageRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
@Component
@RequiredArgsConstructor
public class DockerProvider {

    //don't be use this value in out of class
    private final DockerClient dockerClient = DockerDataStore.getInstance().getDockerClient();
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ContainerRepository containerRepository;

    public List<Container> getContainerList() {
        return dockerClient.listContainersCmd().exec();
    }

    public List<Image> getImageList() {
        return dockerClient.listImagesCmd().exec();
    }

    /**
     * Make Automatically image build and start process
     * */
//    public String imageRegister(File dockerFile, String projectName, String versionName, String outerPort, String innerPort) throws Exception {
//        if (!dockerFile.exists()) {
//            throw new NotFoundException("Docker File Not Exist");
//        }
////        if (scheduler == null) {
////            throw new NullPointerException("Scheduler is null");
////        }
//
//        String tempImageId = ""; //todo : make random and push to db
//
//        buildImage(dockerFile, new BuildImageResultCallback() {
//            @Override
//            public void onNext(BuildResponseItem item) {
//                super.onNext(item);
//                if (item.getImageId() == null) {
//                    return;
//                }
//                //todo : update temp id to real id
//                taggingImage(item.getImageId(), projectName, versionName);
//
//                String containerId = createContainer(projectName, outerPort, innerPort, versionName);
//                dockerClient.startContainerCmd(containerId).exec();
//            }
//        });
//
//        return ""; // todo : return db id
//    }

    public void buildImage(File file, BuildImageResultCallback callback) {
        System.out.println("buildImage : " + file.exists() + "/" + file.length());

        BuildImageCmd image = dockerClient.buildImageCmd(file);
        image.exec(callback);
    }

    public void taggingImage(String imageId, String name, String tag) {
        dockerClient.tagImageCmd(imageId, name, tag).exec();
    }

    public void buildImage(HttpServletRequest request, File file, String name, @Nullable String tag, @Nullable String key, DockerProviderBuildCallback callback) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        if (name != null && tag != null) {
            List<Image> existingImages = dockerClient.listImagesCmd().exec();
            boolean imageExists = existingImages.stream()
                    .anyMatch(image -> image.getRepoTags() != null &&
                            Arrays.asList(image.getRepoTags()).contains(name + ":" + tag));

            if (imageExists) {
                throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
            }
        }

        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(file);

        if (key != null && !key.isEmpty()) {
            buildImageCmd.withBuildArg("key", key);
        }

        try {
            buildImageCmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    super.onNext(item);
                    System.out.println("onNext: " + item.getImageId());
                    if (item.getImageId() != null) {
                        taggingImage(item.getImageId(), name, tag);
                        callback.getImageId(item.getImageId());
                    }
                    ImageEntity imageEntity = ImageEntity.builder()
                            .imageId(item.getImageId())
                            .user(userPk)
                            .variableKey(key)
                            .build();

                    imageRepository.save(imageEntity);
                }
            });
        } catch (Exception e) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }

    }

    public String createContainer(RequestDockerContainerDto dto, HttpServletRequest request) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        ExposedPort tcpOuter = ExposedPort.tcp(dto.getInternalPort());
        Ports portBindings = new Ports();
        portBindings.bind(tcpOuter, Ports.Binding.bindPort(dto.getOuterPort()));

        // create container from image
        try {

            CreateContainerResponse container = dockerClient.createContainerCmd(dto.getName())
                    .withExposedPorts(tcpOuter)
                    .withHostConfig(newHostConfig()
                            .withPortBindings(portBindings))
                    .withImage(dto.getName() + ":" + dto.getTag())
                    .exec();

            // start the container
            dockerClient.startContainerCmd(container.getId()).exec();

            ContainerEntity containerEntity = ContainerEntity.builder()
                    .containerId(container.getId()) // ?
                    .imageId(dto.getName() + ":" + dto.getTag())
                    .user(userPk)
                    .build();

            containerRepository.save(containerEntity);

            return container.getId();
        } catch (Exception e) {
            throw new DockerRequestException("3005 FAILED CONTAINER BUILD", ErrorCode.FAILED_CONTAINER_BUILD);
        }
    }

    public void stopContainer(String containerId) {
        try {
            StopContainerCmd stopContainer = dockerClient.stopContainerCmd(containerId);
            stopContainer.exec();
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

    public void removeImage(String imageId) {
        RemoveImageCmd removeCmd = dockerClient.removeImageCmd(imageId);
        removeCmd.withForce(true); //check :: is force enabled??
        removeCmd.exec();
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
        void getImageId(String imageId);
    }


    public interface innerDockerProviderBuildCallback {
        void closeFunction();
    }

    //examples end---


}