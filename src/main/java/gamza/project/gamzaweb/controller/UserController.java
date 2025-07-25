package gamza.project.gamzaweb.controller;

import gamza.project.gamzaweb.dto.User.request.RequestUserLoginDto;
import gamza.project.gamzaweb.dto.User.request.RequestUserSignUpDto;
import gamza.project.gamzaweb.dto.User.response.ResponseUserList;
import gamza.project.gamzaweb.service.Interface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "${cors.allowed-origins}")
@Slf4j
public class UserController {

    private final UserService userService;

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

    @GetMapping("/logout")
    @Operation(description = "로그아웃")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok().body("Success Logout!");
    }

    @GetMapping("/reissue")
    @Operation(description = "토큰 재발급")
    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {
        userService.reissueToken(request, response);
        return ResponseEntity.ok().body("Success reissue Token!");
    }

    @GetMapping("/list")
    @Operation(description = "유저 리스트 출력")
    public ResponseUserList userList() {
        return userService.userList();
    }
}
