package gamza.project.gamzaweb.validate.custom;


import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
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
public class AdminCheckAspect {

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

        if (!"0".equals(userRole)) {
            throw new UnAuthorizedException("401 NOT ADMIN" , ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
    }
}
