package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}