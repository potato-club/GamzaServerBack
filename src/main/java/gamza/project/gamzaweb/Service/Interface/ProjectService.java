package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.project.ProjectListResponseDto;
import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto);

    ProjectListResponseDto getAllProject(Pageable pageable);
}
