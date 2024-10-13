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
import gamza.project.gamzaweb.Dto.docker.ImageBuildEventDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDto;
import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.DockerRequestException;
import gamza.project.gamzaweb.Error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.Repository.ContainerRepository;
import gamza.project.gamzaweb.Repository.ImageRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

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
//    private final DockerScheduler dockerScheduler;

    public List<Container> getContainerList() {
        return dockerClient.listContainersCmd().exec();
    }

    public List<Image> getImageList() {
        return dockerClient.listImagesCmd().exec();
    }

    /**
     * Make Automatically image build and start process
     */
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

//    public void buildImage(File file, BuildImageResultCallback callback) {
//        System.out.println("buildImage : " + file.exists() + "/" + file.length());
//
//        BuildImageCmd image = dockerClient.buildImageCmd(file);
//        image.exec(callback);
//    }
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

    // 이미지를 빌드를 한다 -> 도커 스케쥴러에서 이미지가 있는지 체크한다 -> 이미지 빌드가 다되면 스케쥴러에서 디비에 넣어준다. 유저는 어떻게 넣지
    public void buildImage(HttpServletRequest request, File file, String name, @Nullable String tag, @Nullable String key, DockerProviderBuildCallback callback) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        ImageEntity imageEntity = ImageEntity.builder()
                .user(userPk)
                .name(name)
                .variableKey(key)
                .build();
        imageRepository.save(imageEntity);

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
                    System.out.println("error: " + item.isErrorIndicated()); // 이거던져주면됨 error
                    if (item.getImageId() != null) {
                        taggingImage(item.getImageId(), name, tag);
                        callback.getImageId(item.getImageId());

                        ImageBuildEventDto event = new ImageBuildEventDto(
                                userPk, item.getImageId(), name, key
                        );
                        applicationEventPublisher.publishEvent(event);
                    }
                }
            });
        } catch (Exception e) {
            throw new DockerRequestException("3001 FAILED IMAGE BUILD", ErrorCode.FAILED_IMAGE_BUILD);
        }

    }

    public Path unzip(MultipartFile file, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newPath = zipSlipProtect(zipEntry, destDir); // 보안 검사를 통해 zip 파일 내 경로 검사
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    // 파일 해제
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        return destDir;
    }

    // zip 파일 내부 경로 보안을 위한 메서드
    private Path zipSlipProtect(ZipEntry zipEntry, Path destDir) throws IOException {
        Path targetDirResolved = destDir.resolve(zipEntry.getName());
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(destDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
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

    public void stopContainer(HttpServletRequest request, String containerId) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        if (userPk == null) {
            throw new UnAuthorizedException("401 ERROR USER NOT FOUNT ", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Optional<ContainerEntity> userContainer = containerRepository.findByContainerIdAndUser(containerId, userPk);
        if (userContainer.isEmpty()) {
            throw new DockerRequestException("3006 FAILED CONTAINER STOP", ErrorCode.FAILED_CONTAINER_STOP);
        }

        try {
            StopContainerCmd stopContainer = dockerClient.stopContainerCmd(containerId);
            stopContainer.exec();
            containerRepository.delete(userContainer.get());
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
        void getImageId(String imageId);
    }


    public interface innerDockerProviderBuildCallback {
        void closeFunction();
    }

    //examples end---


}