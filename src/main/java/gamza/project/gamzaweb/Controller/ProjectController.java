package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.project.ProjectListResponseDto;
import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import gamza.project.gamzaweb.Dto.project.ProjectResponseDto;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost:3000, localhost:3000")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/list")
    public ProjectListResponseDto allProjectList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "4") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return projectService.getAllProject(pageable);
    }

    @PostMapping("/create")
    @Operation(description = "프로젝트 생성 API")
    public ResponseEntity<String> createProject(HttpServletRequest request, @RequestBody ProjectRequestDto dto) {
        projectService.createProject(request, dto);
        return ResponseEntity.ok().body("프로젝트가 생성되었습니다.");
    }
}
