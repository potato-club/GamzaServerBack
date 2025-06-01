package gamza.project.gamzaweb.utils.validate.aop;

import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminCheckAOP {

    private final JwtTokenProvider jwtTokenProvider;

    @Pointcut("@annotation(AdminCheck)")
    public void adminCheckPointcut() {}

    @Before("adminCheckPointcut()")
    public void checkAdminRole() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        String token = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(token);

        if (!userRole.equals("0")) {
            throw new UnAuthorizedException("해당 API 접근 권한이 존재하지 않습니다 - ADMIN LEVEL" , ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }
}
