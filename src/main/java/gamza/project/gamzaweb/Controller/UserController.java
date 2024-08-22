package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.User.RequestUserLoginDto;
import gamza.project.gamzaweb.Dto.User.RequestUserSignUpDto;
import gamza.project.gamzaweb.Service.Interface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class UserController {

    private final UserService userService;

    // 유저 회원가입 미승인 리스트 GET API add  -> admin

    @PostMapping("/signup")
    @Operation(description = "회원 가입")
    public ResponseEntity<String> signUp(@RequestBody RequestUserSignUpDto dto, HttpServletResponse response) {
        userService.signUp(dto, response);
        return ResponseEntity.ok().body("Success Sing Up!\nIf you want to see the PK value, please ask SH :)");
    }

    @PostMapping("/login")
    @Operation(description = "로그인 AT, RT")
    public ResponseEntity<String> login(@RequestBody RequestUserLoginDto dto, HttpServletResponse response) {
        userService.login(dto, response);
        return ResponseEntity.ok().body("Success Login!");
    }

    @GetMapping("/reissue")
    @Operation(description = "토큰 재발급")
    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {
        userService.reissueToken(request, response);
        return ResponseEntity.ok().body("Success reissue Token!");
    }

    @PostMapping("/approve/{id}")
    @Operation(description = "유저 권한 승인")
    public ResponseEntity<String> approve(HttpServletRequest request, @PathVariable("id") Long id) {
        userService.approve(request,id);
        return ResponseEntity.ok().body("해당 유저 가입이 승인되었습니다.");
    }

}
