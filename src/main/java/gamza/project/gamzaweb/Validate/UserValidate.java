package gamza.project.gamzaweb.Validate;

import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.Service.Jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserValidate {

    private final JwtTokenProvider jwtTokenProvider;

    public void validateUserRole(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);
        if (!userRole.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }

    public void invalidUserRole(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);
        if(userRole.equals("2")) {
            throw new UnAuthorizedException("401 Invalid UserRole", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }
}
