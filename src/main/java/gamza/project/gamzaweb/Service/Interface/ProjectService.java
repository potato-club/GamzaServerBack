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

    ProjectListPerResponseDto personalProject(HttpServletRequest request);

    void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id);

    Page<ProjectListNotApproveResponse> notApproveProjectList(HttpServletRequest request, Pageable pageable);

    Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(HttpServletRequest request, Pageable pageable);

    ProjectDetailResponseDto getProjectById(HttpServletRequest request, Long id);

    ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, Long projectId);

//    void onContainerCreated(String containerId);

    void approveExecutionApplication(HttpServletRequest request, Long id);

    void removeExecutionApplication(HttpServletRequest request, Long id);

    void deleteProjectById(HttpServletRequest request, Long projectId);

    void removeTeamProjectInMyPage(HttpServletRequest request, Long id);

    void approveFixedExecutionApplication(HttpServletRequest request, Long id);

    void removeFixedExecutionApplication(HttpServletRequest request, Long id);
}
