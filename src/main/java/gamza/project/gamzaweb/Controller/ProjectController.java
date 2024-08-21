package gamza.project.gamzaweb.Controller;


import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import gamza.project.gamzaweb.Service.Interface.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost:3000, localhost:3000")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<String> createProject(HttpServletRequest request, @RequestBody ProjectRequestDto dto) {
        projectService.createProject(request, dto);
        return ResponseEntity.ok().body("프로젝트가 생성되었습니다.");
    }
}
