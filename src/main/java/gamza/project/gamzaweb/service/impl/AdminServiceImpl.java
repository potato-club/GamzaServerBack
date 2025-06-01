package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus;
import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.dctutil.DockerProvider;
import gamza.project.gamzaweb.dto.project.response.FixedProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.project.response.ProjectListApproveResponse;
import gamza.project.gamzaweb.dto.project.response.ProjectListNotApproveResponse;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;
import gamza.project.gamzaweb.repository.ContainerRepository;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.AdminService;
import gamza.project.gamzaweb.utils.JpaAssistance;
import gamza.project.gamzaweb.validate.FileUploader;
import gamza.project.gamzaweb.validate.ProjectValidate;
import jakarta.servlet.http.HttpServletRequest;
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
    private final ContainerRepository containerRepository;

    private final JpaAssistance jpaAssistance;
    private final FileUploader fileUploader;
    private final DockerProvider dockerProvider;
    private final ProjectValidate projectValidate;

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

    @Override
    public Page<FixedProjectListNotApproveResponse> notApproveFixedProjectList(Pageable pageable) {
        Page<ProjectEntity> projectEntities = projectRepository.findByFixedStateAndApproveFixedState(true, false, pageable);
        return projectEntities.map(FixedProjectListNotApproveResponse::new);
    }

    @Override
    public void removeExecutionApplication(Long id) {
        jpaAssistance.getProjectPkValue(id);
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeFixedExecutionApplication(Long id) {
        ProjectEntity project = jpaAssistance.getProjectPkValue(id);
        projectValidate.isProjectFixedState(project);
        projectRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void approveFixedExecutionApplication(HttpServletRequest request, Long id) {
        ProjectEntity project = jpaAssistance.getProjectPkValue(id);

        ContainerEntity containerEntity = containerRepository.findContainerEntityByApplication(project.getApplication());
        dockerProvider.stopContainer(request, containerEntity);
        dockerProvider.removeContainer(containerEntity.getContainerId());
        containerRepository.delete(containerEntity);

        String AT = request.getHeader("Authorization").substring(7);

        boolean buildSuccess = dockerProvider.buildDockerImageFromApplicationZip(AT, project);
        if (buildSuccess) {
            dockerProvider.updateProjectApprovalFixedState(project);
        }
    }


    @Override
    @Transactional
    public void approveExecutionApplication(HttpServletRequest request, Long id) {

        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 프로젝트를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        project.updateApprovalProjectStatus(ApprovalProjectStatus.PENDING);
        projectRepository.save(project);

        String AT = request.getHeader("Authorization").substring(7);
        dockerProvider.startExecutionApplication(project, AT);

    }

}
