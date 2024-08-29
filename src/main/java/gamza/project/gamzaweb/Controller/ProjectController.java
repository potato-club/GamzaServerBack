package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.project.*;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ProjectController {

    private final ProjectService projectService;

    // 프로젝트 미승인 전체조회 GET API add  -> admin

    @GetMapping("/list")
    @Operation(description = "메인 페이지 프로젝트 출력 (페이지네이션 default = 4)")
    public ProjectListResponseDto allProjectList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.getAllProject(pageable);
    }

    @PostMapping("/create")
    @Operation(description = "프로젝트 생성 API")
    public ResponseEntity<String> createProject(HttpServletRequest request, @RequestBody ProjectRequestDto dto) {
        try {
            projectService.createProject(request, dto);
            return ResponseEntity.ok().body("프로젝트가 생성되었습니다.");
        } catch (Exception e) {
            throw new BadRequestException("프로젝트 생성 실패 오류", ErrorCode.FAILED_PROJECT_ERROR);
        }

    }

    // zip 압축 푸는거 먼저 해야할듯
    // 프로젝트 수정 요청 허가 api 나머지값들 / zip, 수정하는 api
    // 프로젝트 zip, port 제외한 나머지 값들 수정 요청하는 API PUT MAPPING
    // 프로젝트 zip, port만 수정 요청하는 api PUT MAPPING

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
    public ProjectListPerResponseDto personalProject(
            HttpServletRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.personalProject(pageable, request);
    }
}
