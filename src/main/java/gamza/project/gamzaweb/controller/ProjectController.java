package gamza.project.gamzaweb.controller;


import gamza.project.gamzaweb.dto.project.request.ApplicationUpdateRequestDto;
import gamza.project.gamzaweb.dto.project.request.ProjectRequestDto;
import gamza.project.gamzaweb.dto.project.request.ProjectUpdateRequestDto;
import gamza.project.gamzaweb.dto.project.response.ApplicationDetailResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectDetailResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectListPerResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectListResponseDto;
import gamza.project.gamzaweb.dto.user.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;
import gamza.project.gamzaweb.service.Interface.ProjectService;
import gamza.project.gamzaweb.validate.custom.AdminCheck;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProjectController {

    private final ProjectService projectService;
    private final DeploymentSseController deploymentSseController;

    @GetMapping("/list")
    @Operation(description = "메인 페이지 프로젝트 출력")
    public ProjectListResponseDto allProjectList(HttpServletRequest request) {
        return projectService.getAllProject(request);
    }

    @PostMapping(value = "/create")
    @Operation(description = "프로젝트 생성 API")
    @AdminCheck
    public ResponseEntity<String> createProject(
            @RequestPart(value = "zip", required = false) MultipartFile file,
            @ModelAttribute ProjectRequestDto dto,
            HttpServletRequest request) {
        try {
            projectService.createProject(request, dto, file);
            return ResponseEntity.ok().body("프로젝트가 생성되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("프로젝트 생성 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    @Operation(description = "프로젝트 수정(zip, port 제외한 나머지 값들")
    public ResponseEntity<String> updateProject(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody ProjectUpdateRequestDto dto) {
        try {
            projectService.updateProject(request, dto, id);
            return ResponseEntity.ok().body("프로젝트가 수정되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("프로젝트 수정 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    @GetMapping("/user/list")
    @Operation(description = "회원이 만든 프로젝트 출력")
    public ProjectListPerResponseDto personalProject(HttpServletRequest request) {
        return projectService.personalProject(request);
    }

    @GetMapping("/{id}")
    @Operation(description = "프로젝트 조회") // -> containerId값 반환 필요한가?
    public ProjectDetailResponseDto getProjectById(HttpServletRequest request, @PathVariable("id") Long id) {
        return projectService.getProjectById(request, id);
    }

    @GetMapping("/app/{projectId}")
    @Operation(description = "어플리케이션 조회, 프로젝트 id로 조회하면 됨") // -> containerId값 반환 필요한가?
    public ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, @PathVariable("projectId") Long projectId) {
        return projectService.getApplicationByProjId(request, projectId);
    }

    @PutMapping("/app/update/{projectId}")
    @Operation(description = "어플리케이션 수정, 프로젝트 id로 조회하면 됨")
    public ResponseEntity<String> getApplicationByProjId(
            HttpServletRequest request,
            @PathVariable("projectId") Long projectId,
            @RequestPart(value = "zip", required = false) MultipartFile file,
            @ModelAttribute ApplicationUpdateRequestDto dto) {
        try {
            projectService.updateApplication(request, dto, projectId, file);
            return ResponseEntity.ok().body("어플리케이션이 수정되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("어플리케이션 수정 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }

    @DeleteMapping("/user/list/{projectId}")
    @Operation(description = "회원이 만든 특정 프로젝트 삭제")
    public ResponseEntity<String> deleteProject(HttpServletRequest request, @PathVariable("projectId") Long projectId) {
        projectService.deleteProjectById(request, projectId);
        return ResponseEntity.ok("프로젝트 삭제 완료");
    }

    @PutMapping("/collaborator/{projectId}")
    @Operation(description = "해당 프로젝트에 참여 인원 추가하기 (프로젝트 생성자, 어드민만 가능) API 미사용")
    public ResponseEntity<String> addProjectCollaborator(HttpServletRequest request, @PathVariable("projectId") Long projectId, @RequestBody RequestAddCollaboratorDto dto) {
//        projectService.addProjectCollaborator(request, projectId, dto);
        return ResponseEntity.ok("프로젝트 참여 인원이 추가되었습니다.");
    }

    @DeleteMapping("/collaborator/{projectId}")
    @Operation(description = "해당 프로젝트에 참여 인원 삭제하기 (프로젝트 생성자, 어드민만 가능) API 미사용 / 2차개발")
    public ResponseEntity<String> deleteProjectCollaborator(HttpServletRequest request, @PathVariable("projectId") Long projectId, @RequestBody RequestAddCollaboratorDto dto) {
        projectService.deleteProjectCollaborator(request, projectId, dto);
        return ResponseEntity.ok("프로젝트 참여 인원이 삭제되었습니다.");
    }

//    @GetMapping("/deploy/subscribe/{projectId}")
//    @Operation(description = "SSE를 활용한 실시간 배포 상태 업데이트")
//    public SseEmitter subscribeToDeploymentUpdates(@PathVariable Long projectId) {
//        return deploymentSseController.subscribe(projectId);
//    }

}
