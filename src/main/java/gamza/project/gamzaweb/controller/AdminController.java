package gamza.project.gamzaweb.controller;

import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.dto.project.FixedProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.project.ProjectListApproveResponse;
import gamza.project.gamzaweb.dto.project.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.service.Interface.AdminService;
import gamza.project.gamzaweb.service.Interface.ProjectService;
import gamza.project.gamzaweb.service.Interface.UserService;
import gamza.project.gamzaweb.validate.custom.AdminCheck;
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
    private final AdminService adminService;
    private final ProjectService projectService;

    @PostMapping("/user/approve/{id}")
    @Operation(description = "유저 권한 승인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> userSignUpApprove(HttpServletRequest request, @PathVariable("id") Long id) {
        try {
            adminService.userSignUpApproveByAdmin(request, id);
            return ResponseEntity.ok().body("해당 유저 가입이 승인되었습니다.");
        } catch (Exception e) {
            throw new UnAuthorizedException("유저 권한 승인 오류", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }

    @PostMapping("/user/refuse/{id}")
    @Operation(description = "유저 승인 거절 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> userSignUpApproveRefused(HttpServletRequest request, @PathVariable("id") Long id) {
        adminService.userSignUpApproveRefusedByAdmin(request, id);
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

    @GetMapping("/project/create/list")
    @Operation(description = "미승인 프로젝트 리스트 출력")
    public Page<ProjectListNotApproveResponse> notApproveProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.notApproveProjectList(request, pageable);
    }

    @GetMapping("/project/pending/list") // containerId 값 반환 필요한가?
    @Operation(description = "승인된 프로젝트 리스트 출력")
    public Page<ProjectListApproveResponse> approvedProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.approvedProjectList(request, pageable);
    }

    @PostMapping("/project/pending/{id}")
    @Operation(description = "프로젝트 성공 확인")
    public ResponseEntity<String> checkSuccessProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.checkSuccessProject(request, id);
        return ResponseEntity.ok().body("프로젝트 생성을 확인했습니다.");
    }

    @PostMapping("/project/approve/{id}")
    @Operation(description = "프로젝트 생성 승인")
    public ResponseEntity<String> approveCreateProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.approveExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.\n승인 프로젝트 시작");
    }

    @DeleteMapping("/project/remove/{id}")
    @Operation(description = "프로젝트 삭제 승인")
    public ResponseEntity<String> RemoveProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.removeExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 삭제되었습니다.");
    }

    @GetMapping("/project/modify/list")
    @Operation(description = "수정 미승인 프로젝트 리스트 출력")
    public Page<FixedProjectListNotApproveResponse> approveFixedProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return projectService.notApproveFixedProjectList(request, pageable);
    }

    @PostMapping("/project/fixed/{id}")
    @Operation(description = "프로젝트 수정 승인")
    public ResponseEntity<String> approveFixedProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.approveFixedExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.");
    }

    @DeleteMapping("/project/fixed/remove/{id}")
    @Operation(description = "프로젝트 수정 삭제")
    public ResponseEntity<String> RemoveFixedProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.removeFixedExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 삭제되었습니다.");
    }

}
