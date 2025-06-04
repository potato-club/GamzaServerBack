package gamza.project.gamzaweb.controller;

import gamza.project.gamzaweb.dto.platform.request.PlatformCreateRequestDto;
import gamza.project.gamzaweb.dto.platform.response.PlatformListResponseDto;
import gamza.project.gamzaweb.service.Interface.PlatformService;
import gamza.project.gamzaweb.utils.validate.aop.AdminCheck;
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
    @AdminCheck
    @Operation(description = "플랫폼 생성 API")
    public ResponseEntity<String> createPlatform(@RequestBody PlatformCreateRequestDto dto) {
        platformService.createPlatform(dto);
        return ResponseEntity.ok().body("플랫폼 생성이 완료되었습니다.");
    }

    @GetMapping("/list")
    @AdminCheck
    @Operation(description = "프로젝트 생성시 플랫폼 선택 리스트 출력 api")
    public PlatformListResponseDto getAllPlatformList() {
        return platformService.getAllPlatformList();
    }

    @DeleteMapping("/{id}")
    @AdminCheck
    @Operation(description = "플랫폼 삭제 (함부로 삭제를 하게 둬선 안돼!!!!!!!)")
    public ResponseEntity<String> deletePlatform(@PathVariable("id") Long id) {
        platformService.deletePlatform(id);
        return ResponseEntity.ok().body("플랫폼이 삭제되었습니다");
    }

}
