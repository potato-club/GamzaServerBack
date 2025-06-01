package gamza.project.gamzaweb.utils.validate.aop;

import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.utils.validate.JpaAssistance;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.UnAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCheckParamAOP implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final JpaAssistance jpaAssistance;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthUserAnnotation = parameter.getParameterAnnotation(AuthCheckParam.class) != null;
        boolean isUserEntityType = UserEntity.class.isAssignableFrom(parameter.getParameterType());
        return hasAuthUserAnnotation && isUserEntityType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        String userRole = jwtTokenProvider.extractRole(accessToken);

        if(!userRole.equals("0")) {
            throw new UnAuthorizedException("401 NOT ADMIN", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Long userId = jwtTokenProvider.extractId(accessToken);

        return jpaAssistance.getUserPkValue(userId);

    }
}