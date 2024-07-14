package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}