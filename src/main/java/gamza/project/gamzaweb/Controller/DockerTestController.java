package gamza.project.gamzaweb.Controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import gamza.project.gamzaweb.Dto.NginxConfigDto;
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

    @GetMapping("/buildImage") /// 변수만 수정
    public String buildImage(
            @RequestParam("name") String name,
            @RequestParam("tag") String tag,
            @RequestParam(value = "jasyptKey", required = false) String jasyptKey) {

        File dockerfile = new File("/Users/kimseonghun/Desktop/docker/Dockerfile");

        CompletableFuture<String> result = new CompletableFuture<>();

        provider.buildImage(dockerfile, name, tag, jasyptKey, new DockerProvider.DockerProviderBuildCallback() {
            @Override
            public void getImageId(String imageId) {
                result.complete(imageId);
            }
        });

        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

//    @GetMapping("/buildImage")
//    public String buildImage(
//            @RequestParam("name") String name,
//            @RequestParam("tag") String tag,
//            @RequestParam(value = "jasyptKey", required = false) String jasyptKey) {
//        CompletableFuture<String> result = new CompletableFuture<>();
//        provider.buildImage(
//                new File("/Users/kimseonghun/Desktop/docker/Dockerfile"),
//                name,
//                tag,
//                new DockerProvider.DockerProviderBuildCallback() {
//                    @Override
//                    public void getImageId(String imageId) {
//                        result.complete(imageId);
//                    }
//                }
//        );
//
//        try {
//            String imageId = result.get();
//
//            return imageId;
//        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
//            e.printStackTrace();
//            return e.getLocalizedMessage();
//        } catch (ExecutionException e) {
////            throw new RuntimeException(e);
//            e.printStackTrace();
//            return e.getLocalizedMessage();
//        }
//    }

    @GetMapping("/logs/{containerId}")
    public List<String> getContainerLogs(@PathVariable("containerId") String containerId,
                                         @RequestParam(value = "lines", defaultValue = "100") int lines) {
        return provider.getContainerLogs(containerId, lines);
    }

    @PostMapping("/update/nginx")
    public String updateNginxConfig(@RequestBody NginxConfigDto configDto) {
        return provider.updateNginxConfig(configDto.getContainerId(), configDto.getPort(), configDto.getCname());
    }


    @GetMapping("/create")
    public String create() {
        return provider.createContainer("test_name2", "8083", "80", "1.0.0");
    }
    @GetMapping("/update")
    public String update() {
        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
        //3c74c4202325
        return provider.createContainer("test_name", "8082", "80", "1.0.0");
    }
}