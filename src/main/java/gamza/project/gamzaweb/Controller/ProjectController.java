package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.User.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            e.printStackTrace();
            throw new BadRequestException("프로젝트 수정 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }
    }


    // TODO : /project/create  에 s3를 적용한 file upload Logic ( O ) project url이 FileEntity에 저장됩니다 링크 방문시 다운로드됨
    // TODO : /user/list 에 파일 부분에 링크 달아줄것 FileEntity에 fileUrl 달아주자 (O) fileURL 반환하도록 했습니다 없는거는 그전에 만들어진거라 null
    // TODO : /project/update/{projectId} 이거 수정하기 ( O )
    // {
    //    "name" : "fixed???",
    //    "description" : "rea???:L???",
    //    "state" : "DONE",
    //    "startedDate" : "2025-01-01",
    //    "endedDate" : "2025-03-30",
    //    "collaborators" : [1,2,3]
    //}
    // TODO : yml 서버꺼 수정해줘야함 s3 변수 추가됨 추가하기 ( O )

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

}
