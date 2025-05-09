package gamza.project.gamzaweb.controller;

import gamza.project.gamzaweb.dto.platform.PlatformCreateRequestDto;
import gamza.project.gamzaweb.dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.service.Interface.PlatformService;
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

    @PostMapping("/create")
    @Operation(description = "플랫폼 생성 API")
    public ResponseEntity<String> createPlatform(HttpServletRequest request, @RequestBody PlatformCreateRequestDto dto) {
        platformService.createPlatform(request, dto);
        return ResponseEntity.ok().body("플랫폼 생성이 완료되었습니다.");
    }

    @GetMapping("/list")
    @Operation(description = "프로젝트 생성시 플랫폼 선택 리스트 출력 api")
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
