package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.User.RequestAddCollaboratorDto;
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
    public ProjectListResponseDto allProjectList() {
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

    // TODO : 프로젝트 생성시 유저 넣기 (O)
    // TODO : 이 유저가 승인된 유저 리스트중 프로젝트 참여 인원으로 추가 할 수 있도록 만들기 (O)
    // TODO : 해당 프로젝트에 참여한 유저 리스트 볼 수 있도록 나타내기 -? 한개의 프로젝트 조회시 참여 인원 모두 나타내면 되지않나? (API 수정예정)
    // TODO : 해당 프로젝트에 존재하는 참여 유저 삭제하는 DELETE API 만들기 (예정 목까지 만들자)

    // TODO : S3 를 활용해서 zip파일을 다운받을 수 있는 API 설계, GET API 하나를 만들고 /project/download/zip/{projectId} 이런식으로 해야할듯
    // TODO : 위 서비스를 진행하려면 프로젝트 만들떄 zip파일을 s3에 저장하는 API 를 추가해야함..
    // TODO : Deadline 목 회의 전


    @PutMapping("/collaborator/{projectId}")
    @Operation(description = "해당 프로젝트에 참여 인원 추가하기 (프로젝트 생성자, 어드민만 가능)")
    public ResponseEntity<String> addProjectCollaborator(HttpServletRequest request, @PathVariable("projectId") Long projectId, @RequestBody RequestAddCollaboratorDto dto) {
        projectService.addProjectCollaborator(request, projectId, dto);
        return ResponseEntity.ok("프로젝트 참여 인원이 추가되었습니다.");
    }

    @DeleteMapping("/collaborator/{projectId}")
    @Operation(description = "해당 프로젝트에 참여 인원 삭제하기 (프로젝트 생성자, 어드민만 가능)")
    public ResponseEntity<String> deleteProjectCollaborator(HttpServletRequest request, @PathVariable("projectId") Long projectId, @RequestBody RequestAddCollaboratorDto dto) {
        projectService.deleteProjectCollaborator(request, projectId, dto);
        return ResponseEntity.ok("프로젝트 참여 인원이 삭제되었습니다.");
    }

}
