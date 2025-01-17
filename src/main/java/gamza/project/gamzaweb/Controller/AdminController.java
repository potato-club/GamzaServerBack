package gamza.project.gamzaweb.Controller;

import gamza.project.gamzaweb.Dto.User.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.Dto.project.FixedProjectListNotApproveResponse;
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
    public Page<ProjectListNotApproveResponse> approveProjectList(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.notApproveProjectList(request, pageable);
    }

    // TODO : 프로젝트 수정 dockerfile 실행되는 코드 만들기 ( O )
    //      TODO : -> 위 방식은 기존 도커 컨테이너 스탑, 삭제  이후 실행하는걸로 했음
    //      TODO : 근데 컨테이너랑 프로젝트 관계설정이 안돼서 그것도 추가해줘서 jpa방식과 합쳐서 해결 완료
    // TODO : zip string 으로  보내주는거 url로 보내주기, 오름차순? pk값? ( O )
    // TODO : 서버 이미지 크기 nginx 설정해주기 ( O ) 100MB 로 설정함 만약 안되면 서버 엔진엑스 껏다 켜봐야할듯? 일단 내가 리로드하긴함

    @PostMapping("/project/approve/{id}")
    @Operation(description = "프로젝트 생성 승인")
    public ResponseEntity<String> approveCreateProject(HttpServletRequest request, @PathVariable("id") Long id) {
        projectService.approveExecutionApplication(request, id);
        return ResponseEntity.ok().body("해당 프로젝트가 승인되었습니다.");
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

//    @DeleteMapping("/project/fixed/remove/{id}")
//    @Operation(description = "프로젝트 수정 삭제")
//    public ResponseEntity<String> RemoveFixedProject(HttpServletRequest request, @PathVariable("id") Long id) {
//        projectService.removeFixedExecutionApplication(request, id);
//        return ResponseEntity.ok().body("해당 프로젝트가 삭제되었습니다.");
//    }

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
