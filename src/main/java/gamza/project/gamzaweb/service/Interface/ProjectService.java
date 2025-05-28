package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.user.request.RequestAddCollaboratorDto;
import gamza.project.gamzaweb.dto.project.*;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file);

    ProjectListResponseDto getAllProject(HttpServletRequest request);

    ProjectListPerResponseDto personalProject(HttpServletRequest request);

    void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id);

    Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(HttpServletRequest request, Pageable pageable);

    ProjectDetailResponseDto getProjectById(HttpServletRequest request, Long id);

    ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, Long projectId);

    void updateApplication(HttpServletRequest request, ApplicationUpdateRequestDto dto, Long projectId,MultipartFile file);

    void deleteProjectCollaborator(HttpServletRequest request, Long projectId, RequestAddCollaboratorDto dto);

    void approveExecutionApplication(HttpServletRequest request, Long id);

    void startExecutionApplication(ProjectEntity project, String AT);

    void removeExecutionApplication(HttpServletRequest request, Long id);

    void deleteProjectById(HttpServletRequest request, Long projectId);

    void removeTeamProjectInMyPage(HttpServletRequest request, Long id);

    void approveFixedExecutionApplication(HttpServletRequest request, Long id);

    void removeFixedExecutionApplication(HttpServletRequest request, Long id);
}
