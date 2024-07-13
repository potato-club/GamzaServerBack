package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class DuplicateException extends BusinessException {

    public DuplicateException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}