package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorCode;

public class DuplicateException extends BusinessException {

    public DuplicateException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}