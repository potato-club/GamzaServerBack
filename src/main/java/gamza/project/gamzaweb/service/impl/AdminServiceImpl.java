package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.Interface.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public void userSignUpApproveByAdmin(HttpServletRequest request, Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        user.approveUserStatus(); // USER Role -> Member Role 로 변경합니다.
        userRepository.save(user);
    }

    @Override
    public void userSignUpApproveRefusedByAdmin(HttpServletRequest request, Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow();
        user.notApproveUserStatus();
        userRepository.save(user);
    }
}
