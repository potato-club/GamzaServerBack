package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDeleteDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDeleteDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDto;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/doc")
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequiredArgsConstructor
public class DockerController {

    private final DockerProvider provider;

//    @GetMapping("/list") // 도커리스트 출력
//    @Operation(description = "도커 리스트 출력")
//    public String dockerList(HttpServletRequest request) {
//        return provider.listContainers(request);
//    }
//
//    @PostMapping("/removeImage")
//    @Operation(description = "이미지 삭제 (추후 삭제될 API 스케줄러를 사용하여 자동화될 예정)")
//    public ResponseEntity<String> removeImage(@RequestBody RequestDockerImageDeleteDto dto, HttpServletRequest request) {
//        provider.removeImage(dto.getImageId(), request);
//        return ResponseEntity.status(HttpStatus.OK).body("Success Delete Image");
//    }
//
//    @PostMapping("/create/container")
//    @Operation(description = "컨테이너 생성(추후 삭제될 API 스케줄러를 사용하여 자동화될 예정)")
//    public String create(@RequestBody RequestDockerContainerDto requestDockerContainerDto, HttpServletRequest request) {
//        return provider.createContainer(requestDockerContainerDto, request);
//    }
//
//    @PostMapping("/stopContainer") // 완료
//    @Operation(description = "컨테이너 스탑(추후 삭제될 API 스케줄러를 사용하여 자동화될 예정)")
//    public ResponseEntity<String> stopContainer(@RequestBody RequestDockerContainerDeleteDto dto, HttpServletRequest request) {
//        provider.stopContainer(request, dto.getContainerId());
//        return ResponseEntity.status(HttpStatus.OK).body("Container Stop And Delete in DB");
//    }

    @GetMapping("/logs/{containerId}")
    @Operation(description = "컨테이너 로그")
    public List<String> getContainerLogs(@PathVariable("containerId") String containerId,
                                         @RequestParam(value = "lines", defaultValue = "100") int lines) {
        return provider.getContainerLogs(containerId, lines);
    }

//    @GetMapping("/update/nginx")
//    public String updateNginxConfig(@RequestParam("containerId") String containerId,
//                                    @RequestParam("port") String port,
//                                    @RequestParam("cname") String cname) {
//        return provider.updateNginxConfig(containerId, port, cname);
//    }

//    @GetMapping("/stopContainer")
//    public ResponseEntity<String> stopContainer(@RequestParam("id") String id) {
//        //https://camel-context.tistory.com/20
//        CountDownLatch latch = new CountDownLatch(1);
//
//        final String[] result = new String[1];
//
//        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
//        CompletableFuture.runAsync(() -> {
//            dockerScheduler.addContainerCheckList(id, new DockerScheduler.ContainContainerCallBack() {
//                @Override
//                public void containerCheckResult(boolean checkResult, Container container) {
//                    try {
//                        dockerScheduler.removeImageCheckList(id);
//                        result[0] = container != null ? container.getStatus() : "null Container";
//                        latch.countDown(); // 비동기 작업 완료를 알림
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        });
//
//        provider.stopContainer(id);
//
//        try {
//            latch.await(); // 비동기 작업 완료 대기
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error occurred during container check.");
//        }
//
//        return ResponseEntity.ok("Container check status for ID " + id + ": " + result[0]);
//    }


}