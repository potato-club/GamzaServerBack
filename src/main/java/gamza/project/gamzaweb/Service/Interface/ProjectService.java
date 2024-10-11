package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.application.ApplicationRequestDto;
import gamza.project.gamzaweb.Dto.project.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file);

    ProjectListResponseDto getAllProject();

    ProjectListPerResponseDto personalProject(Pageable pageable, HttpServletRequest request);

    void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id);

    Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable);

    void approveExecutionApplication(HttpServletRequest request, Long id);

    void removeExecutionApplication(HttpServletRequest request, Long id);

}
