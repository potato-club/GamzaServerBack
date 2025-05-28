package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.project.FixedProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.project.ProjectListApproveResponse;
import gamza.project.gamzaweb.dto.project.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    void userSignUpApproveByAdmin(Long id);

    void userSignUpApproveRefusedByAdmin(Long id);

    void checkSuccessProject(Long id);

    void approveExecutionApplication(HttpServletRequest request, Long id);

    void removeExecutionApplication(Long id);

    void approveFixedExecutionApplication(HttpServletRequest request, Long id);

    void removeFixedExecutionApplication(Long id);

    Page<ResponseNotApproveDto> notApproveUserList(Pageable pageable);

    Page<ProjectListNotApproveResponse> notApproveProjectList(Pageable pageable);

    Page<ProjectListApproveResponse> approvedProjectList(Pageable pageable);

    Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(Pageable pageable);

}
