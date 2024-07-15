package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import jakarta.annotation.Nullable;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md
//https://docs.docker.com/engine/api/v1.45/#tag/Image/operation/BuildPrune
public class DockerProvider {

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


    //examples start ---

//    public String getJarPath() throws Exception {
//        return new File(DockerProvider.class.getProtectionDomain().getCodeSource().getLocation()
//                .toURI()).getPath();
//    }

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

    public String createContainer(String name, String outerPort, String innerPort, String tag) {
        ExposedPort tcpOuter = ExposedPort.tcp(Integer.parseInt(innerPort));
        Ports portBindings = new Ports();
        portBindings.bind(tcpOuter, Ports.Binding.bindPort(Integer.parseInt(outerPort)));

        // create container from image
        CreateContainerResponse container = getDockerClient().createContainerCmd(name)
                .withExposedPorts(tcpOuter)
                .withHostConfig(newHostConfig()
                        .withPortBindings(portBindings))
                .withImage(name + ":" + tag)
                .exec();

        // start the container
        getDockerClient().startContainerCmd(container.getId()).exec();
        return container.getId();
    }

    public void buildImage(File file, BuildImageResultCallback callback) {
        System.out.println("buildImage : " + file.exists() + "/" + file.length());

        BuildImageCmd image = getDockerClient().buildImageCmd(file);
        image.exec(callback);
    }

    public void taggingImage(String imageId, String name, String tag) {
        getDockerClient().tagImageCmd(imageId, name, tag).exec();
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

