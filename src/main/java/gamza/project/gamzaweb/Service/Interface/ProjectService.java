package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.project.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto);

    ProjectListResponseDto getAllProject(Pageable pageable);

    ProjectListPerResponseDto personalProject(Pageable pageable, HttpServletRequest request);

    void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id);

    Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable);

    void approveCreateProject(HttpServletRequest request, Long id);
}
