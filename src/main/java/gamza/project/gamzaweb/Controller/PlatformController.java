package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.Service.Interface.PlatformService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/platform")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class PlatformController {

    private final PlatformService platformService;

    // TODO : 플랫폼별 프로젝트 리스트 반환 -> 일단 무한스크롤? 페이지네이션 ㄴ? 4개적당해보이는데
    // TODO :

//    @GetMapping("/project/list")
//    @Operation(description = "플랫폼별 프로젝트 리스트 출력")
//    public void getAllPlatformProjectList() {
//        return platformService
//    }

    @GetMapping("/list")
    @Operation(description = " 프로젝트 생성시 플랫폼 선택 리스트 출력 api") // ->
    public PlatformListResponseDto getAllPlatformList(HttpServletRequest request) {
        return platformService.getAllPlatformList(request);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "플랫폼 삭제 (함부로 삭제를 하게 둬선 안돼!!!!!!!)")
    public ResponseEntity<String> deletePlatform(HttpServletRequest request, @PathVariable("id") Long id) {
        platformService.deletePlatform(request, id);
        return ResponseEntity.ok().body("진짜 이거 뜬거면 이제 복구 못함 수고요");
    }

}
