package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorCode;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}