package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

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
}
