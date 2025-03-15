package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorCode;

public class DuplicateException extends BusinessException {

    public DuplicateException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}