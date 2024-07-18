package gamza.project.gamzaweb.Controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

@RestController
@RequestMapping("/doc")
public class DockerTestController {

    DockerProvider provider = new DockerProvider();
    DockerClient dockerClient = provider.getDockerClient();

    @GetMapping("/list")
    public String something() {
        List<Container> containers = dockerClient.listContainersCmd().exec();
        StringBuilder sb = new StringBuilder();
        sb.append("containers.size() : ").append(containers.size()).append("\n");
        for (Container container : containers) {
            sb.append(container.toString());
        }

        return sb.toString();
    }


    @GetMapping("/buildImage")
    public String buildImage(@RequestParam String name, @RequestParam String tag) {
        CompletableFuture<String> result = new CompletableFuture<>();
        provider.buildImage(
                new File("/Users/isaacjang/Documents/test-nginx/Dockerfile"),
                name,
                tag,
                new DockerProvider.DockerProviderBuildCallback() {
                    @Override
                    public void getImageId(String imageId) {
                        result.complete(imageId);
                    }
                }
        );

        try {
            String imageId = result.get();

            return imageId;
        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            return e.getLocalizedMessage();
        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }


    @GetMapping("/create")
    public String create() {
        return provider.createContainer("test_name", "8082", "80", "1.0.0");
    }
}
