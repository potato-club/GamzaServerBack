package gamza.project.gamzaweb.Controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.List;


//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

@RestController
@RequestMapping("/doc")
public class DockerTestController {

    @GetMapping("/list")
    public String something() {
        DockerProvider provider = new DockerProvider();
        DockerClient dockerClient = provider.getDockerClient();

        List<Container> containers = dockerClient.listContainersCmd().exec();
        StringBuilder sb = new StringBuilder();
        sb.append("containers.size() : ").append(containers.size()).append("\n");
        for (Container container : containers) {
            sb.append(container.toString());
        }



        return sb.toString();
    }
}
