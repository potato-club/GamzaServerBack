package gamza.project.gamzaweb.Service.Jwt;

import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.ErrorJwtCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Error.requestError.ExpiredRefreshTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        ErrorJwtCode errorCode;

        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            if (refreshToken == null) {
                setResponse(response, ErrorJwtCode.INVALID_VALUE);
                return;
            }

            if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken) && path.contains("/reissue")) {
                jwtTokenProvider.validateRefreshToken(refreshToken);
                filterChain.doFilter(request, response);
            }
        } catch (ExpiredJwtException e) {
            setResponse(response, ErrorJwtCode.EXPIRED_REFRESH_TOKEN);
            return;
        }

        try {
            String accessToken = jwtTokenProvider.resolveAccessToken(request);
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            if (accessToken == null) {
                setResponse(response, ErrorJwtCode.INVALID_VALUE);
                return;
            }

            if (accessToken != null && jwtTokenProvider.validateAccessToken(accessToken)) {
                setAuthentication(accessToken);
            } else if (accessToken == null && refreshToken == null) {
                filterChain.doFilter(request, response);
                return;
            }
            filterChain.doFilter(request, response);
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
            e.printStackTrace();
            errorCode = ErrorJwtCode.RUNTIME_EXCEPTION;
            setResponse(response, errorCode);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);

    }

    private void setAuthentication(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
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
