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
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
public class DockerProvider {

    //don't be use this value in out of class
    public final DockerClient dockerClient;
    //    DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//            .withDockerHost("tcp://localhost:2376")
//            .withDockerTlsVerify(true)
//            .withDockerCertPath("/opt/homebrew/Cellar/docker/27.0.3")
//            .withRegistryUsername(registryUser)
//            .withRegistryPassword(registryPass)
//            .withRegistryEmail(registryMail)
//            .withRegistryUrl(registryUrl)
//            .build();

    private static DockerProvider instance;
    private DockerScheduler scheduler;

    public void setScheduler(DockerScheduler scheduler) {
        this.scheduler = scheduler;
    }

    private DockerProvider() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public static synchronized DockerProvider getInstance() {
        if (instance == null) {
            instance = new DockerProvider();
        }
        return instance;
    }

    public List<Container> getContainerList() {
        return dockerClient.listContainersCmd().exec();
    }

    public List<Image> getImageList() {
        return dockerClient.listImagesCmd().exec();
    }

    /**
     * Make Automatically image build and start process
     * */
    public String imageRegister(File dockerFile, String projectName, String versionName, String outerPort, String innerPort) throws Exception {
        if (!dockerFile.exists()) {
            throw new NotFoundException("Docker File Not Exist");
        }
        if (scheduler == null) {
            throw new NullPointerException("Scheduler is null");
        }

        String tempImageId = ""; //todo : make random and push to db

        buildImage(dockerFile, new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                super.onNext(item);
                if (item.getImageId() == null) {
                    return;
                }
                //todo : update temp id to real id
                taggingImage(item.getImageId(), projectName, versionName);

                String containerId = createContainer(projectName, outerPort, innerPort, versionName);
                //todo : input container id
                dockerClient.startContainerCmd(containerId).exec();
            }
        });

        return ""; // todo : return db id
    }



    public void buildImage(File file, String name, @Nullable String tag, @Nullable String jasyptKey, DockerProviderBuildCallback callback) {
        BuildImageCmd buildImageCmd = getDockerClient().buildImageCmd(file);

        if (jasyptKey != null && !jasyptKey.isEmpty()) {
            buildImageCmd.withBuildArg("JASYPT_KEY", jasyptKey);
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
            }
        });
    }
//    public void buildImage(File file, String name, @Nullable String tag, DockerProviderBuildCallback callback) {
//        buildImage(file, new BuildImageResultCallback() {
//            @Override
//            public void onNext(BuildResponseItem item) {
//                super.onNext(item);
//                System.out.println("onNext: " + item.getImageId());
//                if (item.getImageId() != null) {
//                    taggingImage(item.getImageId(), name, tag);
//                    callback.getImageId(item.getImageId());
//                }
//            }
//        });
//    }

    public void buildImage(File file, BuildImageResultCallback callback) {
        System.out.println("buildImage : " + file.exists() + "/" + file.length());

        BuildImageCmd image = dockerClient.buildImageCmd(file);
        image.exec(callback);
    }

    public void taggingImage(String imageId, String name, String tag) {
        dockerClient.tagImageCmd(imageId, name, tag).exec();
    }

    public String createContainer(String name, String outerPort, String innerPort, String tag) {
        ExposedPort tcpOuter = ExposedPort.tcp(Integer.parseInt(innerPort));
        Ports portBindings = new Ports();
        portBindings.bind(tcpOuter, Ports.Binding.bindPort(Integer.parseInt(outerPort)));

        CreateContainerResponse container = dockerClient.createContainerCmd(name)
                .withExposedPorts(tcpOuter)
                .withHostConfig(newHostConfig()
                        .withPortBindings(portBindings))
                .withImage(name + ":" + tag)
                .exec();

        return container.getId();
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