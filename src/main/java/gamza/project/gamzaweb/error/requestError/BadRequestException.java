package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorCode;

public class BadRequestException extends BusinessException {

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}