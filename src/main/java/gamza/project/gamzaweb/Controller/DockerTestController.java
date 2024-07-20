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
import org.springframework.web.bind.annotation.*;

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
    public String buildImage(@RequestParam("name") String name, @RequestParam("tag") String tag) {
        CompletableFuture<String> result = new CompletableFuture<>();
        provider.buildImage(
                new File("/Users/kimseonghun/Desktop/docker/Dockerfile"),
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

    @GetMapping("/logs/{containerId}")
    public List<String> getContainerLogs(@PathVariable("containerId") String containerId,
                                         @RequestParam("lines") int lines) {
        return provider.getContainerLogs(containerId, lines);
    }

//    @GetMapping("/update/nginx") // 피그마에 엔진엑스 conf 파일 수정 내용이 왜있지?
//    public String updateNginxConfig(@RequestParam("containerId") String containerId,
//                                    @RequestParam("port") String port,
//                                    @RequestParam("cname") String cname) {
//        return provider.updateNginxConfig(containerId, port, cname);
//    }


    @GetMapping("/create")
    public String create() {
        return provider.createContainer("test_name", "8082", "80", "1.0.0");
    }
    @GetMapping("/update")
    public String update() {
        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
        //3c74c4202325
        return provider.createContainer("test_name", "8082", "80", "1.0.0");
    }
}
