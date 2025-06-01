package gamza.project.gamzaweb.utils.error;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorJwtCode {

    INVALID_JWT_FORMAT("1001","1001 INVALID JWT FORMAT", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED_JWT_TOKEN("1002","1002 UNSUPPORTED JWT TOKEN", HttpStatus.UNAUTHORIZED),
    INVALID_VALUE("1003","1003 INVALID VALUE", HttpStatus.UNAUTHORIZED),
    RUNTIME_EXCEPTION("1004","1004 RUNTIME EXCEPTION", HttpStatus.UNAUTHORIZED),
    EXPIRED_ACCESS_TOKEN("5001","5001 EXPIRED ACCESS TOKEN", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("5002","5002 EXPIRED REFRESH TOKEN", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorJwtCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}