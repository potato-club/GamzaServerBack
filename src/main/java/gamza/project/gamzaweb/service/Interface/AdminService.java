package gamza.project.gamzaweb.service.Interface;

import jakarta.servlet.http.HttpServletRequest;

public interface AdminService {

    void userSignUpApproveByAdmin(HttpServletRequest request, Long id);

    void userSignUpApproveRefusedByAdmin(HttpServletRequest request, Long id);

}
