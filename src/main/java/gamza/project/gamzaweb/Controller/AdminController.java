package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.User.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.Dto.project.FixedProjectListNotApproveResponse;
import gamza.project.gamzaweb.Dto.project.ProjectListApproveResponse;
import gamza.project.gamzaweb.Dto.project.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import gamza.project.gamzaweb.Service.Interface.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
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

    @PostMapping("/user/refuse/{id}")
    @Operation(description = "유저 승인 삭제")
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

    @GetMapping("/project/create/list")
    @Operation(description = "미승인 프로젝트 리스트 출력")
    public Page<ProjectListNotApproveResponse> notApproveProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.notApproveProjectList(request, pageable);
    }

    @GetMapping("/project/create/approve/list")
    @Operation(description = "승인 프로젝트 리스트 출력")
    public Page<ProjectListApproveResponse> approvedProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.approvedProjectList(request, pageable);
    }

    //TODO: - 백엔드에서 도커이미지 실행 스텝별로 프론트로 보낼 수 있는지 (O)
    //TODO:-- 성공, 실패 여부도 (o)
    //TODO:- 생성승인했을때 프로젝트 실패하면 approve안되게 하고 수정승인 삭제 api (O)
    //TODO:- 프론트 도커 파일 빌드 안됨,,,,재확인->이거 nginx 때문에 그런듯
    //TODO: 마이페이지 손보기 -> 접속해보면 오류남 -> ( O )
    //TODO: 프로젝트 수정삭제 주석 풀어야함.. -> ( O )

    // 1/27 추가 요청사항
    // TODO : 로그아웃 기능 만들기
    // TODO : 서브도메인 만들기

    @PostMapping("/project/approve/{id}")
    @Operation(description = "프로젝트 생성 승인")
    public ResponseEntity<String> approveCreateProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.approveExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다. 승인 프로젝트 시작됨");
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

    @GetMapping("/request")
    public String getRequestInfo(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr(); // 요청자의 IP 주소
        String userAgent = request.getHeader("User-Agent"); // 요청자의 User-Agent 정보
        String method = request.getMethod(); // HTTP 메서드 (GET, POST 등)
        String url = request.getRequestURL().toString(); // 요청 URL

        return String.format("IP: %s, User-Agent: %s, Method: %s, URL: %s",
                ipAddress, userAgent, method, url);
    }


}
