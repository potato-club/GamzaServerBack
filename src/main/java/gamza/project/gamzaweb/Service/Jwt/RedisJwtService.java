package gamza.project.gamzaweb.Service.Jwt;

import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.ExpiredRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisJwtService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setValues(String token, String email) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        operations.set(token, map, Duration.ofDays(7));
    }

    public Map<String, String> getValues(String token) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Object object = operations.get(token);

        if (object instanceof Map) {
            return (Map<String, String>) object;
        }
        return null;
    }

    public void delValues(String token) {
        redisTemplate.delete(token);
    }

    public void addRefreshTokenInBlacklist(String token, long expiration) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(token, true, expiration, TimeUnit.NANOSECONDS);
    }

    public boolean isTokenInBlacklist(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            throw new ExpiredRefreshTokenException("만료된 RT입니다. 다시 로그인 해주세요.", ErrorCode.UNAUTHORIZED_EXCEPTION);
        }
        return false;
    }




}
