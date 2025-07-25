package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.User.request.RequestUserLoginDto;
import gamza.project.gamzaweb.dto.User.request.RequestUserSignUpDto;
import gamza.project.gamzaweb.dto.User.response.ResponseUserList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    void signUp(RequestUserSignUpDto dto, HttpServletResponse response);

    void login(RequestUserLoginDto dto, HttpServletResponse response);

    void logout(HttpServletRequest request);

    void reissueToken(HttpServletRequest request, HttpServletResponse response);

    void setTokenInHeader(String email, HttpServletResponse response);

    ResponseUserList userList();

}
