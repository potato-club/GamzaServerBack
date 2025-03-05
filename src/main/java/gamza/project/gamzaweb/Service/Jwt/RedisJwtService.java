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
            System.out.println("여기 걸리면 제대로 반환 맞음");
            return (Map<String, String>) object;
        }
        System.out.println("여기 걸리면 null 맞음");
        return null;
    }

    public void delValues(String token) {
        redisTemplate.delete(token);
    }

    public void addRefreshTokenInBlacklist(String token, long expiration) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(token, true, expiration, TimeUnit.NANOSECONDS);
    }

    public boolean isTokenValid(String token) {
        Map<String, String> values = getValues(token);
        return values == null;
    }

}
