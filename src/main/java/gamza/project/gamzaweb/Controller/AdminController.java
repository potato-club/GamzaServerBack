package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.User.ResponseNotApproveDto;
import gamza.project.gamzaweb.Dto.project.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
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
    private final ProjectService projectService;

    @PostMapping("/user/approve/{id}")
    @Operation(description = "유저 권한 승인")
    public ResponseEntity<String> approve(HttpServletRequest request, @PathVariable("id") Long id) {
        userService.approve(request, id);
        return ResponseEntity.ok().body("해당 유저 가입이 승인되었습니다.");
    }

    @PostMapping("/user/not/approve/{id}")
    @Operation(description = "유저 승이 삭제")
    public ResponseEntity<String> notApprove(HttpServletRequest request, @PathVariable("id") Long id) {
        userService.notApprove(request, id);
        return ResponseEntity.ok().body("해당 유저 가입이 거절되었습니다.");
    }

    @GetMapping("/user/approve/list")
    @Operation(description = "미승인 유저 리스트 출력")
    public Page<ResponseNotApproveDto> approveList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return userService.approveList(request, pageable);

    }

    @GetMapping("/project/approve/list")
    @Operation(description = "미승인 프로젝트 리스트 출력")
    public Page<ProjectListNotApproveResponse> approveProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.notApproveProjectList(request, pageable);
    }

    @PostMapping("/project/approve/{id}")
    @Operation(description = "프로젝트 생성 승인")
    public ResponseEntity<String> approveCreateProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.approveExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.");
    }

//
//    @PostMapping("/project/fixed/{id}")
//    @Operation(description = "프로젝트 수정 승인")

}
