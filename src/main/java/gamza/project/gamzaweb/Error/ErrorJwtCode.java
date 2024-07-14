package gamza.project.gamzaweb.Error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ErrorJwtCode {

    INVALID_JWT_FORMAT("1001","1001 INVALID JWT FORMAT"),
    UNSUPPORTED_JWT_TOKEN("1002","1002 UNSUPPORTED JWT TOKEN"),
    INVALID_VALUE("1003","1003 INVALID VALUE"),
    RUNTIME_EXCEPTION("1004","1004 RUNTIME EXCEPTION"),
    EXPIRED_ACCESS_TOKEN("5001","5001 EXPIRED ACCESS TOKEN"),
    EXPIRED_REFRESH_TOKEN("5002","5002 EXPIRED REFRESH TOKEN");

    private final String code;
    private final String message;

    ErrorJwtCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}