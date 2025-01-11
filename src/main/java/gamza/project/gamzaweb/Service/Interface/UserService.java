package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.User.request.RequestUserLoginDto;
import gamza.project.gamzaweb.Dto.User.request.RequestUserSignUpDto;
import gamza.project.gamzaweb.Dto.User.response.ResponseNotApproveDto;
import gamza.project.gamzaweb.Dto.User.response.ResponseUserList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    void signUp(RequestUserSignUpDto dto, HttpServletResponse response);

    void login(RequestUserLoginDto dto, HttpServletResponse response);

    void reissueToken(HttpServletRequest request, HttpServletResponse response);

    void setTokenInHeader(String email, HttpServletResponse response);

    void approve(HttpServletRequest request, Long id);

    void notApprove(HttpServletRequest request, Long id);

    ResponseUserList userList();

    Page<ResponseNotApproveDto> approveList(HttpServletRequest request, Pageable pageable);
}
