package gamza.project.gamzaweb.Controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.sun.jna.WString;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDto;
import gamza.project.gamzaweb.dctutil.DockerDataStore;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import gamza.project.gamzaweb.dctutil.DockerScheduler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

@RestController
@RequestMapping("/doc")
@CrossOrigin(originPatterns = "http://localhost:3000, localhost:3000")
public class DockerTestController {

    @Autowired
    DockerProvider provider;
    @Autowired
    DockerScheduler dockerScheduler;
    @Autowired
    private DockerProvider dockerProvider;

    @PostMapping("/buildImage") /// 변수만 수정
    public String buildImage(@RequestBody RequestDockerImageDto dto, HttpServletRequest request) {

//        File dockerfile = new File("/Users/kimseonghun/Desktop/docker/Dockerfile"); // 성훈 테스트 환경 pull 받을시 수정 요망
        File dockerfile = new File(dto.getDockerfilePath());
        CompletableFuture<String> result = new CompletableFuture<>();

        provider.buildImage(request, dockerfile, dto.getName(), dto.getTag(), dto.getKey(), new DockerProvider.DockerProviderBuildCallback() {
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

//    @GetMapping("/update/nginx") // 피그마에 엔진엑스 conf 파일 수정 내용이 왜있지?
//    public String updateNginxConfig(@RequestParam("containerId") String containerId,
//                                    @RequestParam("port") String port,
//                                    @RequestParam("cname") String cname) {
//        return provider.updateNginxConfig(containerId, port, cname);
//    }


    @PostMapping("/create")
    public String create(@RequestBody RequestDockerContainerDto requestDockerContainerDto, HttpServletRequest request) {
        return provider.createContainer(requestDockerContainerDto, request);
    }

    @GetMapping("/removeImage")
    public String removeImage(@RequestParam("id") String id) {
        provider.removeImage(id);
        return "removed id : " + id;
    }

    @GetMapping("/removeImageCheck")
    public ResponseEntity<String> removeImageCheck(@RequestParam("id") String id) {
        //https://camel-context.tistory.com/20
        CountDownLatch latch = new CountDownLatch(1);

        final boolean[] result = new boolean[1];

        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
        CompletableFuture.runAsync(() -> {
            dockerScheduler.addImageCheckList(id, new DockerScheduler.ContainImageCallBack() {
                @Override
                public void containerCheckResult(boolean checkResult, Image image) {
                    dockerScheduler.removeImageCheckList(id);
                    /**
                     * 못찾으면 삭제된 것 ( 혹은 잘못넣은 id 일지도?)
                     * 실제 api는 선행적으로 리스트 체크가 필요
                     * provider.getContainerList() or imageList
                     * */
                    result[0] = !checkResult;

                    latch.countDown(); // 비동기 작업 완료를 알림
                }
            });
        });

        provider.removeImage(id);

        try {
            latch.await(); // 비동기 작업 완료 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred during image check.");
        }

        return ResponseEntity.ok("Image check result for ID " + id + ": " + (result[0] ? "success" : "failure"));
    }


    @GetMapping("/removeContainerCheck")
    public ResponseEntity<String> removeContainer(@RequestParam("id") String id) {
        //https://camel-context.tistory.com/20
        CountDownLatch latch = new CountDownLatch(1);

        final boolean[] result = new boolean[1];

        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
        CompletableFuture.runAsync(() -> {
            dockerScheduler.addContainerCheckList(id, new DockerScheduler.ContainContainerCallBack() {
                @Override
                public void containerCheckResult(boolean checkResult, Container container) {
                    try {
                        dockerScheduler.removeImageCheckList(id);
                        /**
                         * 못찾으면 삭제된 것 ( 혹은 잘못넣은 id 일지도?)
                         * 실제 api는 선행적으로 리스트 체크가 필요
                         * provider.getContainerList() or imageList
                         * */
                        result[0] = !checkResult;

                        latch.countDown(); // 비동기 작업 완료를 알림
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        provider.removeContainer(id);

        try {
            latch.await(); // 비동기 작업 완료 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred during container check.");
        }

        return ResponseEntity.ok("Container check result for ID " + id + ": " + (result[0] ? "success" : "failure"));
    }

    @GetMapping("/stopContainer")
    public ResponseEntity<String> stopContainer(@RequestParam("id") String id) {
        //https://camel-context.tistory.com/20
        CountDownLatch latch = new CountDownLatch(1);

        final String[] result = new String[1];

        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
        CompletableFuture.runAsync(() -> {
            dockerScheduler.addContainerCheckList(id, new DockerScheduler.ContainContainerCallBack() {
                @Override
                public void containerCheckResult(boolean checkResult, Container container) {
                    try {
                        dockerScheduler.removeImageCheckList(id);
                        result[0] = container != null ? container.getStatus() : "null Container";
                        latch.countDown(); // 비동기 작업 완료를 알림
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        provider.stopContainer(id);

        try {
            latch.await(); // 비동기 작업 완료 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred during container check.");
        }

        return ResponseEntity.ok("Container check status for ID " + id + ": " + result[0]);
    }


//    @GetMapping("/update")
//    public String update() {
//        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
//        //3c74c4202325
//        return provider.createContainer("test_name", "8082", "80", "1.0.0");
//    }

    @GetMapping("/checker")
    public String checker() {
        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
        //3c74c4202325
//        return provider.createContainer("test_name", "8082", "80", "1.0.0");
        System.out.println("is null ? : " + (DockerDataStore.getInstance().getDockerClient() == null));
        System.out.println("is null ? : " + (dockerProvider == null));
        System.out.println("is null ? : " + (dockerScheduler == null));
        return "return";
    }
}