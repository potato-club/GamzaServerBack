package gamza.project.gamzaweb.Error;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

    RUNTIME_EXCEPTION(HttpStatus.BAD_REQUEST, "400","400 BAD REQUEST"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED,"401","401 UNAUTHORIZED"),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN,"403","403 FORBIDDEN"),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,"404","404 NOT FOUND"),
    INVALID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "405", "Invalid access: token in blacklist"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "500", "500 SERVER ERROR"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"5001","5001 EXPIRED ACCESS TOKEN"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"5002","5002 EXPIRED REFRESH TOKEN"),
    NOT_ALLOW_ACCESS_EXCEPTION(HttpStatus.BAD_REQUEST, "5003", "5003 Invalid Request"),
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED,"2001","2001 dosen't have the necessary permission");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
