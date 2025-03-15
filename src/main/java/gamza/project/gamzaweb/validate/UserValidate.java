package gamza.project.gamzaweb.validate;

import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
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
