package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    void userSignUpApproveByAdmin(Long id);

    void userSignUpApproveRefusedByAdmin(Long id);

    Page<ResponseNotApproveDto> notApproveUserList(Pageable pageable);


}
