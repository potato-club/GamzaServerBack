package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.Service.Interface.PlatformService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class PlatformController {

    private final PlatformService platformService;

    @GetMapping("/list")
    @Operation(description = "플랫폼 리스트 발급")
    public PlatformListResponseDto getAllPlatformList(HttpServletRequest request) {
        return platformService.getAllPlatformList(request);
    }
}
