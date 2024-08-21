package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import jakarta.servlet.http.HttpServletRequest;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto);
}
