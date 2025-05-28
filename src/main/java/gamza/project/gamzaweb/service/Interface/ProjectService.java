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
