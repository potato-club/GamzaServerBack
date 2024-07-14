package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.User.RequestUserSignUpDto;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    void signUp(RequestUserSignUpDto dto, HttpServletResponse response);
}
