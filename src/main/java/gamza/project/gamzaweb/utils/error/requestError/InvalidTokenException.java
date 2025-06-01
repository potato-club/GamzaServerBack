package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorCode;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}