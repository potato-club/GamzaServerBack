package gamza.project.gamzaweb.Service.Jwt;

import gamza.project.gamzaweb.Error.ErrorJwtCode;
import gamza.project.gamzaweb.Error.requestError.ExpiredRefreshTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.simple.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        String path = request.getRequestURI();
        ErrorJwtCode errorCode;

        if (accessToken == null && refreshToken != null && path.contains("/reissue")) {
            try {
                jwtTokenProvider.validateRefreshToken(refreshToken);
                filterChain.doFilter(request, response);
            } catch (ExpiredRefreshTokenException e) {
                errorCode = ErrorJwtCode.EXPIRED_REFRESH_TOKEN;
                setResponse(response, errorCode);
                return;
            }
        }

        try {
            if (accessToken == null && refreshToken != null) {
                if (path.contains("/reissue") && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } else if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            } else {
                if (jwtTokenProvider.validateAccessToken(accessToken)) {
                    this.setAuthentication(accessToken);

                }
            }
        } catch (MalformedJwtException e) {
            errorCode = ErrorJwtCode.INVALID_JWT_FORMAT;
            setResponse(response, errorCode);
            return;
        } catch (ExpiredJwtException e) {
            errorCode = ErrorJwtCode.EXPIRED_ACCESS_TOKEN;
            setResponse(response, errorCode);
            return;
        } catch (UnsupportedJwtException e) {
            errorCode = ErrorJwtCode.UNSUPPORTED_JWT_TOKEN;
            setResponse(response, errorCode);
            return;
        } catch (IllegalArgumentException e) {
            errorCode = ErrorJwtCode.INVALID_VALUE;
            setResponse(response, errorCode);
            return;
        } catch (RuntimeException e) {
            errorCode = ErrorJwtCode.RUNTIME_EXCEPTION;
            setResponse(response, errorCode);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);

    }

    private void setAuthentication(String token) throws Exception {
//        Authentication authentication = jwtTokenProvider.(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void setResponse(HttpServletResponse response, ErrorJwtCode errorCode) throws IOException {
        JSONObject json = new JSONObject();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        json.put("code", errorCode.getCode());
        json.put("message", errorCode.getMessage());

        response.getWriter().print(json);
        response.getWriter().flush();
    }
}
