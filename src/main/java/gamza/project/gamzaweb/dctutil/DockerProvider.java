package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.annotation.Nullable;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public void buildImage(File file, String name, @Nullable String tag, DockerProviderBuildCallback callback) {
        buildImage(file, new BuildImageResultCallback() {
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

        dockerClient.startContainerCmd(container.getId()).exec();
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

    public interface DockerProviderBuildCallback {
        void getImageId(String imageId);
    }
}

