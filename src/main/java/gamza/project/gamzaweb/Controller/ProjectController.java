package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.application.ApplicationRequestDto;
import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProjectController {

    private final ProjectService projectService;


    @GetMapping("/list")
    @Operation(description = "메인 페이지 프로젝트 출력")
    public ProjectListResponseDto allProjectList(){
        return projectService.getAllProject();
    }

    @PostMapping(value = "/create")
    @Operation(description = "프로젝트 생성 API")
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
            throw new BadRequestException("프로젝트 수정 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }

    }

    @GetMapping("/user/list")
    @Operation(description = "회원이 만든 프로젝트 출력")
    public ProjectListPerResponseDto personalProject(HttpServletRequest request) {
        return projectService.personalProject(request);
    }

    @GetMapping("/{id}")
    @Operation(description = "프로젝트 조회")
    public ProjectDetailResponseDto getProjectById(HttpServletRequest request, @PathVariable Long id) {
        return projectService.getProjectById(request, id);
    }

    @GetMapping("/app/{projectId}")
    @Operation(description = "어플리케이션 조회, 프로젝트 id로 조회하면 됨")
    public ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, @PathVariable Long projectId) {
        return projectService.getApplicationByProjId(request, projectId);
    }

    @DeleteMapping("/user/list/{projectId}")
    @Operation(description = "회원이 만든 특정 프로젝트 삭제")
    public ResponseEntity<String> deleteProject(HttpServletRequest request, @PathVariable Long projectId) {
        projectService.deleteProjectById(request, projectId);
        return ResponseEntity.ok("프로젝트 삭제 완료");
    }
}
