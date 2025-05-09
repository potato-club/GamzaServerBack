package gamza.project.gamzaweb.aop;

import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
@RequiredArgsConstructor
public class CheckAdminAspect {

    private final JwtTokenProvider jwtTokenProvider;
    private final HttpServletRequest request;

    @Pointcut("@annotation(gamza.project.gamzaweb.utils.CheckAdmin)")
    public void checkAdminPointcut() {}

    @Before("checkAdminPointcut()")
    public void checkAdmin() {
        String token = jwtTokenProvider.resolveAccessToken(request);
        if (token == null || token.isEmpty()) {
            throw new UnAuthorizedException("Access token이 존재하지 않습니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        String role = jwtTokenProvider.extractRole(token);
        if (!"0".equals(role)) {
            throw new UnAuthorizedException("관리자만 접근할 수 있는 기능입니다.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }
}
