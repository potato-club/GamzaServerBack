package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.project.request.ApplicationUpdateRequestDto;
import gamza.project.gamzaweb.dto.project.request.ProjectRequestDto;
import gamza.project.gamzaweb.dto.project.request.ProjectUpdateRequestDto;
import gamza.project.gamzaweb.dto.project.response.ApplicationDetailResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectDetailResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectListPerResponseDto;
import gamza.project.gamzaweb.dto.project.response.ProjectListResponseDto;
import gamza.project.gamzaweb.dto.user.request.RequestAddCollaboratorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProjectService {

    void createProject(HttpServletRequest request, ProjectRequestDto dto, MultipartFile file);

    void updateProject(HttpServletRequest request, ProjectUpdateRequestDto dto, Long id);

    void deleteProjectById(HttpServletRequest request, Long projectId);

    void deleteProjectCollaborator(HttpServletRequest request, Long projectId, RequestAddCollaboratorDto dto);

    void updateApplication(HttpServletRequest request, ApplicationUpdateRequestDto dto, Long projectId, MultipartFile file);

    void removeTeamProjectInMyPage(HttpServletRequest request, Long id);

    ProjectListResponseDto getAllProject(HttpServletRequest request);

    ProjectListPerResponseDto personalProject(HttpServletRequest request);

    ProjectDetailResponseDto getProjectById(HttpServletRequest request, Long id);

    ApplicationDetailResponseDto getApplicationByProjId(HttpServletRequest request, Long projectId);

}
