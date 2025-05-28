package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public void userSignUpApproveByAdmin(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        user.approveUserStatus(); // USER Role -> Member Role 로 변경합니다.
        userRepository.save(user);
    }

    @Override
    public void userSignUpApproveRefusedByAdmin(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        user.notApproveUserStatus();
        userRepository.save(user);
    }

    @Override
    public Page<ResponseNotApproveDto> notApproveUserList(Pageable pageable) {
        Page<UserEntity> userEntities = userRepository.findByUserRole(UserRole.USER, pageable);
        return userEntities.map(ResponseNotApproveDto::new);
    }
}
