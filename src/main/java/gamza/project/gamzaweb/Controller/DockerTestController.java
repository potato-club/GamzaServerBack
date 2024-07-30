package gamza.project.gamzaweb.Controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Service.docker.DockerProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

@RestController
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DockerTestController {

    private final DockerProvider provider;

    @GetMapping("/list") // 도커리스트 출력
    public String something() {
        DockerClient dockerClient = provider.getDockerClient();
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
            @RequestParam(value = "key", required = false) String key,
            HttpServletRequest request) {

        File dockerfile = new File("/Users/kimseonghun/Desktop/docker/Dockerfile"); // 성훈 테스트 환경 pull 받을시 수정 요망

        CompletableFuture<String> result = new CompletableFuture<>();

        provider.buildImage(request, dockerfile, name, tag, key, new DockerProvider.DockerProviderBuildCallback() {
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

    @GetMapping("/logs/{containerId}")
    public List<String> getContainerLogs(@PathVariable("containerId") String containerId,
                                         @RequestParam(value = "lines", defaultValue = "100") int lines) {
        return provider.getContainerLogs(containerId, lines);
    }

//    @PostMapping("/update/nginx") // 미작동
//    public String updateNginxConfig(@RequestBody NginxConfigDto configDto) {
//        return provider.updateNginxConfig(configDto.getContainerId(), configDto.getPort(), configDto.getCname());
//    }


    //"test_name", "8082", "80", "1.0.0"
    @PostMapping("/create")
    public String create(@RequestBody RequestDockerContainerDto requestDockerContainerDto, HttpServletRequest request) {
        return provider.createContainer(requestDockerContainerDto, request);
    }

//    @GetMapping("/update")
//    public String update() {
//        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
//        //3c74c4202325
//        return provider.createContainer("test_name", "8082", "80", "1.0.0");
//    }
}