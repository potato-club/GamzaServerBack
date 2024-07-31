package gamza.project.gamzaweb.Service.docker;

import com.github.dockerjava.api.DockerClient;
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
import gamza.project.gamzaweb.Error.requestError.BusinessException;
import gamza.project.gamzaweb.Error.requestError.DockerRequestException;
import gamza.project.gamzaweb.Repository.ContainerRepository;
import gamza.project.gamzaweb.Repository.ImageRepository;
import gamza.project.gamzaweb.Repository.UserRepository;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
@Service
@RequiredArgsConstructor
public class DockerProvider {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ContainerRepository containerRepository;
    private final ImageRepository imageRepository;

    public DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
//    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//            .withDockerHost("tcp://localhost:2376")
//            .withDockerTlsVerify(true)
//            .withDockerCertPath("/opt/homebrew/Cellar/docker/27.0.3")
//            .withRegistryUsername(registryUser)
//            .withRegistryPassword(registryPass)
//            .withRegistryEmail(registryMail)
//            .withRegistryUrl(registryUrl)
//            .build();

    public DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

    public DockerClient getDockerClient() {
        return DockerClientImpl.getInstance(config, httpClient);
    }

    public void buildImage(HttpServletRequest request, File file, String name, @Nullable String tag, @Nullable String key, DockerProviderBuildCallback callback) {

        String token = jwtTokenProvider.resolveAccessToken(request);
        Long userId = jwtTokenProvider.extractId(token);
        UserEntity userPk = userRepository.findUserEntityById(userId);

        BuildImageCmd buildImageCmd = getDockerClient().buildImageCmd(file);

        if (key != null && !key.isEmpty()) {
            buildImageCmd.withBuildArg("key", key);
        }

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
                        .key(key)
                        .build();

                imageRepository.save(imageEntity);
            }
        });


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

            CreateContainerResponse container = getDockerClient().createContainerCmd(dto.getName())
                    .withExposedPorts(tcpOuter)
                    .withHostConfig(newHostConfig()
                            .withPortBindings(portBindings))
                    .withImage(dto.getName() + ":" + dto.getTag())
                    .exec();

            // start the container
            getDockerClient().startContainerCmd(container.getId()).exec();

            ContainerEntity containerEntity = ContainerEntity.builder()
                    .containerId(container.getId()) // ?
                    .imageId(dto.getName() + ":" + dto.getTag())
                    .user(userPk)
                    .build();

            containerRepository.save(containerEntity);

            return container.getId();
        } catch (DockerRequestException e) {
            throw new DockerRequestException("3005 FAILED CONTAINER BUILD", ErrorCode.FAILED_CONTAINER_BUILD);
        }
    }

    public void taggingImage(String imageId, String name, String tag) {
        getDockerClient().tagImageCmd(imageId, name, tag).exec();
    }

    public List<String> getContainerLogs(String containerId, int lines, HttpServletRequest request) {
        List<String> logs = new ArrayList<>();

        try (LogContainerCmd logContainerCmd = getDockerClient().logContainerCmd(containerId)) {
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



    public String updateNginxConfig(String containerId, String port, String cname) {

        if (isContainerRunning(containerId)) {
            try {
                // Delete the existing nginx.conf file
                getDockerClient().execCreateCmd(containerId)
                        .withCmd("rm", "-f", "/etc/nginx/nginx.conf")
                        .exec();
//                getDockerClient().execStartCmd(containerId).exec());

                String configContent = generateNginxConfig(port, cname);

                Path tempFile = Files.createTempFile("nginx", ".conf");
                Files.write(tempFile, configContent.getBytes());

                // Copy the new nginx.conf to the container
                getDockerClient().copyArchiveToContainerCmd(containerId)
                        .withHostResource(tempFile.toString())
                        .withRemotePath("/etc/nginx/nginx.conf")
                        .exec();

                // Restart the container to apply the new configuration
                getDockerClient().restartContainerCmd(containerId).exec();

                // Delete the temporary file
                Files.delete(tempFile);

                return "Nginx config updated and container restarted.";
            } catch (IOException e) {
                e.printStackTrace();
                return "Error updating Nginx config: " + e.getMessage();
            }
        } else {
            return "Container is not running.";
        }
    }

    private String generateNginxConfig(String port, String cname) {
        return """
                server {
                    listen %s;
                    server_name %s;

                    location / {
                        proxy_pass http://localhost:8080;
                        proxy_set_header Host $host;
                        proxy_set_header X-Real-IP $remote_addr;
                        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                        proxy_set_header X-Forwarded-Proto $scheme;
                    }
                }
                """.formatted(port, cname);
    }

    private boolean isContainerRunning(String containerId) {
        return Boolean.TRUE.equals(getDockerClient().inspectContainerCmd(containerId)
                .exec()
                .getState()
                .getRunning());
    }


    //example interface ---

    public interface DockerProviderBuildCallback {
        void getImageId(String imageId);
    }

    public interface innerDockerProviderBuildCallback {
        void closeFunction();
    }

    //examples end---


}