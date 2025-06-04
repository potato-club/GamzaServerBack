package gamza.project.gamzaweb.controller;

import gamza.project.gamzaweb.dto.User.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.dto.project.response.FixedProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.project.response.ProjectListApproveResponse;
import gamza.project.gamzaweb.dto.project.response.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.BadRequestException;
import gamza.project.gamzaweb.service.Interface.AdminService;
import gamza.project.gamzaweb.utils.validate.aop.AdminCheck;
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

    private final AdminService adminService;

    @PostMapping("/user/approve/{id}")
    @Operation(description = "유저 권한 승인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> userSignUpApprove(@PathVariable("id") Long id) {
        try {
            adminService.userSignUpApproveByAdmin(id);
            return ResponseEntity.ok().body("해당 유저 가입이 승인되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("유저 권한 승인 오류", ErrorCode.BAD_REQUEST_EXCEPTION);
        }
    }

    @PostMapping("/user/refuse/{id}")
    @Operation(description = "유저 승인 거절 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> userSignUpApproveRefused(@PathVariable("id") Long id) {
        try {
            adminService.userSignUpApproveRefusedByAdmin(id);
            return ResponseEntity.ok().body("해당 유저 가입이 거절되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("유저 권한 거절 오류", ErrorCode.BAD_REQUEST_EXCEPTION);
        }
    }

    @GetMapping("/user/approve/list")
    @Operation(description = "미승인 유저 리스트 출력 - ADMIN LEVEL")
    @AdminCheck
    public Page<ResponseNotApproveDto> printNotApproveUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return adminService.notApproveUserList(pageable);
        } catch (Exception e) {
            throw new BadRequestException("유저 리스트 출력 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @GetMapping("/project/create/list")
    @Operation(description = "미승인 프로젝트 리스트 출력 - ADMIN LEVEL")
    @AdminCheck
    public Page<ProjectListNotApproveResponse> printNotApproveProject(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return adminService.notApproveProjectList(pageable);
        } catch (Exception e) {
            throw new BadRequestException("미승인 프로젝트 출력 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @GetMapping("/project/pending/list")
    @Operation(description = "승인된 프로젝트 리스트 출력 - ADMIN LEVEL")
    @AdminCheck
    public Page<ProjectListApproveResponse> approvedProjectList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return adminService.approvedProjectList(pageable);
        } catch (Exception e) {
            throw new BadRequestException("승인 프로젝트 리스트 출력 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @PostMapping("/project/pending/{id}")
    @Operation(description = "프로젝트 성공 확인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> checkSuccessProject(@PathVariable("id") Long id) {
        try {
            adminService.checkSuccessProject(id);
            return ResponseEntity.ok().body("프로젝트 생성을 확인했습니다.");
        } catch (Exception e) {
            throw new BadRequestException("성공 확인요청 반환 오류", ErrorCode.BAD_REQUEST_EXCEPTION);
        }
    }

    @PostMapping("/project/approve/{id}")
    @Operation(description = "프로젝트 생성 승인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> approveCreateProject(HttpServletRequest request, @PathVariable("id") Long id) {
        try {
            adminService.approveExecutionApplication(request, id);
            return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.\n승인 프로젝트 시작");
        } catch (Exception e) {
            throw new BadRequestException("프로젝트 승인 요청 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @DeleteMapping("/project/remove/{id}")
    @Operation(description = "프로젝트 삭제 승인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> RemoveProject(@PathVariable("id") Long id) {
        try {
            adminService.removeExecutionApplication(id);
            return ResponseEntity.ok().body("해당 프로젝트가 삭제되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("프로젝트 삭제 요청 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    @GetMapping("/project/modify/list")
    @Operation(description = "수정 미승인 프로젝트 리스트 출력 - ADMIN LEVEL")
    @AdminCheck
    public Page<FixedProjectListNotApproveResponse> approveFixedProjectList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return adminService.notApproveFixedProjectList(pageable);
        } catch (Exception e) {
            throw new BadRequestException("미승인 프로젝트 리스트 출력 오류", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }

    @PostMapping("/project/fixed/{id}")
    @Operation(description = "프로젝트 수정 승인 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> approveFixedProject(HttpServletRequest request, @PathVariable("id") Long id) {
        try {
            adminService.approveFixedExecutionApplication(request, id);
            return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("프로젝트 수정 승인요청 오류", ErrorCode.BAD_REQUEST_EXCEPTION);
        }
    }

    @DeleteMapping("/project/fixed/remove/{id}")
    @Operation(description = "프로젝트 수정 삭제 - ADMIN LEVEL")
    @AdminCheck
    public ResponseEntity<String> RemoveFixedProject(@PathVariable("id") Long id) {
        try {
            adminService.removeFixedExecutionApplication(id);
            return ResponseEntity.ok().body("해당 프로젝트가 삭제되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("프로젝트 수정요청 삭제요청 오류", ErrorCode.BAD_REQUEST_EXCEPTION);
        }
    }

}
