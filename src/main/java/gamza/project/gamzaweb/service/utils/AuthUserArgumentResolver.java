package gamza.project.gamzaweb.service.utils;


import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.NotFoundException;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.UserRepository;
import gamza.project.gamzaweb.service.jwt.JwtTokenProvider;
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
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the parameter is annotated with @AuthUser
        boolean hasAuthUserAnnotation = parameter.getParameterAnnotation(AuthUser.class) != null;
        // Check if the parameter type is UserEntity
        boolean isUserEntityType = UserEntity.class.isAssignableFrom(parameter.getParameterType());
        return hasAuthUserAnnotation && isUserEntityType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = jwtTokenProvider.resolveAccessToken(request);

        if (accessToken == null) {
            // This implies the Authorization header was missing or the token was not an access token.
            // If @AuthUser is used, we expect an authenticated user.
            // The JwtAuthorizationTokenFilter should ideally have already rejected unauthenticated requests for protected routes.
            log.warn("AuthUserArgumentResolver: Access Token is missing in the request.");
            throw new UnAuthorizedException("Access Token is required for this operation.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }

        Long userId;
        try {
            // Assuming JwtAuthorizationTokenFilter has already validated the token's
            // signature and expiry. This extracts the user ID from the token's claims.
            userId = jwtTokenProvider.extractId(accessToken);
        } catch (Exception e) {
            // This could catch issues during claim extraction/decryption if not caught by the filter
            log.warn("AuthUserArgumentResolver: Failed to extract user ID from token. Token: [{}], Error: {}", accessToken, e.getMessage());
            throw new UnAuthorizedException("Invalid Access Token or unable to extract user details.", ErrorCode.INVALID_TOKEN_EXCEPTION); // Consider a more specific ErrorCode if available
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("AuthUserArgumentResolver: User not found in database for ID: {}", userId);
                    // Using ErrorCode.NOT_FOUND_EXCEPTION as it's used elsewhere for similar user not found scenarios
                    return new NotFoundException("Authenticated user (ID: " + userId + ") not found in database.", ErrorCode.NOT_FOUND_EXCEPTION);
                });
    }
}