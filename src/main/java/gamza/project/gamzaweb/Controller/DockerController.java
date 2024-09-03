package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDeleteDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerContainerDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDeleteDto;
import gamza.project.gamzaweb.Dto.docker.RequestDockerImageDto;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


//https://github.com/docker-java/docker-java/blob/main/docs/getting_started.md

@RestController
@RequestMapping("/doc")
@CrossOrigin(origins = "${cors.allowed-origins}")
@RequiredArgsConstructor
public class DockerController {

    private final DockerProvider provider;

    @GetMapping("/list") // 도커리스트 출력
    @Operation(description = "도커 리스트 출력")
    public String dockerList(HttpServletRequest request) {
        return provider.listContainers(request);
    }


    @PostMapping(value = "/buildImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "이미지 빌드")
    public String buildImage(
            @RequestPart("file") MultipartFile file, // zip 파일 수신
            @RequestPart("name") String name,        // 이미지 이름
            @RequestPart("tag") String tag,          // 태그
            @RequestPart("key") String key,
            HttpServletRequest request) {

        // 압축 파일을 임시 디렉토리에 저장
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("dockerfile-build");
            provider.unzip(file, tempDir); // 압축 파일 해제
        } catch (IOException e) {
            throw new RuntimeException("Failed to process zip file", e);
        }

        // Dockerfile 경로 설정
        File dockerfile = new File(tempDir.toFile(), "Dockerfile");
        if (!dockerfile.exists()) {
            throw new IllegalArgumentException("Dockerfile not found in the zip archive");
        }

        // 비동기 작업을 위한 CompletableFuture
        CompletableFuture<String> result = new CompletableFuture<>();

        // Docker 이미지를 빌드
        provider.buildImage(request, dockerfile, name, tag, key, new DockerProvider.DockerProviderBuildCallback() {
            @Override
            public void getImageId(String imageId) {
                result.complete(imageId);
            }
        });

        // 이미지 빌드 결과 반환
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            return e.getLocalizedMessage();
        }
    }

    @PostMapping("/removeImage")
    @Operation(description = "이미지 삭제")
    public ResponseEntity<String> removeImage(@RequestBody RequestDockerImageDeleteDto dto, HttpServletRequest request) {
        provider.removeImage(dto.getImageId(), request);
        return ResponseEntity.status(HttpStatus.OK).body("Success Delete Image");
    }

    @PostMapping("/create/container")
    @Operation(description = "컨테이너 생성")
    public String create(@RequestBody RequestDockerContainerDto requestDockerContainerDto, HttpServletRequest request) {
        return provider.createContainer(requestDockerContainerDto, request);
    }

    @PostMapping("/stopContainer") // 완료
    @Operation(description = "컨테이너 스탑")
    public ResponseEntity<String> stopContainer(@RequestBody RequestDockerContainerDeleteDto dto, HttpServletRequest request) {
        provider.stopContainer(request, dto.getContainerId());
        return ResponseEntity.status(HttpStatus.OK).body("Container Stop And Delete in DB");
    }

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


//    @GetMapping("/removeImageCheck")
//    public ResponseEntity<String> removeImageCheck(@RequestParam("id") String id) {
//        //https://camel-context.tistory.com/20
//        CountDownLatch latch = new CountDownLatch(1);
//
//        final boolean[] result = new boolean[1];
//
//        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
//        CompletableFuture.runAsync(() -> {
//            dockerScheduler.addImageCheckList(id, new DockerScheduler.ContainImageCallBack() {
//                @Override
//                public void containerCheckResult(boolean checkResult, Image image) {
//                    dockerScheduler.removeImageCheckList(id);
//                    /**
//                     * 못찾으면 삭제된 것 ( 혹은 잘못넣은 id 일지도?)
//                     * 실제 api는 선행적으로 리스트 체크가 필요
//                     * provider.getContainerList() or imageList
//                     * */
//                    result[0] = !checkResult;
//
//                    latch.countDown(); // 비동기 작업 완료를 알림
//                }
//            });
//        });
//
//        provider.removeImage(id);
//
//        try {
//            latch.await(); // 비동기 작업 완료 대기
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error occurred during image check.");
//        }
//
//        return ResponseEntity.ok("Image check result for ID " + id + ": " + (result[0] ? "success" : "failure"));
//    }


//    @GetMapping("/removeContainerCheck") // continaer 가 꺼진지 켜진지 check 값 보내주는 api로 만들면되려나?
//    @Operation(description = "컨테이너 삭제 체크")
//    public ResponseEntity<String> removeContainer(@RequestParam("id") String id) {
//        //https://camel-context.tistory.com/20
//        CountDownLatch latch = new CountDownLatch(1);
//
//        final boolean[] result = new boolean[1];
//
//        // 비동기 작업을 별도로 처리하는 CompletableFuture 생성
//        CompletableFuture.runAsync(() -> {
//            dockerScheduler.addContainerCheckList(id, new DockerScheduler.ContainContainerCallBack() {
//                @Override
//                public void containerCheckResult(boolean checkResult, Container container) {
//                    try {
//                        dockerScheduler.removeImageCheckList(id);
//                        /**
//                         * 못찾으면 삭제된 것 ( 혹은 잘못넣은 id 일지도?)
//                         * 실제 api는 선행적으로 리스트 체크가 필요
//                         * provider.getContainerList() or imageList
//                         * */
//                        result[0] = !checkResult;
//
//                        latch.countDown(); // 비동기 작업 완료를 알림
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        });
//
//        provider.removeContainer(id);
//
//        try {
//            latch.await(); // 비동기 작업 완료 대기
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error occurred during container check.");
//        }
//
//        return ResponseEntity.ok("Container check result for ID " + id + ": " + (result[0] ? "success" : "failure"));
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


//    @GetMapping("/update")
//    public String update() {
//        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
//        //3c74c4202325
//        return provider.createContainer("test_name", "8082", "80", "1.0.0");
//    }

//    @GetMapping("/checker")
//    public String checker() {
//        //f19b857088ef7ebb0e7c6f144391add30ba0d457ed1ce6f4973eba152a837b94
//        //3c74c4202325
////        return provider.createContainer("test_name", "8082", "80", "1.0.0");
//        System.out.println("is null ? : " + (DockerDataStore.getInstance().getDockerClient() == null));
//        System.out.println("is null ? : " + (dockerProvider == null));
//        System.out.println("is null ? : " + (dockerScheduler == null));
//        return "return";
//    }
}