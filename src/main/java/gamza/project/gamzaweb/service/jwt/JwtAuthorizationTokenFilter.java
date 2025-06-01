package gamza.project.gamzaweb.service.jwt;

import gamza.project.gamzaweb.utils.error.ErrorJwtCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisJwtService redisJwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        ErrorJwtCode errorCode;

//        if(AllowPath.isAllowed(path)) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            if (refreshToken != null && refreshToken.trim().isEmpty()) {
                setResponse(response, ErrorJwtCode.INVALID_VALUE); // 빈 값에 대한 에러 반환
                return;
            }

            if (refreshToken != null && path.contains("/reissue")) {
                filterChain.doFilter(request, response);
                return;
            }


        } catch (ExpiredJwtException e) {
            errorCode = ErrorJwtCode.EXPIRED_REFRESH_TOKEN;
            setResponse(response, errorCode);
            return;
        }

        try {
            String accessToken = jwtTokenProvider.resolveAccessToken(request);
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            if (refreshToken == null && accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            } else if (accessToken != null && refreshToken == null) {
                jwtTokenProvider.validateAccessToken(accessToken);
                filterChain.doFilter(request, response);
                return;
            } else if (accessToken != null && refreshToken != null) {
                setResponse(response, ErrorJwtCode.UNSUPPORTED_JWT_TOKEN);
                return;
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
            e.printStackTrace();
            errorCode = ErrorJwtCode.INVALID_VALUE;
            setResponse(response, errorCode);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        filterChain.doFilter(request, response);

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
