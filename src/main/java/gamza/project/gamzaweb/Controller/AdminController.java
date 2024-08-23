package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.User.ResponseNotApproveDto;
import gamza.project.gamzaweb.Service.Interface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class AdminController {

    private final UserService userService;

    @PostMapping("/approve/{id}")
    @Operation(description = "유저 권한 승인")
    public ResponseEntity<String> approve(HttpServletRequest request, @PathVariable("id") Long id) {
        userService.approve(request,id);
        return ResponseEntity.ok().body("해당 유저 가입이 승인되었습니다.");
    }

    @GetMapping("/approve/list")
    @Operation(description = "미승인 유저 리스트 출력")
    public Page<ResponseNotApproveDto> approveList(
                HttpServletRequest request,
                @RequestParam(name = "page", defaultValue = "0") int page,
                @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return userService.approveList(request, pageable);

    }
}
