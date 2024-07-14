package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.User.RequestUserLoginDto;
import gamza.project.gamzaweb.Dto.User.RequestUserSignUpDto;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    void signUp(RequestUserSignUpDto dto, HttpServletResponse response);

    void login(RequestUserLoginDto dto, HttpServletResponse response);

    void setTokenInHeader(String email, HttpServletResponse response);
}
