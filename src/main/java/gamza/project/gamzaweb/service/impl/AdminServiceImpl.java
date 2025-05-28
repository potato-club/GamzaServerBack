package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.dto.project.ProjectListApproveResponse;
import gamza.project.gamzaweb.dto.project.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.NotFoundException;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.AdminService;
import gamza.project.gamzaweb.utils.JpaAssistance;
import gamza.project.gamzaweb.validate.FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final JpaAssistance jpaAssistance;

    private final FileUploader fileUploader;

    @Override
    @Transactional
    public void userSignUpApproveByAdmin(Long id) {
        UserEntity user = jpaAssistance.getUserPkValue(id);
        user.approveUserStatus();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void userSignUpApproveRefusedByAdmin(Long id) {
        UserEntity user = jpaAssistance.getUserPkValue(id);
        user.notApproveUserStatus();
        userRepository.save(user);
    }

    @Override
    public Page<ResponseNotApproveDto> notApproveUserList(Pageable pageable) {
        Page<UserEntity> userEntities = userRepository.findByUserRole(UserRole.USER, pageable);
        return userEntities.map(ResponseNotApproveDto::new);
    }

    @Override
    public Page<ProjectListNotApproveResponse> notApproveProjectList(Pageable pageable) {
        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveState(false, false, pageable);

        return projectEntities.map(project -> {
            String fileUrl = fileUploader.recentGetFileUrl(project);
            return new ProjectListNotApproveResponse(project, fileUrl);
        });
    }

    @Override
    public Page<ProjectListApproveResponse> approvedProjectList(Pageable pageable) {
        Page<ProjectEntity> projectEntities = projectRepository.findByApprovalProjectStatusIsNotNullAndSuccessCheckFalse(pageable);

        return projectEntities.map(project -> {
            String fileUrl = fileUploader.recentGetFileUrl(project);
            return new ProjectListApproveResponse(project, fileUrl);
        });
    }

    @Override
    @Transactional
    public void checkSuccessProject(Long id) {
        ProjectEntity project = jpaAssistance.getProjectPkValue(id);
        project.updateSuccessCheck();
    }

}
