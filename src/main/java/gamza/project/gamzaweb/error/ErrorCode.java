package gamza.project.gamzaweb.error;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorCode {

    BAD_REQUEST_EXCEPTION(HttpStatus.BAD_REQUEST, "400","400 BAD REQUEST"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED,"401","401 UNAUTHORIZED"),
    FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN,"403","403 FORBIDDEN"),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND,"404","404 NOT FOUND"),
    INVALID_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "405", "Invalid access: token in blacklist"),
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "500", "500 SERVER ERROR"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"5001","5001 EXPIRED ACCESS TOKEN"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"5002","5002 EXPIRED REFRESH TOKEN"),
    NOT_ALLOW_ACCESS_EXCEPTION(HttpStatus.BAD_REQUEST, "5003", "5003 Invalid Request"),
    UNAUTHORIZED_EXCEPTION(HttpStatus.UNAUTHORIZED,"2001","2001 dosen't have the necessary permission"),

    // 이미지 빌드 실패, 컨테이너 빌드 실패, 이미지 삭제 실패, 컨테이너 스탑 실패, 엔진엑스 업데이트 실패, 다이나믹 오류
    FAILED_IMAGE_BUILD(HttpStatus.INTERNAL_SERVER_ERROR,"3001", "3001 FAILED IMAGE BUILD"),
    FAILED_IMAGE_DELETE(HttpStatus.INTERNAL_SERVER_ERROR,"3002", "3002 FAILED IMAGE DELETE"),
    FAILED_IMAGE_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"3003", "3003 FAILED IMAGE DELETE"),
    FAILED_CONTAINER_BUILD(HttpStatus.INTERNAL_SERVER_ERROR, "3005", "3005 FAILED CONTAINER BUILD"),
    FAILED_CONTAINER_STOP(HttpStatus.INTERNAL_SERVER_ERROR, "3006", "3006 FAILED CONTAINER STOP"),
    NGINX_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"3009", "3009 NGINX ERROR"),

    // 프로젝트 오류
    FAILED_PROJECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"4001", "4001 PROJECT ERROR");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
