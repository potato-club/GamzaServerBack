package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.user.request.RequestUserLoginDto;
import gamza.project.gamzaweb.dto.user.request.RequestUserSignUpDto;
import gamza.project.gamzaweb.dto.user.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.dto.user.response.ResponseUserList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    void signUp(RequestUserSignUpDto dto, HttpServletResponse response);

    void login(RequestUserLoginDto dto, HttpServletResponse response);

    void logout(HttpServletRequest request);

    void reissueToken(HttpServletRequest request, HttpServletResponse response);

    void setTokenInHeader(String email, HttpServletResponse response);

    ResponseUserList userList();

    Page<ResponseNotApproveDto> approveList(HttpServletRequest request, Pageable pageable);
}
